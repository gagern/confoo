package net.von_gagern.martin.cetm.mesh;

import java.util.Arrays;
import java.util.List;

public class TriangleInequalityException
    extends MeshException implements CorneredTriangle<Object>
{

    private List<Object> corners;

    public TriangleInequalityException(Object rep1, Object rep2, Object rep3) {
        super("Result violates triangle inequality");
        corners = Arrays.asList(rep1, rep2, rep3);
    }

    public Object getCorner(int i) {
        return corners.get(i);
    }

}
