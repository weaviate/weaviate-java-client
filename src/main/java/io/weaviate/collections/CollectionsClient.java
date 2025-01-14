package io.weaviate.collections;

import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.HttpClient;

import io.weaviate.data.WeaviateObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionsClient {
  private HttpClient httpClient;

  public Collection get(String name) {
    return null;
  }

  public <T extends WeaviateObject> Collection get(Class<T> cls) {
    return null;
  }

  public <T extends WeaviateObject> Collection create(Class<T> cls) {
    return null;
  }

  public <T extends WeaviateObject> Collection create(String name, Consumer<Collection.Builder> options) {
    return null;
  }
}
