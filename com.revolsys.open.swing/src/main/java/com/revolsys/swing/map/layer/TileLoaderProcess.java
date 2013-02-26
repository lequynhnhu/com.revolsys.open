package com.revolsys.swing.map.layer;

import java.awt.Image;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.Viewport2D;

public abstract class TileLoaderProcess extends SwingWorker<Image, Void> {
  private Viewport2D viewport;

  private TiledImageLayerRenderer renderer;

  private MapTile mapTile;

  private double scale;

  @Override
  protected void done() {
    try {
      final Image image = get();
      mapTile.setImage(image);
    } catch (final CancellationException e) {
    } catch (final Throwable e) {
    }
    renderer.setLoaded(viewport, mapTile);
  }

  public Image execute(final Viewport2D viewport, final double scale,
    final MapTile mapTile, final TiledImageLayerRenderer renderer) {
    this.viewport = viewport;
    this.scale = scale;
    this.mapTile = mapTile;
    this.renderer = renderer;
    SwingWorkerManager.execute(this);
    return null;
  }

  public MapTile getMapTile() {
    return mapTile;
  }

  public BoundingBox getBoundingBox() {
    return mapTile.getBoundingBox();
  }

  public TiledImageLayerRenderer getRenderer() {
    return renderer;
  }

  public double getScale() {
    return scale;
  }

  public Viewport2D getViewport() {
    return viewport;
  }
}