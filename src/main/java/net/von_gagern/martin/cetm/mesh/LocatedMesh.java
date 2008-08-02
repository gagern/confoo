package net.von_gagern.martin.cetm.mesh;

/**
 * A mesh with 3D vertex positions.<p>
 *
 * This is probably the most common kind of mesh, with explicit
 * coordinates given for the three corners of each triangle. Still
 * keep in mind that vertices are compared using
 * {@link Object#equals(Object) V.equals}, so the same coordinates may
 * represent different vertices if they don't compare equal, and
 * different vertex objects may represent the same vertex if they do
 * compare equal.<p>
 *
 * This interface is also used for flat 2D meshes, where the <i>z</i>
 * coordinate is always fixed to zero.<p>
 *
 * It is permissible for mesh transformations to use the same objects
 * to represent the combinatorics of the mesh, but associate different
 * coordinates with the vertices. Therefore this interface assumes the
 * mesh knows about coordinates, not necessarily the vertex objects. A
 * conforming implementation should avoid relying on coordinates
 * provided directly by the vertex objects.<p>
 *
 * @param <V> the class used to represent vertices of this mesh
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public interface LocatedMesh<V> extends MetricMesh<V> {

    /**
     * Determine x coordinate of a vertex.<p>
     *
     * An implementation may throw any convenient
     * {@link RuntimeException} if the vertex is not part of this
     * mesh, but it is not required to do so.
     *
     * @param v a vertex of the mesh
     * @return the x coordinate of that vertex
     */
    public double getX(V v);

    /**
     * Determine y coordinate of a vertex.<p>
     *
     * An implementation may throw any convenient
     * {@link RuntimeException} if the vertex is not part of this
     * mesh, but it is not required to do so.
     *
     * @param v a vertex of the mesh
     * @return the y coordinate of that vertex
     */
    public double getY(V v);

    /**
     * Determine z coordinate of a vertex.
     * For 2D meshes this should always be zero.<p>
     *
     * An implementation may throw any convenient
     * {@link RuntimeException} if the vertex is not part of this
     * mesh, but it is not required to do so.
     *
     * @param v a vertex of the mesh
     * @return the z coordinate of that vertex
     */
    public double getZ(V v);

}
