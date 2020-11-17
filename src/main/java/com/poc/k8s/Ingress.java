package com.poc.k8s;

import java.io.IOException;
import java.util.HashMap;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressPath;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Ingress;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressBackend;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressList;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressRule;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressSpec;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressStatus;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressTLS;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1LoadBalancerStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.options.CreateOptions;
import io.kubernetes.client.util.generic.KubernetesApiResponse;

public class Ingress {

    private static final String API_VERSION = "v1";
    private static final String INGRESS = "Ingress";
    private static final String NAMESPACE = "test";
    private static final String INGRESS_NAME = "test-ingress";

    public static void main( String[] args ) throws IOException, ApiException  {
        // loading the in-cluster config, including:
        //   1. service-account CA
        //   2. service-account bearer-token
        //   3. service-account namespace
        //   4. master endpoints(ip, port) from pre-set environment variables
        ApiClient client = ClientBuilder.cluster().build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        GenericKubernetesApi<ExtensionsV1beta1Ingress, ExtensionsV1beta1IngressList> ingressApi =
            new GenericKubernetesApi<ExtensionsV1beta1Ingress, ExtensionsV1beta1IngressList>(
                ExtensionsV1beta1Ingress.class,
                ExtensionsV1beta1IngressList.class,
                "extensions",
                "extensions/v1beta1",
                "ingresses",
                client
            );

        // create ingresses
        ExtensionsV1beta1Ingress ingressDescriptor = buildNamespaceIngress();
        ingressApi.create(ingressDescriptor);
        ingressApi.create(NAMESPACE, buildNamespaceIngress(), new CreateOptions());

        // list ingressess
        KubernetesApiResponse<ExtensionsV1beta1IngressList> responseList = ingressApi.list(NAMESPACE);
            // ingressApi.listNamespacedPod("test", null, null, null, null, null, null, null, null, null);
            // api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        if (responseList == null) {
            System.out.println("response is null");
            System.out.println(responseList);
        } else if (responseList.getObject() == null) {
            System.out.println("response.getObject() is null");
            System.out.println(responseList.getObject());
        } else {
            for (ExtensionsV1beta1Ingress item : responseList.getObject().getItems()) {
                System.out.println("==============+>>> item");
                System.out.println(item.getMetadata());
            }
        }
    }

    // private static 

    private static ExtensionsV1beta1Ingress buildNamespaceIngress() {
        var annotations = new HashMap<String, String>();
        annotations.put("ingress.bluemix.net/redirect-to-https", "True");
        annotations.put("ingress.kubernetes.io/force-ssl-redirect", "True");
        annotations.put("ingressclass.kubernetes.io/is-default-class", "True");

        var workerPath = "/worker";
        var workerHostName = "subdomain.k8s.test.local";
        var ingressSecretName = "registry-secret";

        var ingressSpec = new ExtensionsV1beta1IngressSpec()
            .addTlsItem(
                new ExtensionsV1beta1IngressTLS().addHostsItem(workerHostName).secretName(ingressSecretName)
            )
            .addRulesItem(
                new ExtensionsV1beta1IngressRule()
                    .host("k8s.test.local")
                    .http(
                        new ExtensionsV1beta1HTTPIngressRuleValue()
                            .addPathsItem(
                                new ExtensionsV1beta1HTTPIngressPath()
                                    .path(workerPath)
                                    .backend(new ExtensionsV1beta1IngressBackend().serviceName("serviceName").servicePort(new IntOrString("3200")))
                            )
                    )
            );

        var ingressStatus = new ExtensionsV1beta1IngressStatus().loadBalancer(
            new V1LoadBalancerStatus()
                .addIngressItem(new
                    V1LoadBalancerIngress().hostname(workerHostName))
                );

        return new ExtensionsV1beta1Ingress()
            .apiVersion(API_VERSION)
            .kind(INGRESS)
            .metadata(getMetaData(INGRESS_NAME).annotations(annotations))
            .spec(ingressSpec)
            .status(ingressStatus);
    }

    private static V1ObjectMeta getMetaData(final String name) {
        final HashMap<String, String> labels = new HashMap<>();
        labels.put("name", name);
        labels.put("component", "test");

        return new V1ObjectMeta()
            .namespace(NAMESPACE)
            .name(name)
            .labels(labels);
    }
}
