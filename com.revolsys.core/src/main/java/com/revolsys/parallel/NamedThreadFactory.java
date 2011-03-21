package com.revolsys.parallel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

  private static final AtomicInteger poolNumber = new AtomicInteger(1);

  private ThreadGroup group;

  private ThreadGroup parentGroup;

  private final AtomicInteger threadNumber = new AtomicInteger(1);

  private String namePrefix;

  private String threadNamePrefix;

  private int priority;

  public NamedThreadFactory() {
    this(Thread.NORM_PRIORITY);
  }

  public NamedThreadFactory(final int priority) {
    this.priority = priority;
    final SecurityManager securityManager = System.getSecurityManager();
    if (securityManager == null) {
      final Thread currentThread = Thread.currentThread();
      parentGroup = currentThread.getThreadGroup();
    } else {
      parentGroup = securityManager.getThreadGroup();
    }
    this.namePrefix = "pool-" + poolNumber.getAndIncrement();
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public ThreadGroup getParentGroup() {
    return parentGroup;
  }

  public int getPriority() {
    return priority;
  }

  public Thread newThread(final Runnable runnable) {
    synchronized (threadNumber) {
      if (group == null) {
        this.threadNamePrefix = this.namePrefix + "-thread-";
        this.group = new ThreadGroup(parentGroup, namePrefix);
      }
    }

    final String threadName = threadNamePrefix + threadNumber.getAndIncrement();
    final LoggingRunnable loggingRunnable = new LoggingRunnable(runnable);
    final Thread thread = new Thread(group, loggingRunnable, threadName, 0);
    if (thread.isDaemon()) {
      thread.setDaemon(false);
    }
    thread.setPriority(priority);
    return thread;
  }

  public void setNamePrefix(final String namePrefix) {
    this.namePrefix = namePrefix;
  }

  public void setParentGroup(final ThreadGroup parentGroup) {
    this.parentGroup = parentGroup;
  }

  public void setPriority(final int priority) {
    this.priority = priority;
  }

}