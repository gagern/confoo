package net.von_gagern.martin.cetm.conformal;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.cetm.mesh.LocatedMesh;
import net.von_gagern.martin.cetm.mesh.MeshException;

public class TestObjs extends AbstractTestCase {

    private Logger logger = Logger.getLogger(TestObjs.class);

    private Conformal<Integer>
        runWithFixedBoundary(String objName, double... angles)
        throws MeshException, IOException
    {
        Conformal<Integer> c = conformalWithFixedBoundary(objName, angles);
        try {
            c.transform();
        }
        catch (MeshException e) {
            logger.error(e.toString());
            throw e;
        }
        logResult(c);
        return c;
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
        if (error > tolerance)
            assertEquals("Angle v" + v1 + "-v" + v2 + "-v" + v3,
                         expected, actual/DEG, tolerance/DEG);
    }

    private void assertAngle(LocatedMesh<Integer> mesh,
                             int v1, int v2, int v3, double expected) {
        assertAngle(mesh, v1, v2, v3, expected, 0.5*DEG);
    }

    @Test public void testOneRightIsosceles()
        throws MeshException, IOException
    {
        Conformal<Integer> c;
        c = runWithFixedBoundary("oneRightIsosceles.obj", 60., 60., 60.);
        LocatedMesh<Integer> m = c.getMesh();
        assertAngle(m, 2, 1, 3, 60.);
        assertAngle(m, 3, 2, 1, 60.);
        assertAngle(m, 1, 3, 2, 60.);
        assertEquals("Origin", 0., m.getX(1), 0.);
        assertEquals("Origin", 0., m.getY(1), 0.);
        assertTrue("Positive X", m.getX(2) > 0.);
        assertEquals("X-Axis", 0., m.getY(2), 0.);
        assertTrue("Positive Y", m.getY(3) > 0.);
    }

    @Test public void testHyp() throws MeshException, IOException {
        runWithFixedBoundary("hyp.obj", 60., 60., 60.);
    }

}
