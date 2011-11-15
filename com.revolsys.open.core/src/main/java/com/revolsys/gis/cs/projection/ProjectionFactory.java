package com.revolsys.gis.cs.projection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.vividsolutions.jts.geom.Geometry;

public final class ProjectionFactory {
  /** The map from projection names to projection classes. */
  private static final Map<String, Class<? extends CoordinatesProjection>> projectionClasses = new HashMap<String, Class<? extends CoordinatesProjection>>();

  static {
    registerCoordinatesProjection("Albers_Equal_Area",
      AlbersConicEqualArea.class);
    registerCoordinatesProjection("Transverse_Mercator",
      TransverseMercator.class);
    registerCoordinatesProjection("Mercator", Mercator1SP.class);
    registerCoordinatesProjection("Mercator_(1SP)", Mercator1SP.class);
    registerCoordinatesProjection("Mercator_(2SP)", Mercator2SP.class);
    registerCoordinatesProjection("Mercator_(1SP)_(Spherical)",
      Mercator1SPSpherical.class);
    registerCoordinatesProjection("Lambert_Conic_Conformal_(1SP)",
      LambertConicConformal1SP.class);
    registerCoordinatesProjection("Lambert_Conic_Conformal_(2SP)",
      LambertConicConformal.class);
    registerCoordinatesProjection("Lambert_Conic_Conformal_(2SP_Belgium)",
      LambertConicConformal.class);
  }

  public static <T extends Geometry> T convert(
    final T geometry,
    final GeometryFactory targetGeometryFactory) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    if (geometryFactory == targetGeometryFactory) {
      return geometry;
    } else if (geometryFactory == null) {
      return geometry;
    } else {
      final GeometryOperation operation = getGeometryOperation(geometryFactory,
        targetGeometryFactory);
      if (operation == null) {
        return geometry;
      } else {
        return operation.perform(geometry);
      }
    }
  }

  public static CoordinatesOperation getCoordinatesOperation(
    final CoordinateSystem cs1,
    final CoordinateSystem cs2) {
    if (cs1.equals(cs2)) {
      return null;
    } else {
      final List<CoordinatesOperation> operations = new ArrayList<CoordinatesOperation>();
      if (cs1 instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem pcs1 = (ProjectedCoordinateSystem)cs1;
        final CoordinatesOperation inverseOperation = getInverseCoordinatesOperation(pcs1);
        if (inverseOperation == null) {
          return null;
        }
        final Unit<Length> linearUnit1 = pcs1.getLengthUnit();
        if (!linearUnit1.equals(SI.METRE)) {
          operations.add(new UnitConverstionOperation(linearUnit1, SI.METRE));
        }
        operations.add(inverseOperation);
      } else if (cs1 instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem gcs1 = (GeographicCoordinateSystem)cs1;
        final Unit<Angle> angularUnit1 = gcs1.getUnit();
        if (cs2 instanceof GeographicCoordinateSystem) {
          final GeographicCoordinateSystem gcs2 = (GeographicCoordinateSystem)cs2;
          // TODO Datum shift
          final Unit<Angle> angularUnit2 = gcs2.getUnit();
          if (!angularUnit1.equals(angularUnit2)) {
            return new UnitConverstionOperation(angularUnit1, angularUnit2);
          } else {
            return null;
          }
        } else {
          if (!angularUnit1.equals(SI.RADIAN)) {
            operations.add(new UnitConverstionOperation(angularUnit1, SI.RADIAN));
          }
        }
      } else {
        return null;
      }
      if (cs2 instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem pcs2 = (ProjectedCoordinateSystem)cs2;
        final CoordinatesOperation projectOperation = getProjectCoordinatesOperation(pcs2);
        if (projectOperation != null) {
          operations.add(projectOperation);
        }
        final Unit<Length> linearUnit2 = pcs2.getLengthUnit();
        if (!linearUnit2.equals(SI.METRE)) {
          operations.add(new UnitConverstionOperation(SI.METRE, linearUnit2));
        }
      } else if (cs2 instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem gcs2 = (GeographicCoordinateSystem)cs2;
        final Unit<Angle> angularUnit2 = gcs2.getUnit();
        if (!angularUnit2.equals(SI.RADIAN)) {
          operations.add(new UnitConverstionOperation(SI.RADIAN, angularUnit2));
        }
      }
      switch (operations.size()) {
        case 0:
          return null;
        case 1:
          return operations.get(0);
        default:
          return new ChainedCoordinatesOperation(operations);
      }
    }
  }

  /**
   * Get the projection for a projected coordinate system.
   * 
   * @param coordinateSystem The coordinate system.
   * @return The projection.
   */
  public static CoordinatesProjection getCoordinatesProjection(
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final Projection projection = projectedCoordinateSystem.getProjection();
      final String projectionName = projection.getName();
      final Class<? extends CoordinatesProjection> projectionClass = projectionClasses.get(projectionName);
      if (projectionClass == null) {
        return null;
      } else {
        try {
          final Constructor<? extends CoordinatesProjection> constructor = projectionClass.getConstructor(ProjectedCoordinateSystem.class);
          final CoordinatesProjection coordinateProjection = constructor.newInstance(coordinateSystem);
          return coordinateProjection;
        } catch (final NoSuchMethodException e) {
          throw new IllegalArgumentException("Constructor " + projectionClass
            + "(" + ProjectedCoordinateSystem.class.getName()
            + ") does not exist");
        } catch (final InstantiationException e) {
          throw new IllegalArgumentException(projectionClass
            + " cannot be instantiated", e);
        } catch (final IllegalAccessException e) {
          throw new IllegalArgumentException(projectionClass
            + " cannot be instantiated", e);
        } catch (final InvocationTargetException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
          } else if (cause instanceof Error) {
            throw (Error)cause;
          } else {
            throw new IllegalArgumentException(projectionClass
              + " cannot be instantiated", cause);
          }
        }
      }
    } else {
      return null;
    }
  }

  /**
   * Return a geometry operation which convert between the source and target
   * coordinate systems. The geometry operation will create a new geometry
   * instead of modifying the existing geometry.
   * 
   * @param sourceCoordinateSystem The source coordinate system.
   * @param targetCoordinateSystem The target coordinate system.
   * @return The geometry operation.
   */
  public static GeometryOperation getGeometryOperation(
    final CoordinateSystem sourceCoordinateSystem,
    final CoordinateSystem targetCoordinateSystem) {
    return getGeometryOperation(sourceCoordinateSystem, targetCoordinateSystem,
      new GeometryFactory(targetCoordinateSystem));
  }

  /**
   * Return a geometry operation which convert between the source and target
   * coordinate systems. The geometry operation will create a new geometry
   * instead of modifying the existing geometry. All coordinates in the geometry
   * will be made precise using the precision model of the geometry factory.
   * 
   * @param sourceCoordinateSystem The source coordinate system.
   * @param targetCoordinateSystem The target coordinate system.
   * @param geometryFactory The geometry factory.
   * @return The geometry operation.
   */
  public static GeometryOperation getGeometryOperation(
    final CoordinateSystem cs1,
    final CoordinateSystem cs2,
    final GeometryFactory geometryFactory) {
    final CoordinatesOperation operation = getCoordinatesOperation(cs1, cs2);
    if (operation == null) {
      return null;
    } else {
      return new CoordinatesOperationGeometryOperation(operation,
        geometryFactory);
    }
  }

  /**
   * Return a geometry operation which convert between the source and target
   * coordinate systems. The geometry operation will create a new geometry
   * instead of modifying the existing geometry. All coordinates in the geometry
   * will be made precise using the precision model of the geometry factory.
   * 
   * @param sourceCoordinateSystem The source coordinate system.
   * @param targetGeometryFactory The geometry factory.
   * @return The geometry operation.
   */
  public static GeometryOperation getGeometryOperation(
    final CoordinateSystem sourceCoordinateSystem,
    final GeometryFactory targetGeometryFactory) {
    final CoordinateSystem targetCoordinateSystem = targetGeometryFactory.getCoordinateSystem();
    final CoordinatesOperation operation = getCoordinatesOperation(
      sourceCoordinateSystem, targetCoordinateSystem);
    if (operation == null) {
      return null;
    } else {
      return new CoordinatesOperationGeometryOperation(operation,
        targetGeometryFactory);
    }
  }

  public static GeometryOperation getGeometryOperation(
    final GeometryFactory sourceGeometryFactory,
    final GeometryFactory targetGeometryFactory) {
    final CoordinateSystem sourceCoodinateSystem = sourceGeometryFactory.getCoordinateSystem();
    return getGeometryOperation(sourceCoodinateSystem, targetGeometryFactory);
  }

  /**
   * Get the operation to convert coordinates to geographics coordinates.
   * 
   * @param coordinateSystem The coordinate system.
   * @return The coordinates operation.
   */
  public static CoordinatesOperation getInverseCoordinatesOperation(
    final CoordinateSystem coordinateSystem) {
    final CoordinatesProjection projection = getCoordinatesProjection(coordinateSystem);
    if (projection == null) {
      return null;
    } else {
      return new InverseOperation(projection);
    }
  }

  /**
   * Get the operation to convert geographics coordinates to projected
   * coordinates.
   * 
   * @param coordinateSystem The coordinate system.
   * @return The coordinates operation.
   */
  public static CoordinatesOperation getProjectCoordinatesOperation(
    final CoordinateSystem coordinateSystem) {
    final CoordinatesProjection projection = getCoordinatesProjection(coordinateSystem);
    if (projection == null) {
      return new CopyOperation();
    } else {
      return new ProjectOperation(projection);
    }
  }

  /**
   * Get the operation to convert coordinates to geographics coordinates.
   * 
   * @param coordinateSystem The coordinate system.
   * @return The coordinates operation.
   */
  public static CoordinatesOperation getToGeographicsCoordinatesOperation(
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
      final CoordinatesOperation operation = getInverseCoordinatesOperation(projectedCs);
      final GeographicCoordinateSystem geographicsCoordinateSystem = projectedCs.getGeographicCoordinateSystem();

      final Unit<Angle> angularUnit = geographicsCoordinateSystem.getUnit();
      if (angularUnit.equals(SI.RADIAN)) {
        return operation;
      } else {
        return new ChainedCoordinatesOperation(operation,
          new UnitConverstionOperation(SI.RADIAN, angularUnit));
      }
    } else {
      return new CopyOperation();
    }
  }

  /**
   * Register a projection for the named projection.
   * 
   * @param name The name.
   * @param projectionClass The projection class.
   */
  public static void registerCoordinatesProjection(
    final String name,
    final Class<? extends CoordinatesProjection> projectionClass) {
    projectionClasses.put(name, projectionClass);
  }

  private ProjectionFactory() {
  }
}