package net.von_gagern.martin.cetm.mesh;

/**
 * A mesh with edge length information.<p>
 *
 * This representation of a mesh conveys distance information along
 * with the combinatorics of the mesh. It is suitably general to work
 * for different geometries.<p>
 *
 * @param <V> the class used to represent vertices of this mesh
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public interface MetricMesh<V> extends CombinatoricMesh<V> {

    /**
     * Determine distance between adjacent vertices of the mesh.<p>
     *
     * The returned length need not necessarily be equal to the
     * euclidean length as determined from the vertex coordinates,
     * if such coordinates are known in the first place.<p>
     *
     * An implementation may throw any convenient
     * {@link RuntimeException} if the vertices are not adjacent, or
     * not part of this mesh, but it is not required to do so. The
     * order of the arguments should be irrelevant.
     *
     * @param v1 one vertex of an edge
     * @param v2 a second vertex of the same edge
     * @return the distance between the two vertices
     * @throws RuntimeException if the vertices are not adjacent
     */
    public double edgeLength(V v1, V v2);

}
