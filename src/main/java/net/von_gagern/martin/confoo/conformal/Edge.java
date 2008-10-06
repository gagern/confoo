package net.von_gagern.martin.confoo.conformal;

import net.von_gagern.martin.confoo.mesh.MeshException;

/**
 * Internal edge representation.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class Edge {

    /**
     * First endpoint of the edge.
     */
    final Vertex v1;

    /**
     * Second endpoint of the edge.
     */
    final Vertex v2;

    /**
     * First triangle incident to the edge. This is the triangle that
     * has v1 and v2 following one another in this order in its cyclic
     * vertex order.
     */
    final Triangle t1;

    /**
     * Second triangle incident to the edge. This is the triangle that
     * has v2 and v1 following one another in this order in its cyclic
     * vertex order.
     */
    Triangle t2;

    /**
     * Original length in the input mesh.
     */
    final double origLength;

    /**
     * Twice the logarithm of the original length.
     */
    final double origLogLength;

    /**
     * Twice the logarithm of the current length.
     */
    double logLength;

    /**
     * The current length.
     */
    double length;

    /**
     * The direction of this edge.
     * This value is set during the layout phase.
     * Its type depends on the underlying geometry.
     * <dl>
     * <dt>Euclidean:</dt><dd>The angle between the x axis and this edge</dd>
     * <dt>Hyperbolic:</dt><dd>The transformation mapping the origin
     * to v1 and the real axis to the line along this edge</dd>
     * </dl>
     */
    private Object direction;

    /**
     * Construct edge.
     * @param v1 first vertex
     * @param v2 second vertex
     * @param t1 triangle having v1 and v2 in this order
     */
    Edge(Vertex v1, Vertex v2, Triangle t1, double length) {
        this.v1 = v1;
        this.v2 = v2;
        this.t1 = t1;
        this.t2 = null;
        this.origLength = this.length = length;
        this.origLogLength = this.logLength = 2*Math.log(length);
        assert length > 0: "length must be positive";
    }

    /**
     * Add second triangle to edge.
     * Some consistency checks ensure that the vertices match up.
     * @param v1 first vertex for consistency check
     * @param v2 second vertex for consistency check
     * @param t2 triangle having v2 and v1 in this order
     * @throws MeshException for inconsistent orientation
     *         of if called multiple times
     */
    void addTriangle(Vertex v1, Vertex v2, Triangle t2)
        throws MeshException
    {
        if (!(this.v1.equals(v1) && this.v2.equals(v2))) {
            if (this.v1.equals(v2) && this.v2.equals(v1))
                throw new MeshException("inconsistent triangle orientation");
            else
                throw new RuntimeException("invalid vertex pair");
        }
        if (this.t2 != null)
            throw new MeshException("More than two triangles adjacent "+
                                    "to a single edge");
        this.t2 = t2;
    }

    /**
     * Return the other incident triangle besides the given one.
     * @param t the incident triangle not to be returned
     * @return the incident triangle different from <code>t</code>
     */
    public Triangle getOtherTriangle(Triangle t) {
        if (t == t1) return t2;
        assert t == t2: "Argument has to be one of the incedent triangles";
        return t1;
    }

    /**
     * Determine whether the edge lies on the boundary.
     */
    public boolean isBoundary() {
        return t2 == null;
    }

    /**
     * Set the angle of this edge unless it was already set.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    public void offerAngle(double angle) {
        if (direction == null)
            direction = Double.valueOf(angle);
    }

    /**
     * Get the angle of this edge.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    public double getAngle() {
        return ((Double)direction).doubleValue();
    }

    public HypEdgePos offerHypPos(HypEdgePos pos) {
        if (direction == null)
            direction = pos;
        return (HypEdgePos)direction;
    }

    public HypEdgePos getHypPos() {
        return (HypEdgePos)direction;
    }

    /**
     * Format edge using the string representations of the underlying
     * vertex representations in the original input mesh.
     */
    @Override public String toString() {
        return v1.rep.toString() + "--" + v2.rep.toString();
    }

}
