package com.poc.k8s;

import java.io.IOException;
import java.util.HashMap;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
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
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Yaml;

public class IngressBeta {

    private static final String API_VERSION = "extensions/v1beta1";
    private static final String INGRESS_KIND = "Ingress";
    private static final String NAMESPACE = "test";
    private static final String INGRESS_NAME = "test-ingress";
    private static final String INGRESS_NAME_2 = "test-ingress-2";

    public static void main( String[] args ) throws IOException, ApiException  {
        ApiClient client = ClientBuilder.cluster().build();
        client.setDebugging(true);

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        ExtensionsV1beta1Api ingressApi = new ExtensionsV1beta1Api(client);

        // create ingresses
        ExtensionsV1beta1Ingress ingressDescriptor = buildNamespaceIngress(INGRESS_NAME);
        System.out.println("=====================================+>>>");
        System.out.println(Yaml.dump(ingressDescriptor));
        System.out.println("=====================================+>>>");
        try {
            ExtensionsV1beta1Ingress createResponse = ingressApi.createNamespacedIngress(NAMESPACE, ingressDescriptor, null, null, null);
            System.out.println("==============+>>> response");
            System.out.println(createResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ApiResponse<ExtensionsV1beta1Ingress> createResponse2 = ingressApi.createNamespacedIngressWithHttpInfo(NAMESPACE, buildNamespaceIngress(INGRESS_NAME_2), null, null, null);
            if (createResponse2 == null) {
                System.out.println("response is null");
                System.out.println(createResponse2);
            } else {
                System.out.println("==============+>>> item");
                System.out.println(createResponse2.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // list ingressess
        try {
            ExtensionsV1beta1IngressList responseList = ingressApi.listNamespacedIngress(NAMESPACE, null, null, null, null, null, null, null, null, null);
            if (responseList == null) {
                System.out.println("response is null");
                System.out.println(responseList);
            } else {
                System.out.println("size: " +  responseList.getItems().size());
                for (ExtensionsV1beta1Ingress item : responseList.getItems()) {
                    System.out.println("==============+>>> item");
                    System.out.println(item.getMetadata());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        V1Status status = ingressApi.deleteNamespacedIngress(INGRESS_NAME_2, NAMESPACE, null, null, null, null, null, null);
        System.out.println("========================+>>>>>> status");
        System.out.println(status);
        System.out.println("========================+>>>>>> status");

        // list ingressess
        try {
            ExtensionsV1beta1IngressList responseList = ingressApi.listNamespacedIngress(NAMESPACE, null, null, null, null, null, null, null, null, null);
            if (responseList == null) {
                System.out.println("response is null");
                System.out.println(responseList);
            } else {
                System.out.println("size: " +  responseList.getItems().size());
                for (ExtensionsV1beta1Ingress item : responseList.getItems()) {
                    System.out.println("==============+>>> item");
                    System.out.println(item.getMetadata());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ExtensionsV1beta1Ingress buildNamespaceIngress(final String ingressName) {
        var annotations = new HashMap<String, String>();
        annotations.put("ingress.bluemix.net/redirect-to-https", "True");
        annotations.put("ingress.kubernetes.io/force-ssl-redirect", "True");
        annotations.put("ingressclass.kubernetes.io/is-default-class", "True");

        var workerPath = "/worker";
        var workerHostName = "subdomain.k8s.test.local";
        var ingressSecretName = "registry-secret";
        var serviceName = "test-svc";
        var servicePort = 4200;

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
                                    .backend(
                                        new ExtensionsV1beta1IngressBackend()
                                            .serviceName(serviceName)
                                            .servicePort(new IntOrString(servicePort))
                                    )
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
            .kind(INGRESS_KIND)
            .metadata(getMetaData(ingressName).annotations(annotations))
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
