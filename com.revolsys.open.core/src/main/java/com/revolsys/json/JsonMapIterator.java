package com.revolsys.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.json.JsonParser.EventType;

public class JsonMapIterator implements Iterator<Map<String, Object>> {

  /** The current record. */
  private Map<String, Object> currentRecord;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private final JsonParser parser;

  public JsonMapIterator(
    final Reader in)
    throws IOException {
    this.parser = new JsonParser(in);

    if (parser.hasNext()) {
      EventType event = parser.next();
      if (event == EventType.startDocument) {
        if (parser.hasNext()) {
          event = parser.next();
          if (event == EventType.startObject) {
            final String key = JsonParserUtil.getString(parser);
            if (parser.hasNext()) {
              event = parser.next();
              if (event == EventType.colon) {
                if (parser.hasNext()) {
                  event = parser.next();
                  if (event == EventType.startArray) {
                    hasNext = true;
                    readNextRecord();
                  }
                }
              }
            }
          }
        }
      }
    }
    if (!hasNext) {
      close();
    }
  }

  public void close() {
    parser.close();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   * 
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {
    return hasNext;
  }

  /**
   * Return the next record from the iterator.
   * 
   * @return The record
   */
  public Map<String, Object> next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final Map<String, Object> object = currentRecord;
      readNextRecord();
      return object;
    }
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private Map<String, Object> readNextRecord() {

    if (hasNext && parser.hasNext()) {
      final EventType event = parser.next();
      if (event == EventType.endArray) {
        hasNext = false;
        close();
        return null;
      } else {
        currentRecord = JsonParserUtil.getMap(parser);
        return currentRecord;
      }
    } else {
      hasNext = false;
      close();
      return null;
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}