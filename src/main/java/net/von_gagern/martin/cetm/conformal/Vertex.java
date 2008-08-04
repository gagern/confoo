package net.von_gagern.martin.cetm.conformal;

import java.awt.geom.Point2D;

/**
 * Internal vertex representation.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class Vertex {

    /**
     * The vertex representant from the original input mesh.
     */
    private Object rep;

    /**
     * Construct new internal vertex.
     * @param rep the vertex representant from the original input mesh
     */
    public Vertex(Object rep) {
        this.rep = rep;
    }

    /**
     * Get vertex representant from the original input mesh.
     * @return the vertex representant from the original input mesh
     */
    public Object getRep() {
        return rep;
    }


    /**
     * The vertex index.
     * This is used by the Energy function. Fixed vertices will have
     * an index of -1.
     */
    private int index = -1;

    /**
     * Get vertex index.
     * @return the index assigned to the vertex
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set vertex index.
     * @param index the index to be assigned to the vertex
     */
    public void setIndex(int index) {
        this.index = index;
    }


    /**
     * Target angle sum.
     * The transformation will result in a mesh where the sum over all
     * angles incident to this vertex equals this value.
     */
    private double target = 2*Math.PI;

    /**
     * Get target angle sum.
     * @return the target angle sum
     */
    public double getTarget() {
        return target;
    }

    /**
     * Set target angle sum.
     * @param target the new target angle sum
     */
    public void setTarget(double target) {
        this.target = target;
    }


    /**
     * Scale parameter.
     * This length of edges incident to this vertex will be multiplied
     * by exp(u/2).
     */
    private double u = 0;

    /**
     * Get scale parameter.
     * @return the current scale parameter
     */
    public double getU() {
        return u;
    }

    /**
     * Set scale parameter
     * @param u the new scale parameter
     */
    public void setU(double u) {
        this.u = u;
    }


    /**
     * Fixed scale vertex flag.
     * A vertex with this flag set will not contribute a factor to the
     * lengths of its surrounding edges. Its parameter u will remain
     * fixed.
     */
    private boolean fixed = false;

    /**
     * Get fixed vertex flag.
     * @return the current state of the fixed vertex flag
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Set fixed vertex flag.
     * @param fixed the new state of the fixed vertex flag
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }


    /**
     * Enumeration of vertex kinds regarding.
     *
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
    public enum Kind {

        /**
         * Vertx incident with only boundary edges.
         */
        CORNER,

        /**
         * Vertex incident with both boundary and interior edges.
         */
        BOUNDARY,

        /**
         * Vertex incident with only interior edges.
         */
        INTERIOR,

    }

    /**
     * Vertex kind regarding boundary classification.
     */
    private Kind kind = null;

    /**
     * Get vertex kind regarding boundary classification.
     * @return the vertex kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Set vertex kind regarding boundary classification.
     * @param kind the vertex kind
     */
    public void setKind(Kind kind) {
        this.kind = kind;
    }


    /**
     * Vertex location. Set during layout.
     */
    private Point2D location;

    /**
     * Determine whether the vertex has a location assigned to it.
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * Get x coordinate of vertex.
     * @return the x coordinate of this vertex
     */
    public double getX() {
        return location.getX();
    }

    /**
     * Get y coordinate of vertex.
     * @return the y coordinate of this vertex
     */
    public double getY() {
        return location.getY();
    }

    /**
     * Set location of this vertex.
     * @param x the x coordinate of this vertex
     * @param y the y coordinate of this vertex
     */
    public void setLocation(double x, double y) {
        location = new Point2D.Double(x, y);
    }

    /**
     * Set location of this vertex is it wasn't set already.
     * @param x the x coordinate of this vertex
     * @param y the y coordinate of this vertex
     */
    public void offerLocation(double x, double y) {
        if (location == null)
            location = new Point2D.Double(x, y);
    }


    /**
     * String representation of this vertex.
     * The representation is based on the string representation of the
     * input vertex representant.
     * @return a string representation of the vertex.
     */
    @Override public String toString() {
        return rep.toString();
    }

}
