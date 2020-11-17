## Commands

build maven:

```
mvn clean package

java -Dexec.mainClass="com.bp2s.river.App" -Djdk.tls.client.protocols=TLSv1.2 -jar target/k8s-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar

java -Djdk.tls.client.protocols=TLSv1.2 -jar target/k8s-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

push image to docker hub:

```
docker build -f Dockerfile -t k8s-deploy-test:1.0-SNAPSHOT .

docker tag bb10c00ec99c stankoua/k8s-deploy-test:1.0-SNAPSHOT

docker push stankoua/k8s-deploy-test:1.0-SNAPSHOT
```

Dans le container:

```
SERVICEACCOUNT_CA_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"

java -Djdk.tls.client.protocols=TLSv1.2 -Dlog4j.debug=true -jar target/k8s-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar
```


Commandes k8s:

Pour exécuter le projet:

```
kubectl apply -f k8s-security.yaml
kubectl apply -f k8s-pod.yaml
```

Pour tout supprimer

```
kubectl delete -f k8s-pod.yaml
kubectl delete -f k8s-security.yaml
```

Lancer bash dans le container:

```
kubectl exec -it --namespace test test-pod-76666d944c-hn2pv bash
```
