package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;

public class ModifiedAttributePredicate implements HighlightPredicate {
  public static void add(final RecordRowTable table) {
    final RecordRowTableModel model = (RecordRowTableModel)table.getModel();
    final ModifiedAttributePredicate predicate = new ModifiedAttributePredicate(
      model);
    addModifiedHighlighters(table, predicate);
  }

  public static void addModifiedHighlighters(final JXTable table,
    final HighlightPredicate predicate) {

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.EVEN), WebColors.setAlpha(
        WebColors.YellowGreen, 127), WebColors.Black, WebColors.LimeGreen,
        Color.WHITE));

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.ODD), WebColors.YellowGreen,
      WebColors.Black, WebColors.Green, Color.WHITE));
  }

  private final RecordRowTableModel model;

  public ModifiedAttributePredicate(final RecordRowTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    String toolTip = null;
    boolean highlighted = false;
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final Record record = this.model.getRecord(rowIndex);
      if (record instanceof LayerRecord) {
        final LayerRecord layerRecord = (LayerRecord)record;
        final AbstractRecordLayer layer = layerRecord.getLayer();
        if (layer.isDeleted(layerRecord)) {
          highlighted = false;
        } else {
          final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
          final String fieldName = this.model.getFieldName(columnIndex);
          highlighted = layerRecord.isModified(fieldName);
          if (highlighted) {
            final RecordDefinition recordDefinition = layerRecord.getRecordDefinition();
            final Object originalValue = layerRecord.getOriginalValue(fieldName);
            final CodeTable codeTable = recordDefinition.getCodeTableByColumn(fieldName);
            String text;
            if (originalValue == null) {
              text = "-";
            } else if (codeTable == null) {
              text = StringConverterRegistry.toString(originalValue);
            } else {
              text = codeTable.getValue(SingleIdentifier.create(originalValue));
              if (text == null) {
                text = "-";
              }
            }
            toolTip = text;
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      highlighted = false;
    }
    final JComponent component = adapter.getComponent();
    if (toolTip != null && toolTip.length() > 100) {
      toolTip = toolTip.substring(0, 100) + "...";
    }
    component.setToolTipText(toolTip);
    return highlighted;
  }
}
