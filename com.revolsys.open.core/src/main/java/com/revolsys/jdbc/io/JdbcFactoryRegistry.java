package com.revolsys.jdbc.io;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcFactoryRegistry {

  private static JdbcFactoryRegistry instance;

  private static final Logger LOG = LoggerFactory.getLogger(JdbcFactoryRegistry.class);

  private static Map<ApplicationContext, JdbcFactoryRegistry> factoriesByApplicationContext = new WeakHashMap<ApplicationContext, JdbcFactoryRegistry>();

  public static JdbcDatabaseFactory databaseFactory(final DataSource dataSource) {
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory();
    return jdbcFactoryRegistry.getDatabaseFactory(dataSource);
  }

  public static JdbcDatabaseFactory databaseFactory(
    final Map<String, Object> config) {
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory();
    return jdbcFactoryRegistry.getDatabaseFactory(config);
  }

  public static JdbcDatabaseFactory databaseFactory(final String productName) {
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory();
    return jdbcFactoryRegistry.getDatabaseFactory(productName);
  }

  public synchronized static JdbcFactoryRegistry getFactory() {
    if (instance == null) {
      instance = new JdbcFactoryRegistry();
      try {
        ClassLoader classLoader = JdbcFactoryRegistry.class.getClassLoader();
        final Enumeration<URL> resources = classLoader.getResources("META-INF/com.revolsys.gis.jdbc.json");
        while (resources.hasMoreElements()) {
          final URL resourceUrl = resources.nextElement();
          final UrlResource resource = new UrlResource(resourceUrl);
          instance.loadFactories(classLoader, resource);
        }
      } catch (final Throwable e) {
        LOG.error("Unable to initialized JdbcFactoryRegistry for", e);
      }

    }
    return instance;
  }

  public static JdbcFactoryRegistry getFactory(
    final ApplicationContext applicationContext) {
    synchronized (factoriesByApplicationContext) {
      JdbcFactoryRegistry jdbcFactoryRegistry = factoriesByApplicationContext.get(applicationContext);
      if (jdbcFactoryRegistry == null) {
        jdbcFactoryRegistry = new JdbcFactoryRegistry();
        try {
          ClassLoader classLoader = applicationContext.getClassLoader();
          for (final Resource resource : applicationContext.getResources("classpath*:META-INF/com.revolsys.gis.jdbc.json")) {
            jdbcFactoryRegistry.loadFactories(classLoader, resource);
          }
        } catch (final IOException e) {
          LOG.error("Unable to load JdbcFactoryRegistry json files", e);
        }
        factoriesByApplicationContext.put(applicationContext,
          jdbcFactoryRegistry);
      }
      return jdbcFactoryRegistry;
    }
  }

  private Map<String, JdbcDatabaseFactory> databaseFactoriesByProductName = new HashMap<String, JdbcDatabaseFactory>();

  private JdbcFactoryRegistry() {
  }

  @Override
  protected void finalize() throws Throwable {
    databaseFactoriesByProductName = null;
  }

  public JdbcDatabaseFactory getDatabaseFactory(final DataSource dataSource) {
    final String productName = JdbcUtils.getProductName(dataSource);
    return getDatabaseFactory(productName);
  }

  public JdbcDatabaseFactory getDatabaseFactory(final Map<String, Object> config) {
    final String url = (String)config.get("url");
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final JdbcDatabaseFactory databaseFactory : databaseFactoriesByProductName.values()) {
        if (databaseFactory.canHandleUrl(url)) {
          return databaseFactory;
        }
      }
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    }
  }

  public JdbcDatabaseFactory getDatabaseFactory(final String productName) {
    final JdbcDatabaseFactory databaseFactory = databaseFactoriesByProductName.get(productName);
    if (databaseFactory == null) {
      throw new IllegalArgumentException("Data Store not found for "
        + productName);
    } else {
      return databaseFactory;
    }
  }

  private void loadFactories(
    final ClassLoader classLoader,
    final Resource resource) {
    try {
      for (final Map<String, Object> factoryDefinition : AbstractMapReaderFactory.mapReader(resource)) {
        final String jdbcFactoryClassName = (String)factoryDefinition.get("jdbcFactoryClassName");
        if (StringUtils.hasText(jdbcFactoryClassName)) {
          @SuppressWarnings("unchecked")
          final Class<JdbcDatabaseFactory> factoryClass = (Class<JdbcDatabaseFactory>)Class.forName(jdbcFactoryClassName, true, classLoader);
          final JdbcDatabaseFactory factory = factoryClass.newInstance();
          for (final String productName : factory.getProductNames()) {
            databaseFactoriesByProductName.put(productName, factory);
          }
        }
      }
    } catch (final Throwable e) {
      LOG.error("Unable to initialize JdbcDatabaseFactory from " + resource, e);
    }

  }
}