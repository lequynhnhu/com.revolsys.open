package com.revolsys.gis.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.parallel.process.AbstractProcess;

public abstract class BoundingBoxTaskSplitter extends AbstractProcess {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private BoundingBox boundingBox;

  private int numX = 10;

  private int numY = 10;

  private boolean logScriptInfo;

  private Geometry boundary;

  private Geometry preparedBoundary;

  public abstract void execute(BoundingBox cellBoundingBox);

  public Geometry getBoundary() {
    return this.boundary;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public int getNumX() {
    return this.numX;
  }

  public int getNumY() {
    return this.numY;
  }

  public boolean isLogScriptInfo() {
    return this.logScriptInfo;
  }

  protected void postRun() {
  }

  protected void preRun() {
    if (this.boundingBox != null) {
      if (this.boundary != null) {
        this.preparedBoundary = this.boundary.prepare();
      }
    }
  }

  @Override
  public void run() {
    preRun();
    try {
      if (this.boundingBox != null) {
        final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
        final double xInc = this.boundingBox.getWidth() / this.numX;
        final double yInc = this.boundingBox.getHeight() / this.numY;
        double y = this.boundingBox.getMinY();
        for (int j = 0; j < this.numX; j++) {
          double x = this.boundingBox.getMinX();
          for (int i = 0; i < this.numX; i++) {
            final BoundingBox cellBoundingBox = new BoundingBoxDoubleGf(geometryFactory,
              2, x, y, x + xInc, y + yInc);
            if (this.preparedBoundary == null
                || this.preparedBoundary.intersects(cellBoundingBox.toPolygon(50))) {
              if (this.logScriptInfo) {
                this.log.info("Processing bounding box "
                    + cellBoundingBox.toPolygon(1));
              }
              execute(cellBoundingBox);
            }
            x += xInc;
          }
          y += yInc;
        }
      }
    } finally {
      postRun();
    }
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setLogScriptInfo(final boolean logScriptInfo) {
    this.logScriptInfo = logScriptInfo;
  }

  public void setNumX(final int numX) {
    this.numX = numX;
  }

  public void setNumY(final int numY) {
    this.numY = numY;
  }

}
