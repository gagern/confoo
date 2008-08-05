package net.von_gagern.martin.confoo.conformal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;

import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.ObjFormat;

abstract class AbstractTestCase {

    public static final double DEG = Math.PI/180.;

    protected int maxOutLen = 32*1024;

    protected double angleTolerance = 1/3600.;

    protected double lengthTolerance = 1e-12;

    protected Conformal<Integer>
        conformalWithFixedBoundary(String objName, double... angles)
        throws MeshException, IOException
    {
        Conformal<Integer> c = conformal(objName);
        c.fixedBoundaryCurvature(angleMap(angles));
        return c;
    }


    protected ObjFormat objResource(String resourceName) throws IOException {
        InputStream stream = getClass().getResourceAsStream(resourceName);
        if (stream == null)
            throw new MissingResourceException(
		"Obj file " + resourceName + "not found",
		getClass().getName(), resourceName);
        ObjFormat obj = new ObjFormat(stream);
        stream.close();
        return obj;
    }

    protected Conformal<Integer> conformal(String objName)
        throws MeshException, IOException
    {
        return Conformal.getInstance(objResource(objName));
    }

    protected Map<Integer, Double> angleMap(double... angles) {
        Map<Integer, Double> am = new HashMap<Integer, Double>(angles.length);
        for (int i = 0; i < angles.length; ++i)
            am.put(i + 1, angles[i]*DEG);
        return am;
    }

    protected void logResult(LocatedMesh<Integer> mesh) throws IOException {
        Logger logger = Logger.getLogger(AbstractTestCase.class);
        if (!logger.isDebugEnabled()) return;
        ObjFormat obj = new ObjFormat(mesh);
        StringBuilder buf = new StringBuilder("Resulting obj mesh:\n");
        obj.write(buf);
        if (buf.length() <= maxOutLen)
            logger.debug(buf.toString());
    }

    protected void checkEdgeLengths(InternalMesh<?> mesh) {
        for (Edge e: mesh.getEdges()) {
            double dx = mesh.getX(e.v1) - mesh.getX(e.v2);
            double dy = mesh.getY(e.v1) - mesh.getY(e.v2);
            double dz = mesh.getZ(e.v1) - mesh.getZ(e.v2);
            double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
            assertEquals(e.length, len, lengthTolerance);
        }
    }
}
