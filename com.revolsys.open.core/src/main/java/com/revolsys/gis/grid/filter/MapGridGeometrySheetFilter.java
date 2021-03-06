package com.revolsys.gis.grid.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

/**
 * The MapGridGeometrySheetFilter will compare the centroid of the Geometry for
 * a data object to check that it is within the specified map sheet.
 *
 * @author Paul Austin
 */
public class MapGridGeometrySheetFilter implements Filter<Record> {
  /** Set the grid to check the mapsheet for. */
  private RectangularMapGrid grid;

  private boolean inverse;

  /** The map sheet name. */
  private String sheet;

  @Override
  public boolean accept(final Record object) {
    if (this.sheet != null && this.grid != null) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        final Geometry geographicsGeometry = geometry.convert(GeometryFactory.floating3(4326));
        final Point centroid = geographicsGeometry.getCentroid()
            .getPoint();
        final String geometrySheet = this.grid.getMapTileName(centroid.getX(),
          centroid.getY());
        if (geometrySheet != null) {
          if (this.sheet.equals(geometrySheet) == !this.inverse) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * @return the grid
   */
  public RectangularMapGrid getGrid() {
    return this.grid;
  }

  /**
   * @return the sheet
   */
  public String getSheet() {
    return this.sheet;
  }

  /**
   * @return the inverse
   */
  public boolean isInverse() {
    return this.inverse;
  }

  /**
   * @param grid the grid to set
   */
  public void setGrid(final RectangularMapGrid grid) {
    this.grid = grid;
  }

  /**
   * @param inverse the inverse to set
   */
  public void setInverse(final boolean inverse) {
    this.inverse = inverse;
  }

  /**
   * @param sheet the sheet to set
   */
  public void setSheet(final String sheet) {
    this.sheet = sheet;
  }

  @Override
  public String toString() {
    if (this.inverse) {
      return "map sheet != " + this.sheet;
    } else {
      return "map sheet != " + this.sheet;
    }
  }

}
