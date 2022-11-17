package technology.semi.weaviate.client.v1.graphql.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GraphQLQuery {
  String operationName;
  String query;
  Object variables;
}
