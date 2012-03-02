package com.revolsys.jdbc.io;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class JdbcDataSourceFactoryBean implements FactoryBean<DataSource> {

  private Map<String, Object> config = new HashMap<String, Object>();

  private String url;

  private String username;

  private String password;

  private WeakReference<DataSource> dataSourceReference;

  private DataSourceFactory dataSourceFactory;

  @PreDestroy
  public void close() {
    if (dataSourceReference != null) {
      try {
        final DataSource dataSource = dataSourceReference.get();
        if (dataSource != null) {
          dataSourceFactory.closeDataSource(dataSource);
        }
        dataSourceReference.clear();
      } finally {
        config = null;
        dataSourceFactory = null;
        dataSourceReference = null;
        password = null;
        url = null;
        username = null;
      }
    }
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public DataSource getObject() throws Exception {
    final Map<String, Object> config = new HashMap<String, Object>(this.config);
    config.put("url", url);
    config.put("username", username);
    config.put("password", password);
    if (dataSourceReference == null) {
      dataSourceFactory = JdbcFactory.getDataSourceFactory(url);
      final DataSource dataSource = dataSourceFactory.createDataSource(config);
      dataSourceReference = new WeakReference<DataSource>(dataSource);
    }
    return dataSourceReference.get();
  }

  public Class<?> getObjectType() {
    return DataSource.class;
  }

  public String getPassword() {
    return password;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  @Required
  public void setPassword(final String password) {
    this.password = password;
  }

  @Required
  public void setUrl(final String url) {
    this.url = url;
  }

  @Required
  public void setUsername(final String username) {
    this.username = username;
  }
}
