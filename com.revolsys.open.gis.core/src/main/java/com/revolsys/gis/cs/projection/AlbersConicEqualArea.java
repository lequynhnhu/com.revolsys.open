package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.algorithm.Angle;

public class AlbersConicEqualArea implements CoordinatesProjection {
  /** Constant c = sq(m(phi1) + n * q(phi1) */
  private final double c;

  private final double e;

  /** sq(e). */
  private final double ee;

  /** Central Meridian. */
  private final double lambda0;

  /** Constant n = ( sq(m(phi1)) - sq(m(phi2) ) /( q(phi2) - q(phi1) ) */
  private final double n;

  /** Lattitude of Projection. */
  private final double phi0;

  /** First standard parallel. */
  private final double phi1;

  /** Second standard parallel. */
  private final double phi2;

  /** Constant rho0 = semiMajorAxis * sqrt( C - n * q(phi0) ) / n. */
  private final double rho0;

  private final double semiMajorAxis;

  /** The spheriod. */
  private final Spheroid spheroid;

  /** The false Easting. */
  private final double x0;

  /** The false Northing. */
  private final double y0;

  public AlbersConicEqualArea(
    final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final Double centralMeridian = cs.getDoubleParameter("longitude_of_false_origin");
    final Double firstStandardParallel = cs.getDoubleParameter("latitude_of_1st_standard_parallel");
    final Double secondStandardParallel = cs.getDoubleParameter("latitude_of_2nd_standard_parallel");
    final Double latitudeOfProjection = cs.getDoubleParameter("latitude_of_false_origin");

    this.spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter("easting_at_false_origin");
    this.y0 = cs.getDoubleParameter("northing_at_false_origin");
    this.lambda0 = Math.toRadians(centralMeridian);
    this.phi0 = Math.toRadians(latitudeOfProjection);
    this.phi1 = Math.toRadians(firstStandardParallel);
    this.phi2 = Math.toRadians(secondStandardParallel);

    semiMajorAxis = this.spheroid.getSemiMajorAxis();

    this.e = spheroid.getEccentricity();
    this.ee = spheroid.getEccentricitySquared();

    final double m1 = m(phi1);
    final double m2 = m(phi2);
    final double q0 = q(phi0);
    final double q1 = q(phi1);
    final double q2 = q(phi2);

    this.n = (m1 * m1 - m2 * m2) / (q2 - q1);
    this.c = m1 * m1 + this.n * q1;
    rho0 = (semiMajorAxis * Math.sqrt(c - n * q0)) / n;
  }

  /**
   * n = sin(phi1) + sin (phi2)
   * <p>
   * phi = sin-1( ( C - (rho ^ 2) * (n ^ 2) ) / 2 * n )
   * <p>
   * lambda =
   */
  public void inverse(
    final Coordinates from,
    final Coordinates to) {
    final double x = from.getX() - x0;
    final double y = from.getY() - y0;
    final double theta = Math.atan(x / (rho0 - y));
    final double rho = Math.sqrt(x * x + Math.pow((rho0 - y), 2.0));
    final double q = (c - (rho * rho * n * n) / (semiMajorAxis * semiMajorAxis))
      / n;
    final double lambda = lambda0 + theta / n;

    double li = Math.asin(q / 2.0);
    if (!Double.isNaN(li)) {
      double delta = 10e010;
      do {
        final double sinLi = Math.sin(li);

        final double j1 = Math.pow((1.0 - ee * Math.pow(sinLi, 2.0)), 2.0)
          / (2.0 * Math.cos(li));
        final double k1 = q / (1.0 - ee);
        final double k2 = sinLi / (1.0 - ee * Math.pow(sinLi, 2.0));
        final double k3 = (1.0 / (2.0 * e))
          * Math.log((1.0 - e * sinLi) / (1.0 + e * sinLi));
        final double lip1 = li + j1 * (k1 - k2 + k3);
        delta = Math.abs(lip1 - li);
        li = lip1;
      } while (!Double.isNaN(li) && delta > 1.0e-011);
    }
    double phi;
    if (Double.isNaN(li)) {
      phi = Angle.PI_OVER_2;
    } else {
      phi = li;
    }
    to.setValue(0, lambda);
    to.setValue(1, phi);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  /**
   * <pre>
   * cos(phi) / sqrt(1 - sq(e) * sq(sin(phi)))
   * </pre>
   * 
   * @param phi The lattitude in radians.
   * @return
   */
  private double m(
    final double phi) {
    final double sinPhi = Math.sin(phi);
    final double m = Math.cos(phi) / Math.sqrt(1.0 - ee * sinPhi * sinPhi);
    return m;
  }

  /**
   * <pre>
   * a = semiMajorAxis
   * lambda = lon;
   * phi = lat;
   * 
   * x = rho * sin(theta)
   * y = rho0 - rho * sin(theta)
   * 
   * rho = a * sqrt(C - n * q) / n
   * 
   * </pre>
   */
  public void project(
    final Coordinates from,
    final Coordinates to) {
    final double lambda = from.getX();
    final double phi = from.getY();
    final double q = q(phi);
    final double theta = n * (lambda - lambda0);
    final double rho = (semiMajorAxis * Math.sqrt(c - n * q)) / n;

    final double x = x0 + rho * Math.sin(theta);
    final double y = y0 + rho0 - rho * Math.cos(theta);

    to.setValue(0, x);
    to.setValue(1, y);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  /**
   * <pre>
   * (1 - sq(e)) *
   * (
   *   sin(phi) / (1 - sq(e) * sq(sin(phi))) ) -
   *   (1 / (2 * e)) *
   *   ln( ( 1 - e * sin(phi) ) / ( 1 + e sin(phi)))
   * )
   * 
   * </pre>
   * 
   * @param phi The lattitude in radians
   * @return
   */
  private double q(
    final double phi) {
    final double sinPhi = Math.sin(phi);
    final double eSinPhi = e * sinPhi;
    final double q = (1.0 - ee)
      * (sinPhi / (1.0 - ee * sinPhi * sinPhi) - (1.0 / (2.0 * e))
        * Math.log((1.0 - eSinPhi) / (1.0 + eSinPhi)));
    return q;
  }

  /**
   * q = (C - sq(rho) * sq(n) / sq(a)) /n
   * 
   * @param phi
   * @return
   */
  private double qInverse(
    final double rho) {
    return (c - rho * rho * n * n / (semiMajorAxis * semiMajorAxis)) / n;
  }
}