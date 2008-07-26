package net.von_gagern.martin.cetm.conformal;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.log4j.Logger;

import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.ObjFormat;

abstract class AbstractTestCase {

    public static final double DEG = Math.PI/180.;

    protected int maxOutLen = 32*1024;

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

    protected void logResult(Conformal c) throws IOException {
        Logger logger = Logger.getLogger(AbstractTestCase.class);
        if (!logger.isDebugEnabled()) return;
        ObjFormat obj = new ObjFormat(c.getMesh());
        CharArrayWriter w = new CharArrayWriter();
        w.write("Resulting obj mesh:\n");
        obj.write(w);
        String str = w.toString();
        if (str.length() <= maxOutLen)
            logger.debug(str);
    }

}
