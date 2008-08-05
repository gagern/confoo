package net.von_gagern.martin.confoo.conformal;

/**
 * Boundary condition preserving lengths of boundary edges.<p>
 *
 * All boundary and corner vertices will be fixed in order to keep the
 * lengths of boundary edges unmodified. Internal vertices will be
 * assigned a target angle sum of 2&#960;, resulting in a flat mesh.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class IsometricBoundaryCondition<C> extends BoundaryCondition<C> {

    @Override public void setTargets(InternalMesh<C> mesh) {
        for (Vertex v: mesh.getVertices()) {
            switch (v.kind) {
            case CORNER:
            case BOUNDARY:
                v.fixed = true;
                break;
            case INTERIOR:
                v.target = 2*Math.PI;
                break;
            }
        }
    }

    @Override public boolean fixedScale() {
        return true;
    }

}
