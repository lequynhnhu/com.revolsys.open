package com.revolsys.collection;

import java.util.UUID;
import java.util.WeakHashMap;

public class WeakUuidObjectMap extends WeakHashMap<UUID, Object> {
  @SuppressWarnings("unchecked")
  public static <T> T getObject(final UUID uuid) {
    return (T)INSTANCE.get(uuid);
  }

  public static void putObject(final UUID uuid, final Object value) {
    INSTANCE.put(uuid, value);
  }

  private static WeakUuidObjectMap INSTANCE = new WeakUuidObjectMap();
}
