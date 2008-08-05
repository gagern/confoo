package net.von_gagern.martin.confoo.conformal;

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
    Object rep;

    /**
     * Construct new internal vertex.
     * @param rep the vertex representant from the original input mesh
     */
    Vertex(Object rep) {
        this.rep = rep;
    }

    /**
     * The vertex index.
     * This is used by the Energy function. Fixed vertices will have
     * an index of -1.
     */
    int index = -1;

    /**
     * Target angle sum.
     * The transformation will result in a mesh where the sum over all
     * angles incident to this vertex equals this value.
     */
    double target = 2*Math.PI;

    /**
     * Scale parameter.
     * This length of edges incident to this vertex will be multiplied
     * by exp(u/2).
     */
    double u = 0;

    /**
     * Fixed scale vertex flag.
     * A vertex with this flag set will not contribute a factor to the
     * lengths of its surrounding edges. Its parameter u will remain
     * fixed.
     */
    boolean fixed = false;

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
    Kind kind = null;

    /**
     * Vertex location. Set during layout.
     */
    Point2D location;

    /**
     * Determine whether the vertex has a location assigned to it.
     */
    boolean hasLocation() {
        return location != null;
    }

    /**
     * Get x coordinate of vertex.
     * @return the x coordinate of this vertex
     */
    double getX() {
        return location.getX();
    }

    /**
     * Get y coordinate of vertex.
     * @return the y coordinate of this vertex
     */
    double getY() {
        return location.getY();
    }

    /**
     * Set location of this vertex is it wasn't set already.
     * @param x the x coordinate of this vertex
     * @param y the y coordinate of this vertex
     */
    void offerLocation(double x, double y) {
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
