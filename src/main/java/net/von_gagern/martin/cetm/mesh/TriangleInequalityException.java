package net.von_gagern.martin.cetm.mesh;

import java.util.Arrays;
import java.util.List;

/**
 * Violation of triangle inequality.<p>
 *
 * The triangle inequality states that for any three edges <i>a</i>,
 * <i>b</i> and <i>c</i> of a triangle, <i>a</i> <= <i>b</i> +
 * <i>c</i> always holds.<p>
 *
 * Applications relying on the triangle inequality in a
 * <code>MetricMesh</code> should throw this exception if the triangle
 * inequality was violated. Algorithms transforming a mesh may throw
 * it as well if the resulting mesh would violate the triangle
 * inequality.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
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
