package net.von_gagern.martin.cetm.conformal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.von_gagern.martin.cetm.mesh.AbstractMeshIterator;
import net.von_gagern.martin.cetm.mesh.CorneredTriangle;
import net.von_gagern.martin.cetm.mesh.LocatedMesh;
import net.von_gagern.martin.cetm.mesh.MeshIterator;
import net.von_gagern.martin.cetm.mesh.SimpleTriangle;

class ResultMesh<V> implements LocatedMesh<V> {

    private final InternalMesh<V> internal;

    private final Map<V, Vertex> vm;

    public ResultMesh(InternalMesh<V> internal) {
        this.internal = internal;
        vm = internal.getVertexMap();
    }

    public double getX(V v) {
        return internal.getX(vm.get(v));
    }

    public double getY(V v) {
        return internal.getY(vm.get(v));
    }

    public double getZ(V v) {
        return internal.getZ(vm.get(v));
    }

    public double edgeLength(V v1, V v2) {
        return internal.edgeLength(vm.get(v1), vm.get(v2));
    }

    public MeshIterator<V> iterator() {
        return new Iter();
    }

    private V getRep(Vertex v) {
        return (V)v.getRep();
    }

    private class Iter extends AbstractMeshIterator<V> {

        Iterator<? extends CorneredTriangle<? extends Vertex>> i =
            internal.iterator();

        public boolean hasNext() {
            return i.hasNext();
        }

        public SimpleTriangle<V> next() {
            CorneredTriangle<? extends Vertex> t = i.next();
            return new SimpleTriangle<V>(getRep(t.getCorner(0)),
                                         getRep(t.getCorner(1)),
                                         getRep(t.getCorner(2)));
        }

    }

}
