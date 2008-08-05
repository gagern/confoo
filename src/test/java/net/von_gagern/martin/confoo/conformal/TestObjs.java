package net.von_gagern.martin.confoo.conformal;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;

public class TestObjs extends AbstractTestCase {

    private final Logger logger = Logger.getLogger(TestObjs.class);

    private LocatedMesh<Integer>
        runWithFixedBoundary(String objName, double... angles)
        throws MeshException, IOException
    {
        Conformal<Integer> c = conformalWithFixedBoundary(objName, angles);
        try {
            LocatedMesh<Integer> res = c.transform();
            logResult(res);
            return res;
        }
        catch (MeshException e) {
            logger.error(e.toString());
            throw e;
        }
    }

    private double normalizeAngle(double a) {
        while (a > Math.PI) a -= 2*Math.PI;
        while (a <= -Math.PI) a += Math.PI;
        return a;
    }

    private void assertAngle(LocatedMesh<Integer> mesh,
                             int v1, int v2, int v3,
                             double expected, double tolerance) {
        double x1 = mesh.getX(v1), y1 = mesh.getY(v1), z1 = mesh.getZ(v1);
        double x2 = mesh.getX(v2), y2 = mesh.getY(v2), z2 = mesh.getZ(v2);
        double x3 = mesh.getX(v3), y3 = mesh.getY(v3), z3 = mesh.getZ(v3);
        assertEquals("Non-planar mesh", 0, z1, 0.);
        assertEquals("Non-planar mesh", 0, z2, 0.);
        assertEquals("Non-planar mesh", 0, z3, 0.);
        double a1 = Math.atan2(y1 - y2, x1 - x2);
        double a2 = Math.atan2(y3 - y2, x3 - x2);
        double actual = normalizeAngle(a2 - a1);
        logger.debug("Angle v" + v1 + "-v" + v2 + "-v" + v3 +
                     " is " + actual/DEG + " degrees");
        double error = normalizeAngle(actual - expected*DEG);
        if (error > tolerance*DEG)
            assertEquals("Angle v" + v1 + "-v" + v2 + "-v" + v3,
                         expected, actual/DEG, tolerance);
    }

    private void assertAngle(LocatedMesh<Integer> mesh,
                             int v1, int v2, int v3, double expected) {
        assertAngle(mesh, v1, v2, v3, expected, angleTolerance);
    }

    @Test public void testOneRightIsosceles()
        throws MeshException, IOException
    {
        LocatedMesh<Integer> m = runWithFixedBoundary("oneRightIsosceles.obj", 60., 60., 60.);
        assertAngle(m, 2, 1, 3, 60.);
        assertAngle(m, 3, 2, 1, 60.);
        assertAngle(m, 1, 3, 2, 60.);
        assertEquals("Origin", 0., m.getX(1), 0.);
        assertEquals("Origin", 0., m.getY(1), 0.);
        assertTrue("Positive X", m.getX(2) > 0.);
        assertEquals("X-Axis", 0., m.getY(2), 0.);
        assertTrue("Positive Y", m.getY(3) > 0.);
    }

    @Test public void test1Lengths() throws MeshException, IOException {
        Conformal<Integer> c;
        c = conformalWithFixedBoundary("test1.obj", 90., 90., 90., 90.);
        LocatedMesh<Integer> m = c.transform();
        assertAngle(m, 2, 1, 4, 90.);
        assertAngle(m, 3, 2, 1, 90.);
        assertAngle(m, 4, 3, 2, 90.);
        assertAngle(m, 1, 4, 3, 90.);
        checkEdgeLengths(c.getInternalMesh());
    }

    @Test public void test1Coordinates() throws MeshException, IOException {
        LocatedMesh<Integer> m = runWithFixedBoundary("test1.obj", 90., 90., 90., 90.);
        AffineTransform t = transformToUnitBox(m, 1, 2, 4);
        Point2D.Double p = new Point2D.Double();
        for (int i = 1; i <= 9; ++i) {
            p.setLocation(m.getX(i), m.getY(i));
            t.transform(p, p);
            double x = p.getX();
            double y = p.getY();
            switch (i) {
            case 1:
                assertEquals("x1", 0., x, lengthTolerance);
                assertEquals("y1", 0., y, lengthTolerance);
                break;
            case 2:
                assertEquals("x2", 1., x, lengthTolerance);
                assertEquals("y2", 0., y, lengthTolerance);
                break;
            case 3:
                assertEquals("x3", 1., x, lengthTolerance);
                assertEquals("y3", 1., y, lengthTolerance);
                break;
            case 4:
                assertEquals("x4", 0., x, lengthTolerance);
                assertEquals("y4", 1., y, lengthTolerance);
                break;
            case 5:
                assertRange("x5", 0., 1., x);
                assertEquals("y5", 0., y, lengthTolerance);
                break;
            case 6:
                assertEquals("x6", 1., x, lengthTolerance);
                assertRange("y6", 0., 1., y);
                break;
            case 7:
                assertRange("x7", 0., 1., x);
                assertEquals("y7", 1., y, lengthTolerance);
                break;
            case 8:
                assertEquals("x8", 0., x, lengthTolerance);
                assertRange("y8", 0., 1., y);
                break;
            case 9:
                assertRange("x9", 0., 1., x);
                assertRange("y9", 0., 1., y);
                break;
            }
        }
    }

    @Test public void test1Isometric() throws MeshException, IOException {
        LocatedMesh<Integer> in = objResource("test1.obj");
        Conformal<Integer> c = Conformal.getInstance(in);
        c.isometricBoundaryCondition();
        LocatedMesh<Integer> out = c.transform();
        checkEdgeLengths(c.getInternalMesh());
        for (Edge e: c.getInternalMesh().getEdges()) {
            Integer v1 = (Integer)e.v1.rep, v2 = (Integer)e.v2.rep;
            assertEquals("Edge " + e, in.edgeLength(v1, v2),
                         out.edgeLength(v1, v2), lengthTolerance);
        }
    }

    private void assertRange(String msg, double min, double max,
                             double actual) {
        if (Double.isNaN(actual) || actual < min || actual > max)
            fail (msg + ": Expected " + min + " <= " + actual + " <= " + max);
    }

    private <V> AffineTransform transformToUnitBox(LocatedMesh<V> m,
                                               V... unitPoints) {
        double x1 = m.getX(unitPoints[0]), y1 = m.getY(unitPoints[0]);
        double x2 = m.getX(unitPoints[1]), y2 = m.getY(unitPoints[1]);
        double x3 = m.getX(unitPoints[2]), y3 = m.getY(unitPoints[2]);
        double det = x1*y2 + x2*y3 + x3*y1 - x1*y3 - x2*y1 - x3*y2;
        return new AffineTransform((y3-y1)/det, (y1-y2)/det,
                                   (x1-x3)/det, (x2-x1)/det,
                                   (x3*y1-x1*y3)/det, (x1*y2-x2*y1)/det);
    }

}
