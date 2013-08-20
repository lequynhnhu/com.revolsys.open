package com.revolsys.swing.table.worker;

import java.awt.BorderLayout;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.awt.SwingWorkerManager;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.table.BaseJxTable;

@SuppressWarnings("serial")
public class SwingWorkerTableModel extends AbstractTableModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static JPanel createPanel() {
    final JPanel taskPanel = new JPanel(new BorderLayout());
    final BaseJxTable table = SwingWorkerTableModel.createTable();
    final JScrollPane scrollPane = new JScrollPane(table);
    taskPanel.add(scrollPane, BorderLayout.CENTER);
    return taskPanel;
  }

  public static BaseJxTable createTable() {
    final SwingWorkerTableModel model = new SwingWorkerTableModel();
    final BaseJxTable table = new BaseJxTable(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setAutoCreateColumnsFromModel(false);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      if (i == 0) {
        column.setMinWidth(200);
        column.setPreferredWidth(400);
      } else if (i == 1) {
        column.setMinWidth(100);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);
      }
    }
    return table;
  }

  private final List<String> columnTitles = Arrays.asList("Description",
    "Status");

  private final InvokeMethodPropertyChangeListener listener;

  public SwingWorkerTableModel() {
    this.listener = new InvokeMethodPropertyChangeListener(this,
      "fireTableDataChanged");
    final PropertyChangeSupport propertyChangeSupport = SwingWorkerManager.getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(this.listener);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return String.class;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return this.columnTitles.get(columnIndex);
  }

  @Override
  public int getRowCount() {
    return SwingWorkerManager.getWorkerCount();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final SwingWorker<?, ?> worker = SwingWorkerManager.getWorker(rowIndex);
    if (worker == null) {
      return "-";
    } else {
      if (columnIndex == 1) {
        if (SwingWorkerManager.isWorkerRunning(worker)) {
          return "Running";
        } else {
          return "Waiting";
        }
      } else {
        return worker.toString();
      }
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
  }
}
