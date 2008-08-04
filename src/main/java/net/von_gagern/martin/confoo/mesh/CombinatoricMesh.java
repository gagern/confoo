package net.von_gagern.martin.confoo.mesh;

import java.util.Iterator;

/**
 * The most basic kind of mesh representing only its combinatorics.<p>
 *
 * The mesh can be traversed by iterating over its triangles. Vertices
 * should be compared using {@link Object#equals(Object) V.equals},
 * not object identity. Triangles should be oriented consistently.<p>
 *
 * Due to the laxer type requirements of this iterator method, the
 * interface does not directly extend {@link Iterable}. Implementing
 * classes are encouraged to also to implement <code>Iterable</code>.<p>
 *
 * Meshes that can provide more inforation than the simple
 * combinatorics should implement one of the derived interfaces.<p>
 *
 * @param <V> the class used to represent vertices of this mesh
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public interface CombinatoricMesh<V> {

    /**
     * Return an iterator over the triangles of this mesh.
     *
     * @return an iterator over the triangles of this mesh
     * @see Iterable#iterator()
     * @see MeshIterator
     */
    public Iterator<? extends CorneredTriangle<? extends V>> iterator();

}
