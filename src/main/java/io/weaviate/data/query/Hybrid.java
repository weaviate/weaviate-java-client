package io.weaviate.data.query;

public class Hybrid extends BM25 {
  public final Fusion fusion;
  public final Float alpha;

  public static class OptionsBuilder extends BM25.OptionsBuilder {
    public OptionsBuilder(String query) {
      super(query);
    }

    private Fusion fusion;

    public OptionsBuilder fusion(Fusion fusion) {
      this.fusion = fusion;
      return this;
    }

    private Float alpha;

    public OptionsBuilder alpha(float alpha) {
      this.alpha = alpha;
      return this;
    }

    @Override
    public OptionsBuilder returnProperties(String... properties) {
      super.returnProperties(properties);
      return this;
    }

    @Override
    public OptionsBuilder limit(int limit) {
      super.limit(limit);
      return this;
    }

    @Override
    public OptionsBuilder metadata(Metadata... metadata) {
      super.metadata(metadata);
      return this;
    }

    Hybrid build() {
      return new Hybrid(super.query, returnProperties, limit, fusion, alpha, metadata);
    }
  }

  Hybrid(String query, String[] returnProperties, int limit, Fusion fusion, Integer alpha, Metadata[] metadata) {
    super(query, returnProperties, limit, metadata);
    this.fusion = fusion;
    this.alpha = alpha;
  }

  public static enum Fusion {
    RANKED("rankedFusion"),
    RELATIVE_SCORE("relativeScoreFusion");

    private final String name;

    private Fusion(String name) {
      this.name = name;
    }
  }
}
