package com.revolsys.gis.converter.process;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.schema.RecordDefinition;

public class MapValues extends AbstractSourceToTargetProcess<Record, Record> {
  private String sourceFieldName;

  private String targetFieldName;

  private Map<Object, Object> valueMap = new LinkedHashMap<Object, Object>();

  public MapValues() {
  }

  public MapValues(final String sourceFieldName, final String targetFieldName) {
    this.sourceFieldName = sourceFieldName;
    this.targetFieldName = targetFieldName;
  }

  public MapValues(final String sourceFieldName, final String targetFieldName,
    final Map<Object, Object> valueMap) {
    this.sourceFieldName = sourceFieldName;
    this.targetFieldName = targetFieldName;
    this.valueMap = valueMap;
  }

  public void addValueMap(final Object sourceValue, final Object targetValue) {
    this.valueMap.put(sourceValue, targetValue);
  }

  public String getSourceFieldName() {
    return this.sourceFieldName;
  }

  public String getTargetFieldName() {
    return this.targetFieldName;
  }

  public Map<Object, Object> getValueMap() {
    return this.valueMap;
  }

  @Override
  public void process(final Record source, final Record target) {
    final Object sourceValue = RecordUtil.getFieldByPath(source,
      this.sourceFieldName);
    if (sourceValue != null) {
      final Object targetValue = this.valueMap.get(sourceValue);
      if (targetValue != null) {
        final RecordDefinition targetRecordDefinition = target.getRecordDefinition();
        final CodeTable codeTable = targetRecordDefinition.getCodeTableByColumn(this.targetFieldName);
        if (codeTable == null) {
          target.setValue(this.targetFieldName, targetValue);
        } else {
          final Object codeId = codeTable.getId(targetValue);
          target.setValue(this.targetFieldName, codeId);
        }
      }
    }
  }

  public void setSourceFieldName(final String sourceFieldName) {
    this.sourceFieldName = sourceFieldName;
  }

  public void setTargetFieldName(final String targetFieldName) {
    this.targetFieldName = targetFieldName;
  }

  public void setValueMap(final Map<Object, Object> valueMap) {
    this.valueMap = valueMap;
  }

  @Override
  public String toString() {
    return "copy" + this.valueMap;
  }
}
