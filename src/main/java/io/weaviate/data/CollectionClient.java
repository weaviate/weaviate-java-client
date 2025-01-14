package io.weaviate.data;

import java.util.List;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.HttpClient;

import io.weaviate.data.query.BM25;

public class CollectionClient<T extends WeaviateObject> {
  public final DataClient data;
  public final QueryClient query;

  public CollectionClient(HttpClient httpClient) {
    this.httpClient = httpClient;
    this.data = null;
    this.query = null;
  }

  private HttpClient httpClient;

  public class DataClient {
    public T insert(T object) {
      return null;
    };

    public T insertMany(List<T> object) {
      return null;
    };
  }

  public class QueryClient {
    public List<T> bm25(String query) {
      return bm25(query, opt -> {
      });
    }

    public List<T> bm25(String query, Consumer<BM25.OptionsBuilder> optional) {
      BM25 search = BM25.with(query, optional);
      // Create GraphQL request and send;
      return null;
    }

    public List<T> hybrid(String query, Consumer<Hybrid.OptionsBuilder> optional) {
      Hybrid search = Hybrid.with(query, optional);
      // Create GraphQL request and send;
      return null;
    }
  }
}
