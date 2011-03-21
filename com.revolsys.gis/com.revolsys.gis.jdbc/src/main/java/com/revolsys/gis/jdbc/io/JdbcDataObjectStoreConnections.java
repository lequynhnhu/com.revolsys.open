package com.revolsys.gis.jdbc.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.util.CollectionUtil;

public class JdbcDataObjectStoreConnections {

  private static JdbcDataObjectStoreConnections INSTANCE = new JdbcDataObjectStoreConnections();

  private Map<String, JdbcDataObjectStore> dataStores = new HashMap<String, JdbcDataObjectStore>();

  public static JdbcDataObjectStoreConnections get() {
    return INSTANCE;
  }

  private final Preferences jdbcDataStoresPrefereneces;

  public JdbcDataObjectStoreConnections() {
    this(Preferences.userRoot(), "com/revolsys/jdbc/dataSource");
  }

  public JdbcDataObjectStoreConnections(final Preferences root,
    final String preferencesPath) {
    jdbcDataStoresPrefereneces = root.node("com/revolsys/jdbc/dataSource");
  }

  public JdbcDataObjectStoreConnections(final String preferencesPath) {
    this(Preferences.userRoot(), preferencesPath);
  }

  public List<String> getConnectionNames() {
    try {
      final String[] names = jdbcDataStoresPrefereneces.childrenNames();
      return Arrays.asList(names);
    } catch (final BackingStoreException e) {
      throw new RuntimeException(e);
    }
  }

  private DataObjectStore getDataObjectStore(final String connectionName) {
    JdbcDataObjectStore dataStore = dataStores.get(connectionName);
    if (dataStore == null) {
      final Preferences preferences = getPreferences(connectionName);
      Map<String, Object> config = CollectionUtil.toMap(preferences);
      config.remove("productName");
      try {
        if (config.get("url") != null) {
          dataStore = JdbcFactory.createDataObjectStore(config);
          dataStore.setLabel(connectionName);
          dataStores.put(connectionName, dataStore);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
    return dataStore;
  }

  public List<DataObjectStore> getDataObjectStores() {
    final List<DataObjectStore> dataObjectStores = new ArrayList<DataObjectStore>();
    final List<String> connectionNames = getConnectionNames();
    for (final String connectionName : connectionNames) {
      final DataObjectStore dataObjectStore = getDataObjectStore(connectionName);
      if (dataObjectStore != null) {
        dataObjectStores.add(dataObjectStore);
      }
    }
    return dataObjectStores;
  }

  private Preferences getPreferences(final String connectionName) {
    return jdbcDataStoresPrefereneces.node(connectionName);
  }

  @Override
  public String toString() {
    return "JDBC Connections";
  }

  public void createConnection(String connectionName, Map<String, String> config) {
    final Preferences preferences = getPreferences(connectionName);
    for (Entry<String, String> param : config.entrySet()) {
      preferences.put(param.getKey(), param.getValue());
    }
  }
}