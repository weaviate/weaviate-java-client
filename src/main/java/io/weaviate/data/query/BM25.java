package io.weaviate.data.query;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.MODULE)
public class BM25 {
  public final String query;
  public final String[] returnProperties;

  // TODO: push these up to the common query parameters
  public final Integer limit;
  public final Metadata[] metadata;

  public static class OptionsBuilder {
    final String query;

    public OptionsBuilder(String query) {
      this.query = query;
    }

    String[] returnProperties;

    public OptionsBuilder returnProperties(String... properties) {
      this.returnProperties = properties;
      return this;
    }

    Integer limit;

    public OptionsBuilder limit(int limit) {
      this.limit = limit;
      return this;
    }

    Metadata[] metadata;

    public OptionsBuilder metadata(Metadata... metadata) {
      this.metadata = metadata;
      return this;
    }

    BM25 build() {
      return new BM25(query, returnProperties, limit, metadata);
    }
  }

  public static BM25 with(String query, Consumer<OptionsBuilder> optional) {
    OptionsBuilder b = new OptionsBuilder(query);
    optional.accept(b);
    return b.build();
  }
}
