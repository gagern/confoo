package net.von_gagern.martin.confoo.conformal;

/**
 * Unordered vertex pair.
 * This is the raw form of an edge: a pair of vertices with no
 * additional ionformation about the edge itself. It is used as a key
 * to identify edge objects. It provides a suitable equality
 * comparison.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class VertexPair {

    /**
     * The first vertex.
     */
    private Vertex v1;

    /**
     * The second vertex.
     */
    private Vertex v2;

    /**
     * Construct new vertex pair.
     * @param v1 the first vertex
     * @param v2 the second vertex
     */
    public VertexPair(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Calculate hash code for vertex pair.
     * @return a hash code for the vertex pair
     */
    @Override public int hashCode() {
        return v1.hashCode() ^ v2.hashCode();
    }

    /**
     * Determine whether this object is equal to another object.
     * To be equal, the other object has to be a vertex pair as well,
     * and the sets of vertices of both pairs have to be equal,
     * without taking vertex order into account.
     * @param o an object to be compared to this one
     * @return whether the object represents a pair of the same two
     *         vertices
     */
    @Override public boolean equals(Object o) {
        if (!(o instanceof VertexPair)) return false;
        VertexPair that = (VertexPair)o;
        return (this.v1.equals(that.v1) && this.v2.equals(that.v2)) ||
               (this.v1.equals(that.v2) && this.v2.equals(that.v1));
    }

}
