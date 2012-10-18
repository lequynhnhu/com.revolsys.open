package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class ReferenceEntrySet<K, V> extends AbstractSet<Entry<K, V>> {

  private Collection<Entry<K, Reference<V>>> collection;

  public ReferenceEntrySet(Collection<Entry<K, Reference<V>>> collection) {
    this.collection = collection;
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return new ReferenceEntryIterator<K, V>(collection);
  }

  @Override
  public int size() {
    return collection.size();
  }

}