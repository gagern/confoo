package net.von_gagern.martin.confoo.conformal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.ObjFormat;

/**
 * Command line interface to flatten a mesh.<p>
 *
 * <b>Usage:</b> <code>java net.von_gagern.martin.confoo.conformal.FlattenObj
 * in.obj out.obj a1 a2 ... an</code><p>
 *
 * The application will read the mesh from <code>in.obj</code>, adjust
 * the angle at the first <i>n</i> vertices to the values given on the
 * command line and interpreted as degrees. Then it will transform the
 * mesh and write the resulting flat mesh to <code>out.obj</code>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class FlattenObj {

    /**
     * Main function of the command line interface.
     * @param args arguments as described above
     */
    public static void main(String[] args) throws MeshException, IOException {
        String inFileName = null, outFileName = null;
        HashMap<Integer, Double> angles = new HashMap<Integer, Double>(4);
        for (String arg: args) {
            if (inFileName == null) {
                inFileName = arg;
            }
            else if (outFileName == null) {
                outFileName = arg;
            }
            else {
                double angle = Double.parseDouble(arg);
                angle *= Math.PI/180.;
                angles.put(angles.size() + 1, angle);
            }
        }
        File inFile = new File(inFileName);
        File outFile = new File(outFileName);
        FileInputStream inStream = new FileInputStream(inFile);
        ObjFormat inObj = new ObjFormat(inStream);
        inStream.close();
        Conformal<Integer> c = Conformal.getInstance(inObj);
        c.fixedBoundaryCurvature(angles);
        ObjFormat outObj = new ObjFormat(c.transform());
        FileOutputStream outStream = new FileOutputStream(outFile);
        outObj.write(outStream);
        outStream.close();
    }

}
