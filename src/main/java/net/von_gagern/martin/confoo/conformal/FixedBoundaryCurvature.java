package net.von_gagern.martin.confoo.conformal;

import java.util.List;
import java.util.Map;

/**
 * Boundary condition with fixed boundary curvature.<p>
 *
 * Selected corners may be given in form of a map from original input
 * vertex representants to angles measured in radians. These angles
 * will be set as target values for the corresponding internal
 * vertices.<p>

 * For vertices not present in the map, the defauls will be based upon
 * vertex kind. Interior vertices will be assigned a target angle of
 * 2&#960;, boundary vertices (incident with both boundary and
 * internal edges) &#960; and corner vertices (incident to boundary
 * edges only) &#960;/2.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class FixedBoundaryCurvature<C> extends BoundaryCondition<C> {

    private Map<? extends C, Double> angles;

    private Map<C, Vertex> vm;

    public FixedBoundaryCurvature(Map<? extends C, Double> angles,
                                  Map<C, Vertex> vm) {
        this.angles = angles;
        this.vm = vm;
    }

    public void setTargets(InternalMesh<C> mesh) {
        // initialize target angles
        for (Vertex v: mesh.getVertices()) {
            switch (v.kind) {
            case CORNER:
                v.target = Math.PI/2.;
                break;
            case BOUNDARY:
                v.target = Math.PI;
                break;
            case INTERIOR:
                v.target = 2*Math.PI;
                break;
            }
        }

        // honour explicitely set angles
        for (Map.Entry<? extends C, Double> entry: angles.entrySet()) {
            vm.get(entry.getKey()).target = entry.getValue();
        }

        // fix a single arbitrary vertex to fix scale:
        List<Vertex> vs = mesh.getVertices();
        vs.get(vs.size()/2).fixed = true;
    }

}
