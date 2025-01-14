package io.weaviate.data.query;

public class BaseQuery {
  public final Integer limit;
  public final Metadata[] metadata;

  BaseQuery(Integer limit, Metadata[] metadata) {
    this.limit = limit;
    this.metadata = metadata;
  }

  @SuppressWarnings("unchecked")
  public abstract static class Builder<T extends Builder<T>> {
    Integer limit;

    public T limit(int limit) {
      this.limit = limit;
      return (T) this;
    }

    Metadata[] metadata;

    public T metadata(Metadata... metadata) {
      this.metadata = metadata;
      return (T) this;
    }
  }
}
