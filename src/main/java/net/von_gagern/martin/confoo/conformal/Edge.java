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
    private final Vertex v1;

    /**
     * Second endpoint of the edge.
     */
    private final Vertex v2;

    /**
     * First triangle incident to the edge. This is the triangle that
     * has v1 and v2 following one another in this order in its cyclic
     * vertex order.
     */
    private final Triangle t1;

    /**
     * Second triangle incident to the edge. This is the triangle that
     * has v2 and v1 following one another in this order in its cyclic
     * vertex order.
     */
    private Triangle t2;

    /**
     * Original length in the input mesh.
     */
    private final double origLength;

    /**
     * Twice the logarithm of the original length.
     */
    private final double origLogLength;

    /**
     * Twice the logarithm of the current length.
     */
    private double logLength;

    /**
     * The current length.
     */
    private double length;

    /**
     * The angle of this edge.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    private double angle = Double.NaN;

    /**
     * Construct edge.
     * @param v1 first vertex
     * @param v2 second vertex
     * @param t1 triangle having v1 and v2 in this order
     */
    public Edge(Vertex v1, Vertex v2, Triangle t1, double length) {
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
    public void addTriangle(Vertex v1, Vertex v2, Triangle t2)
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
     * Update edge length from vertex length factors.
     */
    public void update() {
        logLength = origLogLength + v1.getU() + v2.getU();
        assert !Double.isInfinite(logLength): "logLength is infinite";
        assert !Double.isNaN(logLength): "logLength is NaN";
        length = Math.exp(logLength/2);
        assert length > 0: "length must stay positive (" + logLength + ")";
    }

    /**
     * Return twice the logarithm of the current length.
     * @return twice the logarithm of the current length.
     */
    public double logLength() {
        return logLength;
    }

    /**
     * Return current length.
     * @return current length
     */
    public double length() {
        return length;
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
     * Return first vertex.
     */
    public Vertex getV1() {
        return v1;
    }

    /**
     * Return second vertex.
     */
    public Vertex getV2() {
        return v2;
    }

    /**
     * Get the angle of this edge.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Set the angle of this edge.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Set the angle of this edge unless it was already set.
     * This is the angle between the x axis and this edge and is set
     * during the layout phase.
     */
    public void offerAngle(double angle) {
        if (Double.isNaN(this.angle))
            this.angle = angle;
    }

    /**
     * Format edge using the string representations of the underlying
     * vertex representations in the original input mesh.
     */
    @Override public String toString() {
        return v1.getRep().toString() + "--" + v2.getRep().toString();
    }

}
