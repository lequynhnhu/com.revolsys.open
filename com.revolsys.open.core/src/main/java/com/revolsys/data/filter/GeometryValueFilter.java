package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class GeometryValueFilter implements Filter<Record> {
  private final Geometry geometry;

  public GeometryValueFilter(final Geometry geometry) {
    this.geometry = geometry;
  }

  public GeometryValueFilter(final Record object) {
    this(object.getGeometryValue());
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry value = object.getGeometryValue();
    if (value == this.geometry) {
      return true;
    } else if (value != null && this.geometry != null) {
      return value.equals(this.geometry);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return " geometry == " + this.geometry;
  }

}
