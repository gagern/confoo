package net.von_gagern.martin.confoo.conformal;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;

/**
 * Internal triangle representation.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class Triangle implements CorneredTriangle<Vertex> {

    /**
     * List of the three angles of a triangle.
     */
    private List<Angle> as;

    /**
     * Flag used to mark visited triangles during graph traversal.
     */
    private boolean iterFlag;

    /**
     * Construct a triangle.
     */
    public Triangle() {
    }

    /**
     * Set the angles of the triangle.
     * @param a1 the first corner angle
     * @param a2 the second corner angle
     * @param a3 the third corner angle
     */
    public void setAngles(Angle a1, Angle a2, Angle a3) {
        as = Arrays.asList(a1, a2, a3);
    }

    /**
     * Get specified corner of the triangle.
     * @param i the index of the corner
     * @return the requested corner
     * @throws IndexOutOfBoundsException unless 0 &lt;= i &lt; 3
     */
    public Vertex getCorner(int i) {
        return as.get(i).vertex;
    }

    /**
     * Get list of all vertices.
     * @return a list of the three vertices of the triangle
     */
    public List<Vertex> getVertices() {
        return new VertexList();
    }

    /**
     * Get list of all edges.
     * The edges will be ordered such that
     * <code>getEdges().get(i)</code> is the edge <em>opposite</em> to
     * <code>getVertices().get(i)</code>.
     * @return a list of the three edges of the triangle
     */
    public List<Edge> getEdges() {
        return new EdgeList();
    }

    /**
     * Get list of all angles.
     * The angles will be ordered such that
     * <code>getVertices().get(i).vertex()</code> equals
     * <code>getVertices().get(i)</code>.
     * @return a list of the three angles of the triangle
     */
    public List<Angle> getAngles() {
        return as;
    }

    /**
     * Get iteration flag. This flag can be used to mark visited
     * triangles during graph traversal.
     * @return the current value of the flag
     * @see #setIterFlag()
     * @see #clearIterFlag()
     */
    public boolean getIterFlag() {
        return iterFlag;
    }

    /**
     * Set iteration flag.
     * @see #getIterFlag()
     * @see #clearIterFlag()
     */
    public void setIterFlag() {
        iterFlag = true;
    }

    /**
     * Clear iteration flag.
     * @see #getIterFlag()
     * @see #setIterFlag()
     */
    public void clearIterFlag() {
        iterFlag = false;
    }

    /**
     * Determine whether the triangle lies on the boundary.
     * @return whether the triangle lies on the boundary
     */
    public boolean isBoundary() {
        for (Edge e: getEdges())
            if (e.getOtherTriangle(this) == null)
                return true;
        return false;
    }

    /**
     * Get the vertex opposite a given edge.
     * @param e an edge of the triangle
     * @return the vertex opposite <code>e</code>
     * @throws IllegalArgumentException if <code>e</code> is not an
     *         edge of the triangle
     */
    public Vertex getOppositeVertex(Edge e) {
        for (Angle a: as)
            if (a.oppositeEdge == e)
                return a.vertex;
        throw new IllegalArgumentException("given edge not part of triangle");
    }

    /**
     * Get the angle at the vertex following a given vertex.
     * So in a triangle ABC for input vertex A this method would
     * return the angle CBA, i.e. the anglke centered at B.
     * @param v a vertex of the triangle
     * @return the angle centered at the vertex following
     *         <code>v</code>
     * @throws IllegalArgumentException if <code>e</code> is not a
     *         vertex of the triangle
     */
    public Angle getNextAngle(Vertex v) {
        for (Angle a: as)
            if (a.prevVertex == v)
                return a;
        throw new IllegalArgumentException("given vertex not part of triangle");
    }

    /**
     * Get the angle at the preceding following a given vertex.
     * So in a triangle ABC for input vertex A this method would
     * return the angle CAB, i.e. the anglke centered at C.
     * @param v a vertex of the triangle
     * @return the angle centered at the vertex preceding
     *         <code>v</code>
     * @throws IllegalArgumentException if <code>e</code> is not a
     *         vertex of the triangle
     */
    public Angle getPrevAngle(Vertex v) {
        for (Angle a: as)
            if (a.nextVertex == v)
                return a;
        throw new IllegalArgumentException("given vertex not part of triangle");
    }

    /**
     * List adapter to turn angle list into edge list.
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
    private class EdgeList extends AbstractList<Edge> {

        /**
         * Get the edge opposite the angle with the given index.
         * @param index the index of the angle
         * @return the edge opposite that angle
         */
        public Edge get(int index) {
            return as.get(index).oppositeEdge;
        }

        /**
         * Return the size of the wrapped angle list.
         * @return the size of the angle list
         */
        public int size() {
            return as.size();
        }

    }

    /**
     * List adapter to turn angle list into vertex list.
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
    private class VertexList extends AbstractList<Vertex> {

        /**
         * Get the edge vertex at the center of the angle with the
         * given index.
         * @param index the index of the angle
         * @return the vertex at the center of that angle
         */
        public Vertex get(int index) {
            return as.get(index).vertex;
        }

        /**
         * Return the size of the wrapped angle list.
         * @return the size of the angle list
         */
        public int size() {
            return as.size();
        }

    }

    /**
     * Build string representation of triangle from representations of
     * the underlying vertex objects of the input mesh.
     * @return a string describing the triangle
     */
    @Override public String toString() {
        StringBuilder buf = new StringBuilder("Triangle(");
        buf.append(as.get(0).vertex.rep.toString()).append(", ");
        buf.append(as.get(1).vertex.rep.toString()).append(", ");
        buf.append(as.get(2).vertex.rep.toString()).append(")");
        return buf.toString();
    }

}
