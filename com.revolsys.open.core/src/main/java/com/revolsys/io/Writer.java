package com.revolsys.io;

public interface Writer<T> extends ObjectWithProperties, AutoCloseable {
  @Override
  void close();

  void flush();

  void open();

  void write(T object);
}
