package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.LineString;

public class LineIntersectsFilter implements Filter<LineString> {
  private final LineString line;

  public LineIntersectsFilter(final LineString line) {
    this.line = line;
  }

  @Override
  public boolean accept(final LineString line) {
    return LineStringUtil.intersects(this.line, line);
  }
}
