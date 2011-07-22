package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStoreFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;

public class EsriFileGeodatabaseDataObjectStoreFactory implements DataObjectStoreFactory{

  private static final List<String> URL_PATTERNS = Arrays.asList("file:///.*.gdb");

  public EsriFileGeodatabaseDataObjectStore createDataObjectStore(
    Map<String, Object> connectionProperties) {
    Map<String, Object> properties = new LinkedHashMap<String, Object>(connectionProperties);
    String url = (String)properties.remove("url");
    try {
      URI uri = new URI(url);
      File file = new File(uri);
      
      EsriFileGeodatabaseDataObjectStore dataObjectStore = new EsriFileGeodatabaseDataObjectStore(file);
      DataObjectStoreFactoryRegistry.setConnectionProperties(dataObjectStore, properties);
      return dataObjectStore;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Url is not valid " + url,e);
    }
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}