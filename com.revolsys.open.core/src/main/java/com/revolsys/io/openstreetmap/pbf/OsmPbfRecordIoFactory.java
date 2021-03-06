package com.revolsys.io.openstreetmap.pbf;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryReaderFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;

public class OsmPbfRecordIoFactory extends
AbstractRecordAndGeometryReaderFactory {
  public OsmPbfRecordIoFactory() {
    super("Open Street Map PBF", true);
    addMediaTypeAndFileExtension("application/x-pbf+osm", "osm.pbf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final OsmPbfRecordIterator iterator = new OsmPbfRecordIterator(resource);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return Collections.singleton(EpsgCoordinateSystems.wgs84());
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystem instanceof GeographicCoordinateSystem;
  }

}
