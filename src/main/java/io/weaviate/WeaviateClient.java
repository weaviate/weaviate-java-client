package io.weaviate;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import io.weaviate.collections.CollectionsClient;
import io.weaviate.data.CollectionClient;
import io.weaviate.data.WeaviateObject;

public class WeaviateClient {
  public final CollectionsClient collections;

  private final Config config;
  private final HttpClient httpClient;

  public WeaviateClient(Config config) {
    this.config = config;
    this.httpClient = HttpClientBuilder.create().build();
    this.collections = new CollectionsClient(httpClient);
  }

  public <T extends WeaviateObject> CollectionClient<T> useCollection() {
    return new CollectionClient<T>(httpClient);
  }

  public static class Config {
    private final String scheme;
    private final String host;
    private final String version;

    public Config(String scheme, String host) {
      this.scheme = scheme;
      this.host = host;
      this.version = "v1";
    }

    public String baseUrl() {
      return scheme + "://" + host + "/" + version;
    }
  }
}
