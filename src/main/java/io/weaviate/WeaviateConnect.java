package io.weaviate;

public class WeaviateConnect {
  public static WeaviateClient toLocal() {
    return new WeaviateClient(new WeaviateClient.Config("http", "locahost:8080"));
  }
}
