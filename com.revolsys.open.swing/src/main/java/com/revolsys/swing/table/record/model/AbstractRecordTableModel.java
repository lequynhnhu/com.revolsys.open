package com.revolsys.swing.table.record.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordTableModel extends com.revolsys.swing.table.AbstractTableModel
  implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private RecordDefinition recordDefinition;

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private boolean editable;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public AbstractRecordTableModel() {
    this(null);
  }

  public AbstractRecordTableModel(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    Property.addListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void addReadOnlyFieldNames(final Collection<String> fieldNames) {
    if (fieldNames != null) {
      this.readOnlyFieldNames.addAll(fieldNames);
    }
  }

  public void addReadOnlyFieldNames(final String... readOnlyFieldNames) {
    if (readOnlyFieldNames != null) {
      final List<String> fieldNames = Arrays.asList(readOnlyFieldNames);
      addReadOnlyFieldNames(fieldNames);
    }
  }

  @PreDestroy
  public void dispose() {
    this.recordDefinition = null;
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  protected void firePropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
  }

  protected void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public String getFieldName(final int attributeIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldName(attributeIndex);
  }

  public abstract String getFieldName(int rowIndex, int columnIndex);

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isReadOnly(final String fieldName) {
    return this.readOnlyFieldNames.contains(fieldName);
  }

  public abstract boolean isSelected(boolean selected, int rowIndex, int columnIndex);

  public void loadCodeTable(final CodeTable codeTable) {
    if (!codeTable.isLoaded()) {
      codeTable.getCodes();
      fireTableDataChanged();
    }
  }

  public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    Property.removeListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    if (readOnlyFieldNames == null) {
      this.readOnlyFieldNames = new HashSet<String>();
    } else {
      this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
    }
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (recordDefinition != this.recordDefinition) {
      this.recordDefinition = recordDefinition;
      fireTableStructureChanged();
    }
  }

  public String toDisplayValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    String text;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String idFieldName = recordDefinition.getIdFieldName();
    final String name = getFieldName(attributeIndex);
    if (objectValue == null || name == null) {
      if (name.equals(idFieldName)) {
        return "NEW";
      } else {
        text = "-";
      }
    } else {
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.getGeometryType();
      }
      CodeTable codeTable = null;
      if (!name.equals(idFieldName)) {
        codeTable = recordDefinition.getCodeTableByColumn(name);
      }
      if (codeTable == null) {
        text = StringConverterRegistry.toString(objectValue);
      } else {
        if (codeTable.isLoaded()) {
          final List<Object> values = codeTable.getValues(SingleIdentifier.create(objectValue));
          if (values == null || values.isEmpty()) {
            text = StringConverterRegistry.toString(objectValue);
          } else {
            text = CollectionUtil.toString(values);
          }
        } else {
          if (!codeTable.isLoading()) {
            Invoke.background("Load " + codeTable, this, "loadCodeTable", codeTable);
          }
          text = "...";
        }
      }
      if (text.length() == 0) {
        text = "-";
      }
    }
    return text;
  }

  public Object toObjectValue(final String fieldName, final Object displayValue) {
    if (!Property.hasValue(displayValue)) {
      return null;
    }
    final RecordDefinition recordDefinition = getRecordDefinition();
    final CodeTable codeTable = recordDefinition.getCodeTableByColumn(fieldName);
    if (codeTable == null) {
      final Class<?> fieldClass = recordDefinition.getFieldClass(fieldName);
      final Object objectValue = StringConverterRegistry.toObject(fieldClass, displayValue);
      return objectValue;
    } else {
      if (displayValue instanceof Identifier) {
        final Identifier identifier = (Identifier)displayValue;
        return identifier;
      } else {
        final Object objectValue = codeTable.getId(displayValue);
        return objectValue;
      }
    }
  }

}
