package com.revolsys.io;

import java.util.Iterator;

import javax.annotation.PreDestroy;

import com.revolsys.collection.AbstractIterator;

public abstract class AbstractMultipleIteratorReader<T> extends
  AbstractReader<T> implements Iterator<T> {

  private AbstractIterator<T> iterator;

  private boolean open;

  @PreDestroy
  public void close() {
    if (iterator != null) {
      iterator.close();
      iterator = null;
    }
  }

  public void open() {
    if (!open) {
      open = true;
      hasNext();
    }
  }

  public Iterator<T> iterator() {
    open();
    return this;
  }

  public boolean hasNext() {
    if (iterator == null) {
      iterator = getNextIterator();
      if (iterator == null) {
        close();
        return false;
      }
    }
    while (!iterator.hasNext()) {
      iterator.close();
      iterator = getNextIterator();
      if (iterator == null) {
        return false;
      }
    }
    return true;
  }

  protected abstract AbstractIterator<T> getNextIterator();

  public T next() {
    final T object = iterator.next();
    process(object);
    return object;
  }

  protected void process(T object) {
  }

  public void remove() {
    iterator.remove();
  }
}