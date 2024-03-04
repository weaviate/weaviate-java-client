package io.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.File;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearVideoArgument implements Argument {

  String video;
  File videoFile;
  Float certainty;
  Float distance;
  String[] targetVectors;

  @Override
  public String build() {
    return NearMediaArgumentHelper.builder()
      .certainty(certainty)
      .distance(distance)
      .targetVectors(targetVectors)
      .data(video)
      .dataFile(videoFile)
      .mediaField("video")
      .mediaName("nearVideo")
      .build().build();
  }
}
