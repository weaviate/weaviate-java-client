package io.weaviate.collections;

public class Property {
  public final String name;
  public final DataType type;
  public final String[] references;

  public static Property text(String name) {
    return new Property(name, DataType.TEXT);
  }

  public static Property reference(String name, DataType type, String... references) {
    return new Property(name, type, references);
  }

  public Property(String name, DataType type, String... references) {
    this.name = name;
    this.type = type;
    this.references = references;
  }

  public enum DataType {
    TEXT,
    NUMBER,
    REFERENCE;
  }
}
