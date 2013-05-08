package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoTiffImage;

public class TiledImageLayerRenderer extends
  AbstractLayerRenderer<AbstractTiledImageLayer> implements
  PropertyChangeListener {

  private final Map<MapTile, MapTile> cachedTiles = new HashMap<MapTile, MapTile>();

  private final Map<MapTile, TileLoaderProcess> imageLoading = new HashMap<MapTile, TileLoaderProcess>();

  private double scale = -1;

  public TiledImageLayerRenderer(final AbstractTiledImageLayer layer) {
    super("tiledImage", layer);
    layer.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final Object newValue = evt.getNewValue();
    if (newValue instanceof BoundingBox) {
      final BoundingBox newBoundingBox = (BoundingBox)newValue;
      synchronized (cachedTiles) {
        final List<MapTile> mapTiles = new ArrayList<MapTile>(
          cachedTiles.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final MapTile mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            cachedTiles.remove(boundingBox);
          }
        }
      }

      synchronized (imageLoading) {
        final List<MapTile> mapTiles = new ArrayList<MapTile>(
          imageLoading.keySet());
        final GeometryFactory newGeometryFactory = newBoundingBox.getGeometryFactory();
        for (final MapTile mapTile : mapTiles) {
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          if (!geometryFactory.equals(newGeometryFactory)
            || !newBoundingBox.intersects(boundingBox)) {
            final TileLoaderProcess process = imageLoading.remove(boundingBox);
            process.cancel(true);
          }
        }
      }
    } else if (!"loading".equals(evt.getPropertyName())) {
      synchronized (cachedTiles) {
        cachedTiles.clear();
      }
      synchronized (imageLoading) {
        final ArrayList<TileLoaderProcess> processes = new ArrayList<TileLoaderProcess>(
          imageLoading.values());
        for (final TileLoaderProcess process : processes) {
          process.cancel(true);
        }
        imageLoading.clear();
      }

    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractTiledImageLayer layer) {
    synchronized (cachedTiles) {
      final double viewportScale = viewport.getScale();
      if (viewportScale != scale) {
        cachedTiles.clear();
        this.scale = viewportScale;
      }
    }
    for (final MapTile mapTile : getLayer().getOverlappingEnvelopes(viewport)) {
      if (mapTile != null) {
        MapTile cachedTile = null;

        synchronized (imageLoading) {
          cachedTile = cachedTiles.get(mapTile);
          if (cachedTile == null) {
            cachedTile = mapTile;
            cachedTiles.put(cachedTile, cachedTile);
            final TileLoaderProcess process = getLayer().getTileLoaderProcess();
            imageLoading.put(cachedTile, process);
            process.execute(viewport, scale, cachedTile, this);

          }
        }
        GeoReferencedImageLayerRenderer.render(viewport, graphics, cachedTile);
      }
    }
  }

  public void setLoaded(final Viewport2D viewport, final MapTile mapTile) {
    synchronized (imageLoading) {
      imageLoading.remove(mapTile);
    }
    viewport.update();
    getLayer().getPropertyChangeSupport().firePropertyChange("loading", false,
      true);
  }

}
