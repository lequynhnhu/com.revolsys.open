package com.revolsys.gis.algorithm.index;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.MathUtil;

public class PointQuadTreeNode<T> {
  private final double x;

  private final double y;

  private final T value;

  private PointQuadTreeNode<T> northWest;

  private PointQuadTreeNode<T> northEast;

  private PointQuadTreeNode<T> southWest;

  private PointQuadTreeNode<T> southEast;

  public PointQuadTreeNode(final T value, final double x, final double y) {
    this.value = value;
    this.x = x;
    this.y = y;
  }

  public boolean contains(final Point point) {
    if (point.equals(this.x, this.y)) {
      return true;
    }
    final boolean xLess = isLessThanX(point.getX());
    final boolean yLess = isLessThanY(point.getY());
    if (this.southWest != null && xLess && yLess) {
      if (this.southWest.contains(point)) {
        return true;
      }
    }
    if (this.northWest != null && xLess && !yLess) {
      if (this.northWest.contains(point)) {
        return true;
      }
    }
    if (this.southEast != null && !xLess && yLess) {
      if (this.southEast.contains(point)) {
        return true;
      }
    }
    if (this.northEast != null && !xLess && !yLess) {
      if (this.northEast.contains(point)) {
        return true;
      }
    }
    return false;
  }

  public void findEntriesWithin(final List<Entry<Point, T>> results,
    final BoundingBox envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.covers(this.x, this.y)) {
      final PointDouble coordinates = new PointDouble(this.x, this.y);
      results.add(new SimpleImmutableEntry<Point, T>(coordinates, this.value));
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (this.southWest != null && minXLess && minYLess) {
      this.southWest.findEntriesWithin(results, envelope);
    }
    if (this.northWest != null && minXLess && !maxYLess) {
      this.northWest.findEntriesWithin(results, envelope);
    }
    if (this.southEast != null && !maxXLess && minYLess) {
      this.southEast.findEntriesWithin(results, envelope);
    }
    if (this.northEast != null && !maxXLess && !maxYLess) {
      this.northEast.findEntriesWithin(results, envelope);
    }
  }

  public void findWithin(final List<T> results, final BoundingBox envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.covers(this.x, this.y)) {
      results.add(this.value);
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (this.southWest != null && minXLess && minYLess) {
      this.southWest.findWithin(results, envelope);
    }
    if (this.northWest != null && minXLess && !maxYLess) {
      this.northWest.findWithin(results, envelope);
    }
    if (this.southEast != null && !maxXLess && minYLess) {
      this.southEast.findWithin(results, envelope);
    }
    if (this.northEast != null && !maxXLess && !maxYLess) {
      this.northEast.findWithin(results, envelope);
    }
  }

  public void findWithin(final List<T> results, final double x, final double y,
    final double maxDistance, final BoundingBox envelope) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    final double distance = MathUtil.distance(x, y, this.x, this.y);
    if (distance < maxDistance) {
      results.add(this.value);
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (this.southWest != null && minXLess && minYLess) {
      this.southWest.findWithin(results, x, y, maxDistance, envelope);
    }
    if (this.northWest != null && minXLess && !maxYLess) {
      this.northWest.findWithin(results, x, y, maxDistance, envelope);
    }
    if (this.southEast != null && !maxXLess && minYLess) {
      this.southEast.findWithin(results, x, y, maxDistance, envelope);
    }
    if (this.northEast != null && !maxXLess && !maxYLess) {
      this.northEast.findWithin(results, x, y, maxDistance, envelope);
    }
  }

  public boolean isLessThanX(final double x) {
    return x < this.x;
  }

  public boolean isLessThanY(final double y) {
    return y < this.y;
  }

  public void put(final double x, final double y,
    final PointQuadTreeNode<T> node) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (xLess && yLess) {
      if (this.southWest == null) {
        this.southWest = node;
      } else {
        this.southWest.put(x, y, node);
      }
    } else if (xLess && !yLess) {
      if (this.northWest == null) {
        this.northWest = node;
      } else {
        this.northWest.put(x, y, node);
      }
    } else if (!xLess && yLess) {
      if (this.southEast == null) {
        this.southEast = node;
      } else {
        this.southEast.put(x, y, node);
      }
    } else if (!xLess && !yLess) {
      if (this.northEast == null) {
        this.northEast = node;
      } else {
        this.northEast.put(x, y, node);
      }
    }
  }

  public PointQuadTreeNode<T> remove(final double x, final double y,
    final T value) {
    final boolean xLess = isLessThanX(x);
    final boolean yLess = isLessThanY(y);
    if (this.x == x && this.y == y && this.value == value) {
      final List<PointQuadTreeNode<T>> nodes = new ArrayList<PointQuadTreeNode<T>>();
      if (this.northWest != null) {
        nodes.add(this.northWest);
      }
      if (this.northEast != null) {
        nodes.add(this.northEast);
      }
      if (this.southWest != null) {
        nodes.add(this.southWest);
      }
      if (this.southEast != null) {
        nodes.add(this.southEast);
      }
      if (nodes.isEmpty()) {
        return null;
      } else {
        final PointQuadTreeNode<T> node = nodes.remove(0);
        for (final PointQuadTreeNode<T> subNode : nodes) {
          node.put(subNode.x, subNode.y, subNode);
        }
        return node;
      }
    } else if (xLess && yLess) {
      if (this.southWest != null) {
        this.southWest = this.southWest.remove(x, y, value);
      }
    } else if (xLess && !yLess) {
      if (this.northWest != null) {
        this.northWest = this.northWest.remove(x, y, value);
      }
    } else if (!xLess && yLess) {
      if (this.southEast != null) {
        this.southEast = this.southEast.remove(x, y, value);
      }
    } else if (!xLess && !yLess) {
      if (this.northEast != null) {
        this.northEast = this.northEast.remove(x, y, value);
      }
    }
    return this;
  }

  public void setValue(final int index, final double value) {
    throw new UnsupportedOperationException(
        "Cannot change the coordinates on a quad tree");
  }

  public boolean visit(final BoundingBox envelope, final Visitor<T> visitor) {
    final double minX = envelope.getMinX();
    final double maxX = envelope.getMaxX();
    final double minY = envelope.getMinY();
    final double maxY = envelope.getMaxY();
    if (envelope.covers(this.x, this.y)) {
      if (!visitor.visit(this.value)) {
        return false;
      }
    }
    final boolean minXLess = isLessThanX(minX);
    final boolean maxXLess = isLessThanX(maxX);
    final boolean minYLess = isLessThanY(minY);
    final boolean maxYLess = isLessThanY(maxY);
    if (this.southWest != null && minXLess && minYLess) {
      if (!this.southWest.visit(envelope, visitor)) {
        return false;
      }
    }
    if (this.northWest != null && minXLess && !maxYLess) {
      if (!this.northWest.visit(envelope, visitor)) {
        return false;
      }
    }
    if (this.southEast != null && !maxXLess && minYLess) {
      if (!this.southEast.visit(envelope, visitor)) {
        return false;
      }
    }
    if (this.northEast != null && !maxXLess && !maxYLess) {
      if (!this.northEast.visit(envelope, visitor)) {
        return false;
      }
    }
    return true;
  }

  public boolean visit(final Visitor<T> visitor) {
    if (!visitor.visit(this.value)) {
      return false;
    }
    if (this.southWest != null) {
      if (!this.southWest.visit(visitor)) {
        return false;
      }
    }
    if (this.northWest != null) {
      if (!this.northWest.visit(visitor)) {
        return false;
      }
    }
    if (this.southEast != null) {
      if (!this.southEast.visit(visitor)) {
        return false;
      }
    }
    if (this.northEast != null) {
      if (!this.northEast.visit(visitor)) {
        return false;
      }
    }
    return true;
  }

}
