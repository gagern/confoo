package net.von_gagern.martin.cetm.conformal;

import java.util.List;
import java.util.Map;

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
            switch (v.getKind()) {
            case CORNER:
                v.setTarget(Math.PI/2.);
                break;
            case BOUNDARY:
                v.setTarget(Math.PI);
                break;
            case INTERIOR:
                v.setTarget(2*Math.PI);
                break;
            }
        }

        // honour explicitely set angles
        for (Map.Entry<? extends C, Double> entry: angles.entrySet()) {
            vm.get(entry.getKey()).setTarget(entry.getValue());
        }

        // fix a single arbitrary vertex to fix scale:
        List<Vertex> vs = mesh.getVertices();
        vs.get(vs.size()/2).setFixed(true);
    }

}
