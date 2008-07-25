package net.von_gagern.martin.cetm.conformal;

import net.von_gagern.martin.cetm.mesh.MeshException;

abstract class BoundaryCondition<C> {

    public abstract void setTargets(InternalMesh<C> mesh) throws MeshException;

    public boolean fixedScale() {
        return false;
    }

}
