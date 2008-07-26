package net.von_gagern.martin.cetm.conformal;

import java.util.List;
import java.util.Map;
import net.von_gagern.martin.cetm.mesh.LocatedMesh;
import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.MetricMesh;
import net.von_gagern.martin.cetm.opt.Newton;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;

public class Conformal<V> implements Runnable {

    private InternalMesh<V> mesh;

    private BoundaryCondition boundaryCondition;

    private MeshException error;

    private Conformal(MetricMesh<V> mesh) throws MeshException {
        this.mesh = new InternalMesh(mesh);
    }

    public static <V> Conformal<V> getInstance(MetricMesh<V> mesh)
        throws MeshException
    {
        return new Conformal<V>(mesh);
    }

    /*********************************************************************
     * Configuration
     ********************************************************************/

    public void fixedBoundaryCurvature(Map<? extends V, Double> angles) {
        boundaryCondition = new FixedBoundaryCurvature<V>(angles,
                                                          mesh.getVertexMap());
    }

    /*********************************************************************
     * Calculate conformal mapping
     ********************************************************************/

    public void transform() throws MeshException {
        boundary();
        adjust();
        scale();
        layout();
    }

    private void boundary() throws MeshException {
        if (boundaryCondition == null)
            throw new IllegalStateException("No boundary condition set");
        boundaryCondition.setTargets(mesh);
    }

    private void adjust() throws MeshException {
        Energy energy = new Energy(mesh);
        Newton newton = Newton.getInstance(energy);
        try {
            newton.optimize();
        }
        catch (IterativeSolverNotConvergedException e) {
            throw new MeshException("Could not find optimal solution: " +
                                    e.getReason(), e);
        }
    }

    private void scale() {
        if (boundaryCondition.fixedScale()) return;
        List<Vertex> vs = mesh.getVertices();
        double sum = 0;
        for (Vertex v: vs)
            sum += v.getU();
        double diff = -sum/vs.size();
        for (Vertex v: vs)
            v.setU(v.getU() + diff);
        for (Edge e: mesh.getEdges())
            e.update();
        for (Angle a: mesh.getAngles())
            a.update();
    }

    private void layout() throws MeshException {
        Layout layout = new Layout(mesh);
        layout.layout();
    }

    /*********************************************************************
     * Runnable interface and error handling
     ********************************************************************/

    public void run() {
        try {
            transform();
        }
        catch (MeshException e) {
            error = e;
        }
    }

    public void throwInterceptedError() throws MeshException {
        MeshException error = this.error;
        if (error == null) return;
        this.error = null;
        throw error;
    }

    public Throwable getError() {
        return error;
    }

    public void clearError() {
        error = null;
    }

    /*********************************************************************
     * Access to result
     ********************************************************************/

    public LocatedMesh<V> getMesh() {
        return new ResultMesh<V>(mesh);
    }

}
