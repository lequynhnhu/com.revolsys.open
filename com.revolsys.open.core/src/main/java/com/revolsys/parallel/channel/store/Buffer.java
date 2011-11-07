package com.revolsys.parallel.channel.store;

import java.util.LinkedList;
import java.util.Queue;

import com.revolsys.parallel.channel.ChannelDataStore;

/**
 * <h2>Description</h2>
 * <p>
 * The Buffer class is an implementation of ChannelDataStore which allows more
 * than one Object to be sent across the Channel at any one time. The Buffer
 * will store the Objects and allow them to be read in the same order as they
 * were written.
 * </p>
 * <p>
 * The getState method will return EMPTY if the Channel does not contain any
 * Objects, FULL if it cannot accept more data and NONEMPTYFULL otherwise.
 * </p>
 * 
 * @author P.D.Austin
 */
public class Buffer<T> extends ChannelDataStore<T> {
  /** The storage for the buffered Objects */
  private Queue<T> buffer = new LinkedList<T>();

  /** The max size of the Buffer */
  private int maxSize;

  private boolean discardIfFull;

  /**
   * Construct a new Buffer with no maximum size.
   */
  public Buffer() {
    this(0);
  }

  /**
   * Construct a new Buffer with the specified maximum size.
   * 
   * @param maxSize The maximum number of Objects the Buffer can store
   */
  public Buffer(int maxSize) {
    this(new LinkedList<T>(), maxSize);
  }

  /**
   * Construct a new Buffer with the specified maximum size.
   * 
   * @param buffer The buffer to store the elements in.
   * @param size The maximum number of Objects the Buffer can store
   */
  public Buffer(Queue<T> buffer, int maxSize) {
    this.buffer = buffer;
    this.maxSize = maxSize;
  }

  /**
   * Construct a new Buffer with the no maximum size.
   * 
   * @param buffer The buffer to store the elements in.
   */
  public Buffer(Queue<T> buffer) {
    this(buffer, 0);
  }

  /**
   * Returns the first Object from the Buffer and removes the Object from the
   * Buffer.
   * <P>
   * <I>NOTE: getState should be called before this method to check that the
   * state is not EMPTY. If the state is EMPTY the Buffer will be left in an
   * undefined state.</I>
   * <P>
   * Pre-condition: The state must not be EMPTY
   * 
   * @return The next available Object from the Buffer
   */
  protected synchronized T get() {
    return buffer.remove();
  }

  /**
   * Puts a new Object into the Buffer.
   * <P>
   * <I>NOTE: getState should be called before this method to check that the
   * state is not FULL. If the state is FULL the Buffer will be left in an
   * undefined state.</I>
   * <P>
   * Pre-condition: The state must not be FULL
   * 
   * @param value The object to put in the Buffer
   */
  protected synchronized void put(T value) {
    if (maxSize == 0 || buffer.size() < maxSize) {
      buffer.offer(value);
    }
  }

  /**
   * Returns the current state of the Buffer, should be called to ensure the
   * Pre-conditions of the other methods are not broken.
   * 
   * @return The current state of the Buffer (EMPTY, NONEMPTYFULL or FULL)
   */
  protected synchronized int getState() {
    if (buffer.isEmpty()) {
      return EMPTY;
    } else if (!discardIfFull && maxSize > 0 && buffer.size() == maxSize) {
      return FULL;
    } else {
      return NONEMPTYFULL;
    }
  }

  /**
   * The number of items in the buffer.
   * 
   * @return The number of items in the buffer.
   */
  public int size() {
    return buffer.size();
  }

  /**
   * Empty the buffer.
   */
  public synchronized void clear() {
    buffer.clear();
  }

  /**
   * Returns a new Object with the same creation parameters as this Object. This
   * method should be overridden by subclasses to return a new Object that is
   * the same type as this Object. The new instance should be created by
   * constructing a new instance with the same parameters as the original.
   * <I>NOTE: Only the sizes of the data should be cloned not the stored
   * data.</I>
   * 
   * @return The cloned instance of this Object.
   */
  protected Object clone() {
    return new Buffer<T>(maxSize);
  }

  /**
   * Remove the object from the buffer.
   * 
   * @param object The object to remove.
   * @return True if the object was removed.
   */
  public boolean remove(T object) {
    if (buffer.contains(object)) {
      buffer.remove(object);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return buffer.toString();
  }
}