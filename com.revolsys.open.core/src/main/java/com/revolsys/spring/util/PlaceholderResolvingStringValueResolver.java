package com.revolsys.spring.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * BeanDefinitionVisitor that resolves placeholders in String values, delegating
 * to the <code>parseStringValue</code> method of the containing class.
 */
public class PlaceholderResolvingStringValueResolver implements
  StringValueResolver {
  private static final Logger LOG = LoggerFactory.getLogger(PlaceholderResolvingStringValueResolver.class);

  private Map<String, Object> attributes;

  private boolean ignoreUnresolvablePlaceholders;

  private String nullValue;

  private String placeholderPrefix;

  private String placeholderSuffix;

  public PlaceholderResolvingStringValueResolver(String placeholderPrefix,
    String placeholderSuffix, boolean ignoreUnresolvablePlaceholders,
    String nullValue, Map<String, Object> attributes) {
    super();
    this.placeholderPrefix = placeholderPrefix;
    this.placeholderSuffix = placeholderSuffix;
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    this.nullValue = nullValue;
    this.attributes = attributes;
  }

  private int findPlaceholderEndIndex(final CharSequence buf,
    final int startIndex) {
    int index = startIndex + this.placeholderPrefix.length();
    int withinNestedPlaceholder = 0;
    while (index < buf.length()) {
      if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + this.placeholderSuffix.length();
        } else {
          return index;
        }
      } else if (StringUtils.substringMatch(buf, index, this.placeholderPrefix)) {
        withinNestedPlaceholder++;
        index = index + this.placeholderPrefix.length();
      } else {
        index++;
      }
    }
    return -1;
  }

  /**
   * Parse the given String value recursively, to be able to resolve nested
   * placeholders (when resolved property values in turn contain placeholders
   * again).
   * 
   * @param strVal the String value to parse
   * @param props the Properties to resolve placeholders against
   * @param visitedPlaceholders the placeholders that have already been visited
   *          during the current resolution attempt (used to detect circular
   *          references between placeholders). Only non-null if we're parsing a
   *          nested placeholder.
   * @throws BeanDefinitionStoreException if invalid values are encountered
   * @see #resolvePlaceholder(String, java.util.Properties, int)
   */
  protected String parseStringValue(final String strVal,
    final Map<String, Object> attributes, final Set<String> visitedPlaceholders)
    throws BeanDefinitionStoreException {

    StringBuffer buf = new StringBuffer(strVal);

    int startIndex = strVal.indexOf(placeholderPrefix);
    while (startIndex != -1) {
      int endIndex = findPlaceholderEndIndex(buf, startIndex);
      if (endIndex != -1) {
        String placeholder = buf.substring(startIndex
          + placeholderPrefix.length(), endIndex);
        if (!visitedPlaceholders.add(placeholder)) {
          throw new BeanDefinitionStoreException(
            "Circular placeholder reference '" + placeholder
              + "' in property definitions");
        }
        // Recursive invocation, parsing placeholders contained in the
        // placeholder key.
        placeholder = parseStringValue(placeholder, attributes,
          visitedPlaceholders);
        // Now obtain the value for the fully resolved key...
        Object propValue = attributes.get(placeholder);
        if (propValue != null) {
          String propVal = propValue.toString();
          // Recursive invocation, parsing placeholders contained in the
          // previously resolved placeholder value.
          propVal = parseStringValue(propVal, attributes, visitedPlaceholders);
          buf.replace(startIndex, endIndex + this.placeholderSuffix.length(),
            propVal);
          if (LOG.isTraceEnabled()) {
            LOG.trace("Resolved placeholder '" + placeholder + "'");
          }
          startIndex = buf.indexOf(this.placeholderPrefix, startIndex
            + propVal.length());
        } else if (this.ignoreUnresolvablePlaceholders) {
          // Proceed with unprocessed value.
          startIndex = buf.indexOf(this.placeholderPrefix, endIndex
            + this.placeholderSuffix.length());
        } else {
          throw new BeanDefinitionStoreException(
            "Could not resolve placeholder '" + placeholder + "'");
        }
        visitedPlaceholders.remove(placeholder);
      } else {
        startIndex = -1;
      }
    }

    return buf.toString();
  }

  public String resolveStringValue(String strVal) throws BeansException {
     String value = parseStringValue(strVal, this.attributes,
      new HashSet<String>());
    return (value.equals(nullValue) ? null : value);
  }
}