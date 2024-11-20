package io.weaviate.integration.client.async.graphql;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.graphql.api.Get;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.Targets;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.PQConfig;
import io.weaviate.client.v1.misc.model.SQConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerCompose;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ClientGraphQLMultiTargetSearchTest extends AbstractAsyncClientTest {
  private String httpHost;
  private String grpcHost;

  private WeaviateClient syncClient;
  private WeaviateAsyncClient client;
  private GraphQL gql;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    httpHost = compose.getHttpHostAddress();

    Config config = new Config("http", httpHost);
    config.setGRPCSecured(false);
    config.setGRPCHost(grpcHost);
    syncClient = new WeaviateClient(config);

    client = syncClient.async();
    gql = client.graphQL();
  }

  @After
  public void after() {
    client.close();
  }

  private final String id1 = "00000000-0000-0000-0000-000000000001";
  private final String id2 = "00000000-0000-0000-0000-000000000002";
  private final String id3 = "00000000-0000-0000-0000-000000000003";

  private final String titleAndContent = "titleAndContent";
  private final String title1 = "title1";
  private final String title2 = "title2";
  private final String bringYourOwnVector = "bringYourOwnVector";
  private final String bringYourOwnVector2 = "bringYourOwnVector2";

  @Test
  public void shouldPerformMultiTargetSearch() throws InterruptedException {
    String className = "MultiTargetSearch";
    setupDB(className);
    Field _additional = _additional("id", "distance");
    // nearText
    Map<String, Float> weights = new HashMap<>();
    weights.put(titleAndContent, 0.1f);
    weights.put(title1, 0.6f);
    weights.put(title2, 0.3f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ titleAndContent, title1, title2 })
      .combinationMethod(Targets.CombinationMethod.manualWeights)
      .weights(weights)
      .build();
    NearTextArgument nearText = gql.arguments().nearTextArgBuilder()
      .concepts(new String[]{ "Water black" })
      .targets(targets)
      .build();
    Result<GraphQLResponse> response = doGet(get -> get
      .withClassName(className)
      .withNearText(nearText)
      .withFields(_additional));
    assertGetContainsIds(response, className, id1, id2, id3);
    // nearVector with single vector-per-target
    Map<String, Float[]> vectorPerTarget = new HashMap<>();
    vectorPerTarget.put(bringYourOwnVector, new Float[]{ .99f, .88f, .77f });
    vectorPerTarget.put(bringYourOwnVector2, new Float[]{ .11f, .22f, .33f });
    weights = new HashMap<String, Float>() {{
      this.put(bringYourOwnVector, 0.1f);
      this.put(bringYourOwnVector2, 0.6f);
    }};
    targets = Targets.builder()
      .targetVectors(new String[]{ bringYourOwnVector, bringYourOwnVector2 })
      .combinationMethod(Targets.CombinationMethod.manualWeights)
      .weights(weights)
      .build();
    final NearVectorArgument nearVector1 = gql.arguments().nearVectorArgBuilder()
      .vectorPerTarget(vectorPerTarget)
      .targets(targets).build();
    response = doGet(get -> get
      .withClassName(className)
      .withNearVector(nearVector1)
      .withFields(_additional));
    assertNull("check error in response:", response.getError());
    assertGetContainsIds(response, className, id2, id3);
    // nearVector with multiple vector-per-target
    Map<String, Float[][]> vectorsPerTarget = new HashMap<>();
    vectorsPerTarget.put(bringYourOwnVector, new Float[][]{ new Float[]{ .99f, .88f, .77f }, new Float[]{ .99f, .88f, .77f } });
    vectorsPerTarget.put(bringYourOwnVector2, new Float[][]{ new Float[]{ .11f, .22f, .33f } });
    Map<String, Float[]> weightsMulti = new HashMap<>();
    weightsMulti.put(bringYourOwnVector, new Float[]{ 0.5f, 0.5f });
    weightsMulti.put(bringYourOwnVector2, new Float[]{ 0.6f });
    targets = Targets.builder()
      .targetVectors(new String[]{ bringYourOwnVector, bringYourOwnVector2 })
      .combinationMethod(Targets.CombinationMethod.manualWeights)
      .weightsMulti(weightsMulti)
      .build();
    final NearVectorArgument nearVector2 = gql.arguments().nearVectorArgBuilder()
      .vectorsPerTarget(vectorsPerTarget)
      .targets(targets).build();
    response = doGet(get -> get
      .withClassName(className)
      .withNearVector(nearVector2)
      .withFields(_additional));
    assertNull("check error in response:", response.getError());
    assertGetContainsIds(response, className, id2, id3);
    // nearObject
    targets = Targets.builder()
      .targetVectors(new String[]{ bringYourOwnVector, bringYourOwnVector2, titleAndContent, title1, title2 })
      .combinationMethod(Targets.CombinationMethod.average)
      .build();
    NearObjectArgument nearObject = gql.arguments().nearObjectArgBuilder()
      .id(id3).targets(targets).build();
    response = doGet(get -> get
      .withClassName(className)
      .withNearObject(nearObject)
      .withFields(_additional));
    assertGetContainsIds(response, className, id2, id3);
  }

  private void setupDB(String className) {
    // clean
    Result<Boolean> delete = syncClient.schema().allDeleter().run();
    assertThat(delete).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
    // create class
    List<Property> properties = Arrays.asList(
      Property.builder()
        .name("title")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build(),
      Property.builder()
        .name("content")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build(),
      Property.builder()
        .name("title1")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build(),
      Property.builder()
        .name("title2")
        .dataType(Collections.singletonList(DataType.TEXT))
        .build()
    );
    Map<String, WeaviateClass.VectorConfig> vectorConfig = new HashMap<>();
    vectorConfig.put(titleAndContent, getTitleAndContentVectorConfig());
    vectorConfig.put(title1, getTitle1VectorConfig());
    vectorConfig.put(title2, getTitle2VectorConfig());
    vectorConfig.put(bringYourOwnVector, getBringYourOwnVectorVectorConfig());
    vectorConfig.put(bringYourOwnVector2, getBringYourOwnVectorVectorConfig2());
    Result<Boolean> createResult = syncClient.schema().classCreator()
      .withClass(WeaviateClass.builder()
        .className(className)
        .properties(properties)
        .vectorConfig(vectorConfig)
        .build()
      )
      .run();
    assertThat(createResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
    // add data
    // obj1
    Map<String, Object> props1 = new HashMap<>();
    props1.put("title", "The Lord of the Rings");
    props1.put("content", "A great fantasy novel");
    props1.put("title1", "J.R.R. Tolkien The Lord of the Rings");
    props1.put("title2", "Rings");
    Float[] vector1a = new Float[]{ 0.77f, 0.88f, 0.77f };
    Map<String, Float[]> vectors1 = new HashMap<>();
    vectors1.put("bringYourOwnVector", vector1a);
    // don't add vector for bringYourOwnVector2
    // obj2
    Map<String, Object> props2 = new HashMap<>();
    props2.put("title", "Black Oceans");
    props2.put("content", "A great science fiction book");
    props2.put("title1", "Jacek Dukaj Black Oceans");
    props2.put("title2", "Water");
    Float[] vector2a = new Float[]{ 0.11f, 0.22f, 0.33f };
    Float[] vector2b = new Float[]{ 0.11f, 0.11f, 0.11f };
    Map<String, Float[]> vectors2 = new HashMap<>();
    vectors2.put("bringYourOwnVector", vector2a);
    vectors2.put("bringYourOwnVector2", vector2b);
    // obj2
    Map<String, Object> props3 = new HashMap<>();
    props3.put("title", "Into the Water");
    props3.put("content", "New York Times bestseller and global phenomenon The Girl on the Train returns with Into the Water");
    props3.put("title1", "Paula Hawkins Into the Water");
    props3.put("title2", "Water go into it");
    Float[] vector3a = new Float[]{ 0.99f, 0.88f, 0.77f };
    Float[] vector3b = new Float[]{ 0.99f, 0.88f, 0.77f };
    Map<String, Float[]> vectors3 = new HashMap<>();
    vectors3.put("bringYourOwnVector", vector3a);
    vectors3.put("bringYourOwnVector2", vector3b);

    WeaviateObject obj1 = createObject(id1, className, props1, vectors1);
    WeaviateObject obj2 = createObject(id2, className, props2, vectors2);
    WeaviateObject obj3 = createObject(id3, className, props3, vectors3);

    Result<ObjectGetResponse[]> result = syncClient.batch().objectsBatcher()
      .withObjects(obj1, obj2, obj3)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(3);
  }

  private WeaviateClass.VectorConfig getTitleAndContentVectorConfig() {
    Map<String, Object> titleAndContent = new HashMap<>();
    Map<String, Object> text2vecContextionarySettings = new HashMap<>();
    text2vecContextionarySettings.put("vectorizeClassName", false);
    text2vecContextionarySettings.put("properties", new String[]{ "title", "content" });
    titleAndContent.put("text2vec-contextionary", text2vecContextionarySettings);
    return getHNSWSQVectorConfig(titleAndContent);
  }

  private WeaviateClass.VectorConfig getTitle1VectorConfig() {
    Map<String, Object> titleAndContent = new HashMap<>();
    Map<String, Object> text2vecContextionarySettings = new HashMap<>();
    text2vecContextionarySettings.put("vectorizeClassName", false);
    text2vecContextionarySettings.put("properties", new String[]{ "title1" });
    titleAndContent.put("text2vec-contextionary", text2vecContextionarySettings);
    return getHNSWPQVectorConfig(titleAndContent);
  }

  private WeaviateClass.VectorConfig getTitle2VectorConfig() {
    Map<String, Object> titleAndContent = new HashMap<>();
    Map<String, Object> text2vecContextionarySettings = new HashMap<>();
    text2vecContextionarySettings.put("vectorizeClassName", false);
    text2vecContextionarySettings.put("properties", new String[]{ "title2" });
    titleAndContent.put("text2vec-contextionary", text2vecContextionarySettings);
    return getHNSWVectorConfig(titleAndContent);
  }

  private WeaviateClass.VectorConfig getBringYourOwnVectorVectorConfig() {
    Map<String, Object> byov = new HashMap<>();
    byov.put("none", new Object());
    return getFlatBQVectorConfig(byov);
  }

  private WeaviateClass.VectorConfig getBringYourOwnVectorVectorConfig2() {
    Map<String, Object> byov = new HashMap<>();
    byov.put("none", new Object());
    return getFlatVectorConfig(byov);
  }

  private WeaviateClass.VectorConfig getFlatBQVectorConfig(Map<String, Object> vectorizerConfig) {
    return WeaviateClass.VectorConfig.builder()
      .vectorIndexType("flat")
      .vectorizer(vectorizerConfig)
      .vectorIndexConfig(VectorIndexConfig.builder()
        .bq(BQConfig.builder().enabled(true).build())
        .build())
      .build();
  }

  private WeaviateClass.VectorConfig getFlatVectorConfig(Map<String, Object> vectorizerConfig) {
    return WeaviateClass.VectorConfig.builder()
      .vectorIndexType("flat")
      .vectorizer(vectorizerConfig)
      .build();
  }

  private WeaviateClass.VectorConfig getHNSWVectorConfig(Map<String, Object> vectorizerConfig) {
    return WeaviateClass.VectorConfig.builder()
      .vectorIndexType("hnsw")
      .vectorizer(vectorizerConfig)
      .build();
  }

  private WeaviateClass.VectorConfig getHNSWPQVectorConfig(Map<String, Object> vectorizerConfig) {
    return WeaviateClass.VectorConfig.builder()
      .vectorIndexType("hnsw")
      .vectorizer(vectorizerConfig)
      .vectorIndexConfig(VectorIndexConfig.builder()
        .pq(PQConfig.builder().enabled(true).build())
        .build())
      .build();
  }

  private WeaviateClass.VectorConfig getHNSWSQVectorConfig(Map<String, Object> vectorizerConfig) {
    return WeaviateClass.VectorConfig.builder()
      .vectorIndexType("hnsw")
      .vectorizer(vectorizerConfig)
      .vectorIndexConfig(VectorIndexConfig.builder()
        .sq(SQConfig.builder().enabled(true).build())
        .build())
      .build();
  }

  private WeaviateObject createObject(String id, String className, Map<String, Object> props, Map<String, Float[]> vectors) {
    WeaviateObject.WeaviateObjectBuilder obj = WeaviateObject.builder()
      .id(id)
      .className(className)
      .properties(props);
    if (vectors != null) {
      obj = obj.vectors(vectors);
    }
    return obj.build();
  }

  @SuppressWarnings("unchecked")
  private void assertGetContainsIds(Result<GraphQLResponse> response, String className, String... expectedIds) {
    assertThat(response).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<String, Object>) data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<String, Object>) get).get(className)).isInstanceOf(List.class).asList()
      .hasSize(expectedIds.length)
      .extracting(obj -> ((Map<String, Object>) obj).get("_additional"))
      .extracting(add -> ((Map<String, Object>) add).get("id"))
      .containsExactlyInAnyOrder((Object[]) expectedIds);
  }

  private Result<GraphQLResponse> doGet(Consumer<Get> build) {
    Get get = gql.get();
    build.accept(get);
    try {
      return get.run().get();
    } catch (InterruptedException | ExecutionException e) {
      fail("graphQL.get(): " + e.getMessage());
      return null;
    }
  }
}