package net.von_gagern.martin.cetm.conformal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.ObjFormat;

public class FlattenObj {

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
        c.transform();
        ObjFormat outObj = new ObjFormat(c.getMesh());
        FileOutputStream outStream = new FileOutputStream(outFile);
        outObj.write(outStream);
        outStream.close();
    }

}
