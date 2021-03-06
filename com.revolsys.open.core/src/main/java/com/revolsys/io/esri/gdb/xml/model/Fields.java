package com.revolsys.io.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

public class Fields {

  private List<Field> fields = new ArrayList<Field>();

  public List<Field> getFields() {
    return this.fields;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
  }

  @Override
  public String toString() {
    return this.fields.toString();
  }
}
