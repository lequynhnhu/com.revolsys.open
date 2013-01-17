package com.revolsys.swing.table.dataobject.row;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;

@SuppressWarnings("serial")
public class DataObjectRowTableCellEditor extends AbstractCellEditor implements
  TableCellEditor {
  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  private final JTextField editorComponent = new JTextField();

  private ValueUiBuilder uiBuilder;

  public DataObjectRowTableCellEditor() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectRowTableCellEditor(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

  @Override
  public Object getCellEditorValue() {
    if (uiBuilder != null) {
      return uiBuilder.getCellEditorValue();
    } else {
      return editorComponent.getText();
    }
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    final DataObjectListTableModel model = (DataObjectListTableModel)table.getModel();
    final DataObjectMetaData metaData = model.getMetaData();
    uiBuilder = uiBuilderRegistry.getValueUiBuilder(metaData, column);
    if (uiBuilder != null) {
      return uiBuilder.getEditorComponent(value);
    } else {
      String fieldName = metaData.getAttributeName(column);
      return SwingUtil.createField(metaData, fieldName, true);
    }
  }
}