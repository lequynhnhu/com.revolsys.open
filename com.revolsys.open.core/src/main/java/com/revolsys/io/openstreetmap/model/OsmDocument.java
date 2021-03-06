package com.revolsys.io.openstreetmap.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.LongHashMap;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.io.file.UrlResource;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.util.Property;

public class OsmDocument implements OsmConstants {

  public static OsmDocument create(final String serverUrl,
    BoundingBox boundingBox) {
    if (boundingBox != null) {
      boundingBox = boundingBox.convert(OsmConstants.WGS84_2D);
      if (!boundingBox.isEmpty()) {
        final StringBuilder url = new StringBuilder(serverUrl);
        url.append("map?bbox=");
        url.append(boundingBox.getMinX());
        url.append(",");
        url.append(boundingBox.getMinY());
        url.append(",");
        url.append(boundingBox.getMaxX());
        url.append(",");
        url.append(boundingBox.getMaxY());
        try {
          final Resource resource = new UrlResource(url.toString());
          return new OsmDocument(resource);
        } catch (final IOException e) {
          throw new RuntimeException("Unable to read " + url, e);
        }
      }
    }
    return new OsmDocument();
  }

  private String version;

  private String generator;

  private String copyright;

  private String license;

  private OsmUser user;

  private OsmPreferences preferences;

  private OsmGpxFile gpxFile;

  private OsmApi api;

  private OsmChangeset changeset;

  private final List<OsmNode> nodes = new ArrayList<>();

  private final List<OsmWay> ways = new ArrayList<>();

  private final List<OsmRelation> relations = new ArrayList<>();

  private BoundingBox bounds;

  private final LongHashMap<OsmNode> nodeMap = new LongHashMap<>();

  private final LongHashMap<Point> nodePointMap = new LongHashMap<>();

  private final LongHashMap<OsmWay> wayMap = new LongHashMap<>();

  private final LongHashMap<OsmRelation> relationMap = new LongHashMap<>();

  private final List<OsmElement> records = new ArrayList<>();

  private String attribution;

  public OsmDocument() {
  }

  public OsmDocument(final Resource resource) {
    System.out.println(resource);
    final XMLStreamReader xmlReader = StaxUtils.createXmlReader(resource);
    parseDocument(xmlReader);
  }

  public void addNode(final OsmNode node) {
    if (node != null) {
      final long id = node.getId();
      final Point point = node.getGeometryValue();
      this.nodePointMap.put(id, point);

      if (node.isTagged()) {
        this.nodeMap.put(id, node);
        this.nodes.add(node);
        this.records.add(node);
      }
    }
  }

  public void addRelation(final OsmRelation relation) {
    if (relation != null) {
      final long id = relation.getId();
      this.relationMap.put(id, relation);
      if (relation.isTagged()) {
        this.relations.add(relation);
        this.records.add(relation);
      }
    }
  }

  public void addRelations(final List<OsmRelation> relations) {
    for (final OsmRelation relation : relations) {
      addRelation(relation);
    }
  }

  public void addWay(final OsmWay way) {
    final long id = way.getId();
    this.wayMap.put(id, way);
    if (way.isTagged()) {
      this.ways.add(way);
      this.records.add(way);
    }
  }

  public void addWays(final List<OsmWay> ways) {
    for (final OsmWay way : ways) {
      addWay(way);
    }
  }

  public OsmApi getApi() {
    return this.api;
  }

  public String getAttribution() {
    return this.attribution;
  }

  public BoundingBox getBounds() {
    return this.bounds;
  }

  public OsmChangeset getChangeset() {
    return this.changeset;
  }

  public String getCopyright() {
    return this.copyright;
  }

  public String getGenerator() {
    return this.generator;
  }

  public OsmGpxFile getGpxFile() {
    return this.gpxFile;
  }

  public String getLicense() {
    return this.license;
  }

  public OsmNode getNode(final long id) {
    return this.nodeMap.get(id);
  }

  public Point getNodePoint(final long id) {
    return this.nodePointMap.get(id);
  }

  public List<OsmNode> getNodes() {
    return this.nodes;
  }

  public OsmPreferences getPreferences() {
    return this.preferences;
  }

  public OsmElement getRecord(final Identifier identifier) {
    if (identifier instanceof OsmNodeIdentifier) {
      final Long id = identifier.getLong(0);
      return getNode(id);
    } else if (identifier instanceof OsmWayIdentifier) {
      final Long id = identifier.getLong(0);
      return getWay(id);
    } else if (identifier instanceof OsmRelationIdentifier) {
      final Long id = identifier.getLong(0);
      return getRelation(id);
    } else {
      return null;
    }
  }

  public List<OsmElement> getRecords() {
    return this.records;
  }

  public OsmRelation getRelation(final long id) {
    return this.relationMap.get(id);
  }

  public List<OsmRelation> getRelations() {
    return this.relations;
  }

  public OsmUser getUser() {
    return this.user;
  }

  public String getVersion() {
    return this.version;
  }

  public OsmWay getWay(final long id) {
    return this.wayMap.get(id);
  }

  public List<OsmWay> getWays() {
    return this.ways;
  }

  private void parseBounds(final XMLStreamReader in) {
    final double minX = StaxUtils.getDoubleAttribute(in, null, "minlon");
    final double minY = StaxUtils.getDoubleAttribute(in, null, "minlat");
    final double maxX = StaxUtils.getDoubleAttribute(in, null, "maxlon");
    final double maxY = StaxUtils.getDoubleAttribute(in, null, "maxlat");
    final BoundingBoxDoubleGf boundingBox = new BoundingBoxDoubleGf(WGS84_2D,
      2, minX, minY, maxX, maxY);
    setBounds(boundingBox);
    StaxUtils.skipSubTree(in);
  }

  private void parseDocument(final XMLStreamReader in) {
    if (StaxUtils.skipToStartElement(in, OsmConstants.OSM)) {
      for (final String fieldName : Arrays.asList("version", "generator",
        "copyright", "attribution", "license")) {
        final String value = in.getAttributeValue(null, fieldName);
        Property.set(this, fieldName, value);
      }
      while (StaxUtils.skipToChildStartElements(in, OSM_XML_ELEMENTS)) {
        final QName name = in.getName();
        if (name.equals(BOUNDS)) {
          parseBounds(in);
        } else if (name.equals(NODE)) {
          parseNode(in);
        } else if (name.equals(WAY)) {
          parseWay(in);
        } else if (name.equals(RELATION)) {
          parseRelation(in);
        } else {
          StaxUtils.skipSubTree(in);
        }
      }
    } else {
      throw new RuntimeException("Document does not contain an <osm> element");
    }
  }

  private void parseNode(final XMLStreamReader in) {
    final OsmNode node = new OsmNode(in);
    addNode(node);
  }

  private void parseRelation(final XMLStreamReader in) {
    final OsmRelation relation = new OsmRelation(in);
    addRelation(relation);
  }

  private void parseWay(final XMLStreamReader in) {
    final OsmWay way = new OsmWay(this, in);
    addWay(way);
  }

  public void setApi(final OsmApi api) {
    this.api = api;
  }

  public void setAttribution(final String attribution) {
    this.attribution = attribution;
  }

  public void setBounds(final BoundingBox bounds) {
    this.bounds = bounds;
  }

  public void setChangeset(final OsmChangeset changeset) {
    this.changeset = changeset;
  }

  public void setCopyright(final String copyright) {
    this.copyright = copyright;
  }

  public void setGenerator(final String generator) {
    this.generator = generator;
  }

  public void setGpxFile(final OsmGpxFile gpxFile) {
    this.gpxFile = gpxFile;
  }

  public void setLicense(final String license) {
    this.license = license;
  }

  public void setNodes(final List<OsmNode> nodes) {
    this.nodes.clear();
    for (final OsmNode node : nodes) {
      addNode(node);
    }
  }

  public void setPreferences(final OsmPreferences preferences) {
    this.preferences = preferences;
  }

  public void setRelations(final List<OsmRelation> relations) {
    this.relations.clear();
    addRelations(relations);
  }

  public void setUser(final OsmUser user) {
    this.user = user;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public void setWays(final List<OsmWay> ways) {
    this.ways.clear();
    addWays(ways);
  }
}
