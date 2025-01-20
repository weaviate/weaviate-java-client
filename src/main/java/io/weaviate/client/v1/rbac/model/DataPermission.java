package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DataPermission implements Permission<DataPermission> {
  final transient String action;
  final String collection;
  final String object;
  final String tenant;

  public DataPermission(Action action, String collection) {
    this(action, collection, "*", "*");
  }

  DataPermission(String action, String collection) {
    this(CustomAction.fromString(Action.class, action), collection);
  }

  private DataPermission(Action action, String collection, String object, String tenant) {
    this.action = action.getValue();
    this.collection = collection;
    this.object = object;
    this.tenant = tenant;
  }

  @Override
  public WeaviatePermission toWeaviate() {
    return new WeaviatePermission(this.action, this);
  }

  @AllArgsConstructor
  public enum Action implements CustomAction {
    CREATE("create_data"),
    READ("read_data"),
    UPDATE("update_data"),
    DELETE("delete_data"),
    MANAGE("manage_data");

    @Getter
    private final String value;
  }
}