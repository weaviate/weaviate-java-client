package io.weaviate.client.v1.classifications.model;

import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationMeta {
  Date completed;
  Long count;
  Long countFailed;
  Long countSucceeded;
  Date started;
}