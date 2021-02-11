package com.poc.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A simple example of how to use the Java API from an application outside a kubernetes cluster
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.KubeConfigFileClientExample"
 *
 * <p>From inside $REPO_DIR/examples
 */
public class App2 {
  public static void main(String[] args) throws IOException, ApiException {

    // file path to your KubeConfig

    final String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

    // loading the out-of-cluster config, a kubeconfig from file-system
    final ApiClient client =
        ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

    // set the global default api-client to the in-cluster one from above
    Configuration.setDefaultApiClient(client);

    // the CoreV1Api loads default api-client from global configuration.
    final CoreV1Api api = new CoreV1Api();

    // invokes the CoreV1Api client
    final V1PodList list =
        api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

    final var namespace = "default";
    final V1Pod podBody = new V1Pod()
      .apiVersion("v1")
      .metadata(
        new V1ObjectMeta()
          .labels(Map.of("k1", "v1"))
          .name("test-pod")
          .namespace(namespace)
      )
      .spec(
        new V1PodSpec()
          .containers(List.of(
            new V1Container()
              .name("test-pod")
              .image("gcr.io/google-samples/node-hello:1.0")
              .ports(List.of(new V1ContainerPort().containerPort(8080)))
          ))
      );
    final V1Pod createdPod = api.createNamespacedPod(namespace, podBody, null, null, null);
    System.out.println(createdPod.getStatus().getPhase());

    for (V1Pod item : list.getItems()) {
      System.out.println(item.getMetadata().getName());
    }
  }
}
