package net.von_gagern.martin.cetm.conformal;

import net.von_gagern.martin.cetm.mesh.MeshException;

/**
 * Abstract base class for boundary conditions.<p>
 *
 * A boundary condition determines target angles for vertices and also
 * declares vertices as fixed if their length factor should not
 * change.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
abstract class BoundaryCondition<C> {

    /**
     * Set target angles and fixed vertices.
     * @param mesh the mesh to work upon
     * @see Vertex#setTarget(double)
     * @see Vertex#setFixed(boolean)
     */
    public abstract void setTargets(InternalMesh<C> mesh) throws MeshException;

    /**
     * Return whether this boundary condition fixes an absolute
     * scale. If this is not the case, the transformation result will
     * be scaled to keep the sum of all the <i>u<sub>i</sub></i> equal
     * to zero.
     * @return whether the scale is fixed by the boundary condition
     */
    public boolean fixedScale() {
        return false;
    }

}
