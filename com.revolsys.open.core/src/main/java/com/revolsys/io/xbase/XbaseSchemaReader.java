package com.revolsys.io.xbase;

import java.io.IOException;
import java.util.List;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.io.EndianInput;

public class XbaseSchemaReader {
  private final List<XBaseFieldDefinition> fieldDefinitions;

  private final EndianInput in;

  private RecordDefinitionImpl recordDefinition;

  private final String typePath;

  public XbaseSchemaReader(final EndianInput in, final String typePath,
    final List<XBaseFieldDefinition> fieldDefinitions) {
    this.in = in;
    this.typePath = typePath;
    this.fieldDefinitions = fieldDefinitions;
  }

  protected RecordDefinition getRecordDefinition() throws IOException {
    if (this.recordDefinition == null) {
      this.recordDefinition = new RecordDefinitionImpl(this.typePath);
      int b = this.in.read();
      while (b != 0x0D) {
        final StringBuilder fieldName = new StringBuilder();
        boolean endOfName = false;
        for (int i = 0; i < 11; i++) {
          if (!endOfName && b != 0) {
            fieldName.append((char)b);
          } else {

            endOfName = true;
          }
          if (i != 10) {
            b = this.in.read();
          }
        }
        final char fieldType = (char)this.in.read();
        this.in.skipBytes(4);
        final int length = this.in.read();
        this.in.skipBytes(15);
        b = this.in.read();
        final XBaseFieldDefinition field = new XBaseFieldDefinition(
          fieldName.toString(), fieldName.toString(), fieldType, length);
        if (this.fieldDefinitions != null) {
          this.fieldDefinitions.add(field);
        }
        this.recordDefinition.addField(fieldName.toString(),
          field.getDataType(), length, true);
      }
    }
    return this.recordDefinition;
  }

}
