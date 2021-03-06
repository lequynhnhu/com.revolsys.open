package com.revolsys.factory;

import java.util.HashSet;
import java.util.Set;

public class HashSetFactory<V> implements Factory<Set<V>> {
  @SuppressWarnings("unchecked")
  public static <V1> HashSetFactory<V1> get() {
    return INSTANCE;
  }

  @SuppressWarnings("rawtypes")
  private static final HashSetFactory INSTANCE = new HashSetFactory<>();

  @Override
  public Set<V> create() {
    return new HashSet<>();
  }
}
