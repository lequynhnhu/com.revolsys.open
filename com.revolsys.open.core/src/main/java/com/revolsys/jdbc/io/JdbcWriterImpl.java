package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.GlobalIdProperty;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.AbstractWriter;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.transaction.Transaction;

public class JdbcWriterImpl extends AbstractWriter<Record> implements
  JdbcWriter {
  private static final Logger LOG = Logger.getLogger(JdbcWriterImpl.class);

  private int batchSize = 1;

  private Connection connection;

  private DataSource dataSource;

  private JdbcRecordStore recordStore;

  private boolean flushBetweenTypes = false;

  private String hints = null;

  private String label;

  private RecordDefinition lastMetaData;

  private boolean quoteColumnNames = true;

  private String sqlPrefix;

  private String sqlSuffix;

  private final Map<String, Integer> typeCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, Integer> typeDeleteBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeDeleteSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeDeleteStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, Integer> typeInsertBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, Integer> typeInsertSequenceBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeInsertSequenceSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeInsertSequenceStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, String> typeInsertSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeInsertStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, Integer> typeUpdateBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeUpdateSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeUpdateStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private StatisticsMap statistics;

  private boolean throwExceptions = false;

  public JdbcWriterImpl(final JdbcRecordStore recordStore) {
    this(recordStore, recordStore.getStatistics());
  }

  public JdbcWriterImpl(final JdbcRecordStore recordStore,
    final StatisticsMap statistics) {
    this.recordStore = recordStore;
    this.statistics = statistics;
    setConnection(recordStore.getConnection());
    setDataSource(recordStore.getDataSource());
    statistics.connect();
  }

  private void addSqlColumEqualsPlaceholder(final StringBuffer sqlBuffer,
    final JdbcAttribute attribute) {
    final String attributeName = attribute.getName();
    if (quoteColumnNames) {
      sqlBuffer.append('"').append(attributeName).append('"');
    } else {
      sqlBuffer.append(attributeName);
    }
    sqlBuffer.append(" = ");
    attribute.addInsertStatementPlaceHolder(sqlBuffer, false);
  }

  @Override
  @PreDestroy
  public void close() {
    flush();
    doClose();
  }

  private void close(final Map<String, String> sqlMap,
    final Map<String, PreparedStatement> statementMap,
    final Map<String, Integer> batchCountMap) {
    for (final Entry<String, PreparedStatement> entry : statementMap.entrySet()) {
      final String typePath = entry.getKey();
      final PreparedStatement statement = entry.getValue();
      final String sql = sqlMap.get(typePath);
      try {
        processCurrentBatch(typePath, sql, statement, batchCountMap);
      } catch (final DataAccessException e) {
        if (throwExceptions) {
          throw e;
        } else {
          LOG.error("Error commiting records", e);
        }
      }
      JdbcUtils.close(statement);
    }
  }

  public synchronized void commit() {
    flush();
    JdbcUtils.commit(connection);
  }

  private void delete(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final String typePath = objectType.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    PreparedStatement statement = typeDeleteStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getDeleteSql(recordDefinition);
      try {
        statement = connection.prepareStatement(sql);
        typeDeleteStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final JdbcAttribute idAttribute = (JdbcAttribute)recordDefinition.getIdAttribute();
    parameterIndex = idAttribute.setInsertPreparedStatementValue(statement,
      parameterIndex, object);
    statement.addBatch();
    Integer batchCount = typeDeleteBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      typeDeleteBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      typeDeleteBatchCountMap.put(typePath, batchCount);
    }
    recordStore.addStatistic("Delete", object);

    // TODO this locks code tables which prevents insert
    // if (batchCount >= batchSize) {
    // final String sql = getDeleteSql(recordDefinition);
    // processCurrentBatch(typePath, sql, statement, typeDeleteBatchCountMap,
    // getDeleteStatistics());
    // }
  }

  protected synchronized void doClose() {
    if (recordStore != null) {
      try {

        close(typeInsertSqlMap, typeInsertStatementMap, typeInsertBatchCountMap);
        close(typeInsertSequenceSqlMap, typeInsertSequenceStatementMap,
          typeInsertSequenceBatchCountMap);
        close(typeUpdateSqlMap, typeUpdateStatementMap, typeUpdateBatchCountMap);
        close(typeDeleteSqlMap, typeDeleteStatementMap, typeDeleteBatchCountMap);
        if (statistics != null) {
          statistics.disconnect();
          statistics = null;
        }
      } finally {
        typeInsertSqlMap = null;
        typeInsertStatementMap = null;
        typeInsertBatchCountMap = null;
        typeInsertSequenceSqlMap = null;
        typeInsertSequenceStatementMap = null;
        typeInsertSequenceBatchCountMap = null;
        typeUpdateBatchCountMap = null;
        typeUpdateSqlMap = null;
        typeUpdateStatementMap = null;
        typeDeleteBatchCountMap = null;
        typeDeleteSqlMap = null;
        typeDeleteStatementMap = null;
        recordStore = null;
        if (dataSource != null) {
          try {
            if (!Transaction.isHasCurrentTransaction()) {
              connection.commit();
            }
          } catch (final SQLException e) {
            throw new RuntimeException("Failed to commit data:", e);
          } finally {
            JdbcUtils.release(connection, dataSource);
            dataSource = null;
            connection = null;
          }
        }
      }
    }
  }

  @Override
  public synchronized void flush() {
    flush(typeInsertSqlMap, typeInsertStatementMap, typeInsertBatchCountMap);
    flush(typeInsertSequenceSqlMap, typeInsertSequenceStatementMap,
      typeInsertSequenceBatchCountMap);
    flush(typeUpdateSqlMap, typeUpdateStatementMap, typeUpdateBatchCountMap);
    flush(typeDeleteSqlMap, typeDeleteStatementMap, typeDeleteBatchCountMap);
  }

  private void flush(final Map<String, String> sqlMap,
    final Map<String, PreparedStatement> statementMap,
    final Map<String, Integer> batchCountMap) {
    if (statementMap != null) {
      for (final Entry<String, PreparedStatement> entry : statementMap.entrySet()) {
        final String typePath = entry.getKey();
        final PreparedStatement statement = entry.getValue();
        final String sql = sqlMap.get(typePath);
        try {
          processCurrentBatch(typePath, sql, statement, batchCountMap);
        } catch (final DataAccessException e) {
          if (throwExceptions) {
            throw e;
          } else {
            LOG.error("Error writing to database", e);
          }
        }
      }
    }
  }

  private void flushIfRequired(final RecordDefinition recordDefinition) {
    if (flushBetweenTypes && recordDefinition != lastMetaData) {
      flush();
      lastMetaData = recordDefinition;
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

  private RecordDefinition getRecordDefinition(final String typePath) {
    if (recordStore == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = recordStore.getRecordDefinition(typePath);
      return recordDefinition;
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private String getDeleteSql(final RecordDefinition type) {
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = typeDeleteSqlMap.get(typePath);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("delete ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(" from ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" where ");
      final JdbcAttribute idAttribute = (JdbcAttribute)type.getIdAttribute();
      if (idAttribute == null) {
        throw new RuntimeException("No primary key found for " + type);
      }
      addSqlColumEqualsPlaceholder(sqlBuffer, idAttribute);

      sqlBuffer.append(" ");
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();

      typeDeleteSqlMap.put(typePath, sql);
    }
    return sql;
  }

  private String getGeneratePrimaryKeySql(final RecordDefinition recordDefinition) {
    return recordStore.getGeneratePrimaryKeySql(recordDefinition);
  }

  /**
   * @return the hints
   */
  public String getHints() {
    return hints;
  }

  private String getInsertSql(final RecordDefinition type,
    final boolean generatePrimaryKey) {
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql;
    if (generatePrimaryKey) {
      sql = typeInsertSequenceSqlMap.get(typePath);
    } else {
      sql = typeInsertSqlMap.get(typePath);
    }
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("insert ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      if (generatePrimaryKey) {
        final String idAttributeName = type.getIdAttributeName();
        if (quoteColumnNames) {
          sqlBuffer.append('"').append(idAttributeName).append('"');
        } else {
          sqlBuffer.append(idAttributeName);
        }
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdAttributeIndex()) {
          final String attributeName = type.getAttributeName(i);
          if (quoteColumnNames) {
            sqlBuffer.append('"').append(attributeName).append('"');
          } else {
            sqlBuffer.append(attributeName);
          }
          if (i < type.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(") VALUES (");
      if (generatePrimaryKey) {
        sqlBuffer.append(getGeneratePrimaryKeySql(type));
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdAttributeIndex()) {
          final JdbcAttribute attribute = (JdbcAttribute)type.getAttribute(i);
          attribute.addInsertStatementPlaceHolder(sqlBuffer, generatePrimaryKey);
          if (i < type.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(")");
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();
      if (generatePrimaryKey) {
        typeInsertSequenceSqlMap.put(typePath, sql);
      } else {
        typeInsertSqlMap.put(typePath, sql);
      }
    }
    return sql;
  }

  public String getLabel() {
    return label;
  }

  public String getSqlPrefix() {
    return sqlPrefix;
  }

  public String getSqlSuffix() {
    return sqlSuffix;
  }

  private String getUpdateSql(final RecordDefinition type) {
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = typeUpdateSqlMap.get(typePath);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("update ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(tableName);
      sqlBuffer.append(" set ");
      final List<Attribute> idAttributes = type.getIdAttributes();
      boolean first = true;
      for (final Attribute attribute : type.getAttributes()) {
        if (!idAttributes.contains(attribute)) {
          final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(", ");
          }
          addSqlColumEqualsPlaceholder(sqlBuffer, jdbcAttribute);
        }
      }
      sqlBuffer.append(" where ");
      first = true;
      for (final Attribute idAttribute : idAttributes) {
        if (first) {
          first = false;
        } else {
          sqlBuffer.append(" AND ");
        }
        final JdbcAttribute idJdbcAttribute = (JdbcAttribute)idAttribute;
        addSqlColumEqualsPlaceholder(sqlBuffer, idJdbcAttribute);
      }

      sqlBuffer.append(" ");
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();

      typeUpdateSqlMap.put(typePath, sql);
    }
    return sql;
  }

  private void insert(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final String typePath = objectType.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    final String idAttributeName = recordDefinition.getIdAttributeName();
    final boolean hasId = idAttributeName != null;

    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(object);
    if (globalIdProperty != null) {
      if (object.getValue(globalIdProperty.getAttributeName()) == null) {
        object.setValue(globalIdProperty.getAttributeName(), UUID.randomUUID()
          .toString());
      }
    }

    final boolean hasIdValue = hasId
      && object.getValue(idAttributeName) != null;

    if (!hasId || hasIdValue) {
      insert(object, typePath, recordDefinition);
    } else {
      insertSequence(object, typePath, recordDefinition);
    }
    object.setState(RecordState.Persisted);
    recordStore.addStatistic("Insert", object);
  }

  private void insert(final Record object, final String typePath,
    final RecordDefinition recordDefinition) throws SQLException {
    PreparedStatement statement = typeInsertStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getInsertSql(recordDefinition, false);
      try {
        statement = connection.prepareStatement(sql);
        typeInsertStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement,
        parameterIndex, object);
    }
    statement.addBatch();
    Integer batchCount = typeInsertBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      typeInsertBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      typeInsertBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getInsertSql(recordDefinition, false);
      processCurrentBatch(typePath, sql, statement, typeInsertBatchCountMap);
    }
  }

  private void insertSequence(final Record object, final String typePath,
    final RecordDefinition recordDefinition) throws SQLException {
    PreparedStatement statement = typeInsertSequenceStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getInsertSql(recordDefinition, true);
      try {
        statement = connection.prepareStatement(sql);
        typeInsertSequenceStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final Attribute idAttribute = recordDefinition.getIdAttribute();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      if (attribute != idAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
      }
    }
    statement.addBatch();
    Integer batchCount = typeInsertSequenceBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      typeInsertSequenceBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      typeInsertSequenceBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getInsertSql(recordDefinition, true);
      processCurrentBatch(typePath, sql, statement,
        typeInsertSequenceBatchCountMap);
    }
  }

  public boolean isFlushBetweenTypes() {
    return flushBetweenTypes;
  }

  public boolean isQuoteColumnNames() {
    return quoteColumnNames;
  }

  public boolean isThrowExceptions() {
    return throwExceptions;
  }

  private void processCurrentBatch(final String typePath, final String sql,
    final PreparedStatement statement, final Map<String, Integer> batchCountMap) {
    Integer batchCount = batchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 0;
    }
    try {
      Integer typeCount = typeCountMap.get(typePath);
      if (typeCount == null) {
        typeCount = batchCount;
      } else {
        typeCount += batchCount;
      }
      typeCountMap.put(typePath, typeCount);
      statement.executeBatch();
    } catch (final SQLException e) {
      throw JdbcUtils.getException(getDataSource(), connection,
        "Process Batch", sql, e);
    } catch (final RuntimeException e) {
      LOG.error(sql, e);
      throw e;
    } finally {
      batchCountMap.put(typePath, 0);
    }
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
    try {
      setConnection(JdbcUtils.getConnection(dataSource));
      connection.setAutoCommit(false);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to create connection", e);
    }
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  /**
   * @param hints the hints to set
   */
  public void setHints(final String hints) {
    this.hints = hints;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setQuoteColumnNames(final boolean quoteColumnNames) {
    this.quoteColumnNames = quoteColumnNames;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  public void setThrowExceptions(final boolean throwExceptions) {
    this.throwExceptions = throwExceptions;
  }

  @Override
  public String toString() {
    if (recordStore == null) {
      return super.toString();
    } else {
      return recordStore.toString() + " writer";
    }
  }

  private void update(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final String typePath = objectType.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    PreparedStatement statement = typeUpdateStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getUpdateSql(recordDefinition);
      try {
        statement = connection.prepareStatement(sql);
        typeUpdateStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final List<Attribute> idAttributes = recordDefinition.getIdAttributes();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      if (!idAttributes.contains(attribute)) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
      }
    }
    for (final Attribute idAttribute : idAttributes) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)idAttribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement,
        parameterIndex, object);

    }
    statement.addBatch();
    Integer batchCount = typeUpdateBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      typeUpdateBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      typeUpdateBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getUpdateSql(recordDefinition);
      processCurrentBatch(typePath, sql, statement, typeUpdateBatchCountMap);
    }
    recordStore.addStatistic("Update", object);
  }

  @Override
  public synchronized void write(final Record object) {
    try {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final RecordStore recordStore = recordDefinition.getDataStore();
      final RecordState state = object.getState();
      if (recordStore != this.recordStore) {
        if (state != RecordState.Deleted) {
          insert(object);
        }
      } else {
        switch (state) {
          case New:
            insert(object);
          break;
          case Modified:
            update(object);
          break;
          case Persisted:
          // No action required
          break;
          case Deleted:
            delete(object);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final BatchUpdateException e) {
      for (SQLException e1 = e.getNextException(); e1 != null; e1 = e1.getNextException()) {
        LOG.error("Unable to write", e1);
      }
      throw new RuntimeException("Unable to write", e);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}