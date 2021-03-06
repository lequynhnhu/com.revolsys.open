package com.revolsys.jdbc.attribute;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcDateFieldDefinition extends JdbcFieldDefinition {
  public JdbcDateFieldDefinition(final String dbName, final String name,
    final int sqlType, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.DATE, sqlType, 0, 0, required, description,
      properties);
  }

  @Override
  public JdbcDateFieldDefinition clone() {
    return new JdbcDateFieldDefinition(getDbName(), getName(), getSqlType(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final Date value = resultSet.getDate(columnIndex);
    setValue(object, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof Date) {
        final Date date = (Date)value;
        statement.setDate(parameterIndex, date);
      } else if (value instanceof java.util.Date) {
        final java.util.Date date = (java.util.Date)value;
        statement.setDate(parameterIndex, new Date(date.getTime()));
      } else {
        final Date date = Date.valueOf(value.toString());
        statement.setDate(parameterIndex, date);
      }
    }
    return parameterIndex + 1;
  }
}
