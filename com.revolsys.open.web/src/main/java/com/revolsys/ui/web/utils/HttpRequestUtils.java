package com.revolsys.ui.web.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

public final class HttpRequestUtils {
  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  public static HttpServletRequest getHttpServletRequest() {
    final ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    final HttpServletRequest request = requestAttributes.getRequest();
    return request;
  }

  public static String getOriginatingRequestUri() {
    final HttpServletRequest request = getHttpServletRequest();
    final String originatingRequestUri = URL_PATH_HELPER.getOriginatingRequestUri(request);
    return originatingRequestUri;
  }

  public static String getRequestBaseFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = WebUtils.extractFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getRequestFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = WebUtils.extractFullFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getServerUrl() {
    return getServerUrl(getHttpServletRequest());
  }

  public static String getServerUrl(final HttpServletRequest request) {
    final String scheme = request.getScheme();
    final String serverName = request.getServerName();
    final int serverPort = request.getServerPort();
    final StringBuilder url = new StringBuilder();
    url.append(scheme);
    url.append("://");
    url.append(serverName);

    if ("http".equals(scheme)) {
      if (serverPort != 80 && serverPort !=-1) {
        url.append(":").append(serverPort);
      }
    } else if ("https".equals(scheme)) {
      if (serverPort != 443 && serverPort !=-1) {
        url.append(":").append(serverPort);
      }
    }
    return url.toString();

  }

  public static String getFullRequestUrl() {
    return getFullRequestUrl(getHttpServletRequest());
  }

  public static String getFullRequestUrl(final HttpServletRequest request) {
    String serverUrl = getServerUrl(request);
    String requestUri = getOriginatingRequestUri();
    return serverUrl + requestUri;
  }

  private HttpRequestUtils() {

  }
}