package com.revolsys.swing.map.layer.record.renderer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.PointWithOrientation;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.panel.TextStylePanel;
import com.revolsys.util.Property;

public class TextStyleRenderer extends AbstractRecordLayerRenderer {

  public static String getLabel(final Record object, final TextStyle style) {
    if (object == null) {
      return "Text";
    } else {
      final StringBuffer label = new StringBuffer();
      final String labelPattern = style.getTextName();
      final Matcher matcher = Pattern.compile("\\[([\\w.]+)\\]").matcher(
        labelPattern);
      while (matcher.find()) {
        final String propertyName = matcher.group(1);
        final Object value = object.getValueByPath(propertyName);
        String text;
        if (value == null) {
          text = "";
        } else {
          text = StringConverterRegistry.toString(value);
        }
        matcher.appendReplacement(label, text);
      }
      matcher.appendTail(label);

      return label.toString().trim();
    }
  }

  public static PointWithOrientation getTextLocation(final Viewport2D viewport,
    final Geometry geometry, final TextStyle style) {
    if (viewport == null) {
      return new PointWithOrientation(new PointDouble(0.0, 0.0), 0);
    }
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null && geometry != null
        && !geometry.isEmpty()) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();

      Point point = null;

      double orientation = style.getTextOrientation();
      final String placementType = style.getTextPlacementType();
      final Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
        placementType);
      final int vertexCount = geometry.getVertexCount();
      if (vertexCount == 1) {
        point = geometry.getPoint();
        point = point.convert(viewportGeometryFactory);

        return new PointWithOrientation(point, 0);
      } else if (vertexCount > 1) {
        final boolean matches = matcher.matches();
        if (matches) {
          final String argument = matcher.group(1);
          int index;
          if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
            final String indexString = argument.replaceAll("[^0-9]+", "");
            index = vertexCount - 1;
            if (indexString.length() > 0) {
              index -= Integer.parseInt(indexString);
            }
            if (index == 0) {
              index++;
            }
            point = geometry.getVertex(index).convert(viewportGeometryFactory);
            final Point p2 = geometry.getVertex(index - 1).convert(
              viewportGeometryFactory);
            final double angle = Math.toDegrees(p2.angle2d(point));
            orientation += angle;
          } else {
            index = Integer.parseInt(argument);
            if (index + 1 == vertexCount) {
              index--;
            }
            point = geometry.getVertex(index).convert(viewportGeometryFactory);
            final Point p2 = geometry.getVertex(index + 1).convert(
              viewportGeometryFactory);
            final double angle = Math.toDegrees(point.angle2d(p2));
            orientation += angle;
          }

        } else if ("center".equals(placementType)) {
          return getTextLocationCenter(viewportGeometryFactory, geometry, style);
        }
        if (point == null) {
          point = getTextLocationCenter(viewportGeometryFactory, geometry,
            style);
          if (!viewport.getBoundingBox().covers(point)) {
            Geometry clippedGeometry = geometry;
            try {
              clippedGeometry = viewport.getBoundingBox()
                  .toPolygon()
                  .intersection(geometry);
            } catch (final Throwable t) {
            }
            if (!clippedGeometry.isEmpty()) {
              double maxArea = 0;
              double maxLength = 0;
              for (int i = 0; i < clippedGeometry.getGeometryCount(); i++) {
                final Geometry part = clippedGeometry.getGeometry(i);
                if (part instanceof Polygon) {
                  final double area = part.getArea();
                  if (area > maxArea) {
                    maxArea = area;
                    point = getTextLocationCenter(viewportGeometryFactory,
                      geometry, style);
                  }
                } else if (part instanceof LineString) {
                  if (maxArea == 0) {
                    final double length = part.getLength();
                    if (length > maxLength) {
                      maxLength = length;
                      point = getTextLocationCenter(viewportGeometryFactory,
                        geometry, style);
                    }
                  }
                } else if (part instanceof Point) {
                  if (maxArea == 0 && maxLength == 0) {
                    point = getTextLocationCenter(viewportGeometryFactory,
                      geometry, style);
                  }
                }
              }
            }
          }
        }

        if (point != null) {
          final String orientationType = style.getTextOrientationType();
          if ("none".equals(orientationType)) {
            return new PointWithOrientation(point, 0);
          }
          if (point instanceof PointWithOrientation) {
            return (PointWithOrientation)point;
          } else {
            return new PointWithOrientation(point, orientation);
          }
        }
      }
    }
    return null;
  }

  public static PointWithOrientation getTextLocationCenter(
    final GeometryFactory viewportGeometryFactory, final Geometry geometry,
    final TextStyle style) {

    double orientation = style.getTextOrientation();
    Point point = null;
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;

      final double totalLength = line.getLength();
      final double centreLength = totalLength / 2;
      double currentLength = 0;
      for (int i = 1; i < line.getVertexCount() && currentLength < centreLength; i++) {
        Point p1 = line.getVertex(i - 1);
        Point p2 = line.getVertex(i);
        final double segmentLength = p1.distance(p2);
        if (segmentLength + currentLength >= centreLength) {
          p1 = p1.convert(viewportGeometryFactory);
          p2 = p2.convert(viewportGeometryFactory);
          point = LineSegmentUtil.project(viewportGeometryFactory, p1, p2,
            (centreLength - currentLength) / segmentLength);

          final double angle = Math.toDegrees(p1.angle2d(p2));
          orientation += angle;
        }
        currentLength += segmentLength;
      }
    } else {
      point = geometry.getPointWithin();
    }
    return new PointWithOrientation(point, orientation);
  }

  public static final void renderText(final Viewport2D viewport,
    final Graphics2D graphics, final Record object, final Geometry geometry,
    final TextStyle style) {
    final String label = getLabel(object, style);
    renderText(viewport, graphics, label, geometry, style);
  }

  public static void renderText(final Viewport2D viewport,
    final Graphics2D graphics, final String label, final Geometry geometry,
    final TextStyle style) {
    if (Property.hasValue(label) && geometry != null || viewport == null) {
      final PointWithOrientation point = getTextLocation(viewport, geometry,
        style);
      if (point != null) {
        final double orientation = point.getOrientation();

        final Paint paint = graphics.getPaint();
        final Composite composite = graphics.getComposite();
        try {
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
          style.setTextStyle(viewport, graphics);

          final double x = point.getX();
          final double y = point.getY();
          final double[] location;
          if (viewport == null) {
            location = new double[] {
              x, y
            };
          } else {
            location = viewport.toViewCoordinates(x, y);
          }

          final AffineTransform savedTransform = graphics.getTransform();

          style.setTextStyle(viewport, graphics);

          final Measure<Length> textDx = style.getTextDx();
          double dx = Viewport2D.toDisplayValue(viewport, textDx);

          final Measure<Length> textDy = style.getTextDy();
          double dy = -Viewport2D.toDisplayValue(viewport, textDy);

          final FontMetrics fontMetrics = graphics.getFontMetrics();

          double maxWidth = 0;
          final String[] lines = label.split("[\\r\\n]");
          for (final String line : lines) {
            final Rectangle2D bounds = fontMetrics.getStringBounds(line,
              graphics);
            final double width = bounds.getWidth();
            maxWidth = Math.max(width, maxWidth);
          }
          final int descent = fontMetrics.getDescent();
          final int ascent = fontMetrics.getAscent();
          final int leading = fontMetrics.getLeading();
          final double maxHeight = lines.length * (ascent + descent)
              + (lines.length - 1) * leading;
          final String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
          } else if ("middle".equals(verticalAlignment)) {
            dy -= maxHeight / 2;
          } else {
            dy -= maxHeight;
          }

          String horizontalAlignment = style.getTextHorizontalAlignment();
          double screenX = location[0];
          double screenY = location[1];
          final String textPlacement = style.getTextPlacementType();
          if ("auto".equals(textPlacement) && viewport != null) {
            if (screenX < 0) {
              screenX = 1;
              dx = 0;
              horizontalAlignment = "left";
            }
            final int viewWidth = viewport.getViewWidthPixels();
            if (screenX + maxWidth > viewWidth) {
              screenX = (int)(viewWidth - maxWidth - 1);
              dx = 0;
              horizontalAlignment = "left";
            }
            if (screenY < maxHeight) {
              screenY = 1;
              dy = 0;
            }
            final int viewHeight = viewport.getViewHeightPixels();
            if (screenY > viewHeight) {
              screenY = viewHeight - 1 - maxHeight;
              dy = 0;
            }
          }
          graphics.translate(screenX, screenY);
          if (orientation != 0) {
            graphics.rotate(-Math.toRadians(orientation), 0, 0);
          }
          graphics.translate(dx, dy);
          for (final String line : lines) {
            graphics.translate(0, ascent);
            final AffineTransform lineTransform = graphics.getTransform();
            final Rectangle2D bounds = fontMetrics.getStringBounds(line,
              graphics);
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();

            if ("right".equals(horizontalAlignment)) {
              graphics.translate(-width, 0);
            } else if ("center".equals(horizontalAlignment)
                || "auto".equals(horizontalAlignment)) {
              graphics.translate(-width / 2, 0);
            }
            graphics.translate(dx, 0);

            graphics.scale(1, 1);
            if (Math.abs(orientation) > 90) {
              graphics.rotate(Math.PI, maxWidth / 2, -height / 4);
            }

            final int textBoxOpacity = style.getTextBoxOpacity();
            final Color textBoxColor = style.getTextBoxColor();
            if (textBoxOpacity > 0 && textBoxColor != null) {
              graphics.setPaint(textBoxColor);
              final double cornerSize = Math.max(height / 2, 5);
              final RoundRectangle2D.Double box = new RoundRectangle2D.Double(
                bounds.getX() - 3, bounds.getY() - 1, width + 6, height + 2,
                cornerSize, cornerSize);
              graphics.fill(box);
            }

            final double radius = style.getTextHaloRadius();
            final Unit<Length> unit = style.getTextSizeUnit();
            final double textHaloRadius = Viewport2D.toDisplayValue(viewport,
              Measure.valueOf(radius, unit));
            if (textHaloRadius > 0) {
              final Stroke savedStroke = graphics.getStroke();
              final Stroke outlineStroke = new BasicStroke(
                (float)(textHaloRadius + 1), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL);
              graphics.setColor(style.getTextHaloFill());
              graphics.setStroke(outlineStroke);
              final Font font = graphics.getFont();
              final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
              final TextLayout textLayout = new TextLayout(line, font,
                fontRenderContext);
              final Shape outlineShape = textLayout.getOutline(NOOP_TRANSFORM);
              graphics.draw(outlineShape);
              graphics.setStroke(savedStroke);
            }

            graphics.setColor(style.getTextFill());
            if (textBoxOpacity > 0 && textBoxOpacity < 255) {
              graphics.setComposite(AlphaComposite.SrcOut);
              graphics.drawString(line, (float)0, (float)0);
              graphics.setComposite(AlphaComposite.DstOver);
              graphics.drawString(line, (float)0, (float)0);

            } else {
              graphics.setComposite(AlphaComposite.SrcOver);
              graphics.drawString(line, (float)0, (float)0);
            }

            graphics.setTransform(lineTransform);
            graphics.translate(0, leading + descent);
          }
          graphics.setTransform(savedTransform);

        } finally {
          graphics.setPaint(paint);
          graphics.setComposite(composite);
        }
      }
    }
  }

  public static final AffineTransform NOOP_TRANSFORM = AffineTransform.getTranslateInstance(
    0, 0);

  private static final Icon ICON = Icons.getIcon("style_text");

  private TextStyle style;

  public TextStyleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public TextStyleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> textStyle) {
    super("textStyle", "Text Style", layer, parent, textStyle);
    this.style = new TextStyle(textStyle);
    setIcon(ICON);
  }

  @Override
  public TextStyleRenderer clone() {
    final TextStyleRenderer clone = (TextStyleRenderer)super.clone();
    clone.style = this.style.clone();
    return clone;
  }

  @Override
  public ValueField createStylePanel() {
    return new TextStylePanel(this);
  }

  public TextStyle getStyle() {
    return this.style;
  }

  @Override
  public void renderRecord(final Viewport2D viewport,
    final BoundingBox visibleArea, final AbstractLayer layer,
    final LayerRecord object) {
    final Geometry geometry = object.getGeometryValue();
    viewport.drawText(object, geometry, this.style);
  }

  public void setStyle(final TextStyle style) {
    this.style = style;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
