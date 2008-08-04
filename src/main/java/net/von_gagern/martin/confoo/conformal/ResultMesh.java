package net.von_gagern.martin.confoo.conformal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.von_gagern.martin.confoo.mesh.AbstractMeshIterator;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshIterator;
import net.von_gagern.martin.confoo.mesh.SimpleTriangle;

/**
 * Representation of a transformed mesh.
 * This map represents the coordinates of an internal mesh but uses
 * the same objects as the original input mesh to identify vertices.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class ResultMesh<V> implements LocatedMesh<V> {

    /**
     * Internal mesh to be wrapped.
     */
    private final InternalMesh<V> internal;

    /**
     * Vertex map of internal mesh.
     */
    private final Map<V, Vertex> vm;

    /**
     * Construct new result mesh.
     * @param internal the internal mesh to wrap
     */
    public ResultMesh(InternalMesh<V> internal) {
        this.internal = internal;
        vm = internal.getVertexMap();
    }

    /**
     * Get x coordinate of vertex.
     * @param v a vertex of the mesh
     * @return the x coordinate of that vertex
     */
    public double getX(V v) {
        return internal.getX(vm.get(v));
    }

    /**
     * Get y coordinate of vertex.
     * @param v a vertex of the mesh
     * @return the y coordinate of that vertex
     */
    public double getY(V v) {
        return internal.getY(vm.get(v));
    }

    /**
     * Get z coordinate of vertex.
     * @param v a vertex of the mesh
     * @return the z coordinate of that vertex
     */
    public double getZ(V v) {
        return internal.getZ(vm.get(v));
    }

    /**
     * Get edge length.
     * @param v1 one vertex of the mesh
     * @param v2 a vertex adjacent to <code>v1</code>
     * @return the length of the edge between the two vertices
     */
    public double edgeLength(V v1, V v2) {
        return internal.edgeLength(vm.get(v1), vm.get(v2));
    }

    /**
     * Get iterator over all triangles of the mesh.
     */
    public MeshIterator<V> iterator() {
        return new Iter();
    }

    /**
     * Get external representant of an internal vertex.
     * @param v an internal vertex
     * @return the corresponding external representant
     */
    private V getRep(Vertex v) {
        return (V)v.getRep();
    }

    /**
     * Iterator over result triangles.
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
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
