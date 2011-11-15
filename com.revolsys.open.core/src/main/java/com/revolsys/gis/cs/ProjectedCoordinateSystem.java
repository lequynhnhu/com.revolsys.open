package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.vividsolutions.jts.geom.Envelope;

public class ProjectedCoordinateSystem implements CoordinateSystem {
  /**
   * 
   */
  private static final long serialVersionUID = 1902383026085071877L;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<Axis>();

  private boolean deprecated;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private final LinearUnit linearUnit;

  private final int id;

  private final String name;

  private final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

  private final Map<String, Object> lowerParameters = new TreeMap<String, Object>();

  private final Projection projection;

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Area area, final Projection projection,
    final Map<String, Object> parameters, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.area = area;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    setParameters(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public void setParameters(final Map<String, Object> parameters) {
    this.parameters.putAll(parameters);
    for (Entry<String, Object> param : parameters.entrySet()) {
      lowerParameters.put(param.getKey().toLowerCase(), param.getValue());
    }
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Projection projection, final Map<String, Object> parameters,
    final LinearUnit linearUnit, final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    setParameters(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem cs = (ProjectedCoordinateSystem)object;
      if (!geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (!equals(projection, cs.projection)) {
        return false;
      } else if (!equals(lowerParameters, cs.lowerParameters)) {
        return false;
      } else if (!equals(linearUnit, cs.linearUnit)) {
        return false;
      } else if (!equals(axis, cs.axis)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean equals(final Object object1, final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null || object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }

  public Area getArea() {
    return area;
  }

  public BoundingBox getAreaBoundingBox() {
    BoundingBox boundingBox;
    final GeometryFactory geographicGeometryFactory = geographicCoordinateSystem.getGeometryFactory();
    if (area == null) {
      boundingBox = new BoundingBox(geographicGeometryFactory, -180, -90, 180,
        90);
    } else {
      final Envelope latLonBounds = area.getLatLonBounds();
      boundingBox = new BoundingBox(geographicGeometryFactory, latLonBounds);
    }
    final BoundingBox projectedBoundingBox = boundingBox.convert(getGeometryFactory());
    return projectedBoundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory(this);
  }

  public Authority getAuthority() {
    return authority;
  }

  public List<Axis> getAxis() {
    return axis;
  }

  public double getDoubleParameter(final String key) {
    final Number value = getParameter(key);
    if (value == null) {
      return Double.NaN;
    } else {
      return value.doubleValue();
    }
  }

  public GeographicCoordinateSystem getGeographicCoordinateSystem() {
    return geographicCoordinateSystem;
  }

  public Unit<Length> getLengthUnit() {
    return linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return linearUnit;
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(final String key) {
    return (V)parameters.get(key);
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public Projection getProjection() {
    return projection;
  }

  @SuppressWarnings("unchecked")
  public Unit<Length> getUnit() {
    return linearUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + geographicCoordinateSystem.hashCode();
    if (projection != null) {
      result = prime * result + projection.hashCode();
    }
    for (Entry<String, Object> entry : lowerParameters.entrySet()) {
      final String key = entry.getKey();
      result = prime * result + key.hashCode();
      result = prime * result + entry.getValue().hashCode();
    }
    if (linearUnit != null) {
      result = prime * result + linearUnit.hashCode();
    }
    result = prime * result + axis.hashCode();
    return result;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public String toString() {
    return name;
  }
}