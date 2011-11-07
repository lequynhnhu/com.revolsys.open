package com.revolsys.parallel.channel;

public interface SelectableInput {
  int EMPTY = 0;
  int AVAILABLE = 1;
  int CLOSED = 2;
  boolean enable(MultiInputSelector alt);

  boolean disable();
  
  boolean isClosed();
}