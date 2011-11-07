package com.revolsys.ui.html.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;
import com.revolsys.xml.io.XmlWriter;

/**
 * Serialize a value using a method on the {@link HtmlUiBuilder}.
 * 
 * @author Paul Austin
 */
public class BuilderMethodSerializer implements TypeSerializer, KeySerializer {
  /** The HTML UI Builder */
  private HtmlUiBuilder builder;

  /** The method on the builder */
  private Method method;

  /**
   * Construt a new HtmlUiBuilderMethodSerializer.
   * 
   * @param builder The HTML UI Builder the method is on.
   * @param method The serializer method.
   */
  public BuilderMethodSerializer(
    final HtmlUiBuilder builder,
    final Method method) {
    this.builder = builder;
    this.method = method;
  }

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(
    final XmlWriter out,
    final Object value,
    final Locale locale) {
    try {
      method.invoke(builder, new Object[] {
        out, value
      });
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new RuntimeException(cause.getMessage(), cause);
      }
    }
  }

  public void serialize(
    XmlWriter out,
    Object value,
    String key,
    Locale locale) {
    serialize(out, value, locale);
  }

}