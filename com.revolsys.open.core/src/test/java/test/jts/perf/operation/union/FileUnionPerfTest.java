package test.jts.perf.operation.union;

import java.util.List;

import test.jts.TestFiles;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.io.WKTWriter;

public class FileUnionPerfTest {
  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();

  static GeometryFactory fact = new GeometryFactory(pm, 0);

  static WKTReader wktRdr = new WKTReader(fact);

  static WKTWriter wktWriter = new WKTWriter();

  public static void main(final String[] args) {
    final FileUnionPerfTest test = new FileUnionPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  GeometryFactory factory = GeometryFactory.getFactory();

  boolean testFailed = false;

  public FileUnionPerfTest() {
  }

  public void test() throws Exception {

    // test(TestFiles.DATA_DIR + "africa.wkt");
    // test(TestFiles.DATA_DIR + "world.wkt");
    // test(TestFiles.DATA_DIR + "bc-250k.wkt");
    // test(TestFiles.DATA_DIR + "bc_20K.wkt");

    // test("C:\\data\\martin\\proj\\jts\\data\\veg.wkt");

    test(TestFiles.DATA_DIR + "africa.wkt");
    // test(TestFiles.DATA_DIR + "world.wkt");
    // test("C:\\proj\\JTS\\test\\union\\npsa_albers.wkt");

  }

  public void test(final String filename) throws Exception {
    final WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    final List polys = fileRdr.read();

    final UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }

}
