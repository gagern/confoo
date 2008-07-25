package net.von_gagern.martin.cetm.conformal;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.ObjFormat;

public class TestObjs {

    private Logger logger = Logger.getLogger(TestObjs.class);

    private int maxOutLen = 32*1024;

    private void withFixedBoundary(String objName, double... angles)
        throws MeshException, IOException
    {
        logger.info("Running " + objName +
                    " with angles " + Arrays.toString(angles));
        InputStream inStream = getClass().getResourceAsStream(objName);
        if (inStream == null)
            throw new MissingResourceException(
		"Obj file " + objName + "not found",
		getClass().getName(), objName);
        ObjFormat inObj = new ObjFormat(inStream);
        inStream.close();
        Conformal<Integer> c = Conformal.getInstance(inObj);

        Map<Integer, Double> am = new HashMap<Integer, Double>(angles.length);
        for (int i = 0; i < angles.length; ++i)
            am.put(i + 1, Math.PI/180.*angles[i]);
        c.fixedBoundaryCurvature(am);

        c.transform();

        if (logger.isDebugEnabled()) {
            ObjFormat outObj = new ObjFormat(c.getMesh());
            CharArrayWriter w = new CharArrayWriter();
            w.write("Resulting obj mesh:\n");
            outObj.write(w);
            String outStr = w.toString();
            if (outStr.length() <= maxOutLen)
                logger.debug(outStr);
        }
    }

    @Test public void testHyp() throws MeshException, IOException {
        withFixedBoundary("hyp.obj", 60., 60., 60.);
    }

}
