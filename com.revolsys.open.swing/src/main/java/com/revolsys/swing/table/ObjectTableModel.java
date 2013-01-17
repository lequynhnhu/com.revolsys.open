package com.revolsys.swing.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.revolsys.util.JavaBeanUtil;

public class ObjectTableModel<T> extends AbstractTableModel {
  /**
   * 
   */
  private static final long serialVersionUID = -1243907349293763360L;

  private final String[] propertyNames;

  private final String[] lables;

  private final List<T> rows = new ArrayList<T>();

  public ObjectTableModel(final String[] propertyNames, final String[] lables) {
    this.propertyNames = propertyNames;
    this.lables = lables;
  }

  public void addRow(final T row) {
    insertRow(getRowCount(), row);
  }

  @Override
  public int getColumnCount() {
    return propertyNames.length;
  }

  @Override
  public String getColumnName(final int column) {
    return lables[column];
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  public List<T> getRows() {
    return rows;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Object row = rows.get(rowIndex);
    final String propertyName = propertyNames[columnIndex];
    return JavaBeanUtil.getProperty(row, propertyName);
  }

  public void insertRow(final int rowIndex, final T row) {
    rows.add(rowIndex, row);
    fireTableRowsInserted(rowIndex, rowIndex);
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return true;
  }

  public void removeRow(final int rowIndex) {
    rows.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  public void setRows(final List<T> rows) {
    this.rows.clear();
    this.rows.addAll(rows);
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Object row = rows.get(rowIndex);
    final String propertyName = propertyNames[columnIndex];
    JavaBeanUtil.setProperty(row, propertyName, value);
  }

}