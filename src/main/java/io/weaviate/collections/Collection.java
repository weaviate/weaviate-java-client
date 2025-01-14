package io.weaviate.collections;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Collection {
  public final String name;
  public final Property[] properties;

  public class Builder {
    public String name;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Property[] properties;

    public Builder properties(Property... properties) {
      this.properties = properties;
      return this;
    }
  }
}
