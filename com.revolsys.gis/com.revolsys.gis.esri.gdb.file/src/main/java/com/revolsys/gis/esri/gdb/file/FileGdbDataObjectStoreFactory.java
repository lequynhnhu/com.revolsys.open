package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.esri.gdb.file.capi.CapiFileGdbDataObjectStore;

public class FileGdbDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList("file:///.*.gdb");

  public static FileGdbDataObjectStore create(final File file) {
    FileGdbDataObjectStore dataObjectStore;
    try {
      Class.forName("com.esri.arcgis.geodatabase.IWorkspace");
      dataObjectStore = new com.revolsys.gis.esri.gdb.file.arcobjects.ArcObjectsFileGdbDataObjectStore(
        file);
    } catch (final ClassNotFoundException e) {
      dataObjectStore = new CapiFileGdbDataObjectStore(file);
    }
    return dataObjectStore;
  }

  public FileGdbDataObjectStore createDataObjectStore(
    final Map<String, Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>(
      connectionProperties);
    final String url = (String)properties.remove("url");
    try {
      final URI uri = new URI(url);
      final File file = new File(uri);

      final FileGdbDataObjectStore dataObjectStore = create(file);
      DataObjectStoreFactoryRegistry.setConnectionProperties(dataObjectStore,
        properties);
      return dataObjectStore;
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Url is not valid " + url, e);
    }
  }

  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, Object> connectionProperties) {
    return DataObjectStore.class;
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}