package net.von_gagern.martin.cetm.mesh;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A 2D line with unordered equality comparison.<p>
 *
 * This implementation of an edge provides equality comparison based
 * on the coordinates of the endpoints. It can therefore be used to
 * identify edges shared between different triangles.
 */
public class Edge2D extends Line2D.Double {

    /**
     * Construct an edge from two endpoints.
     *
     * @param p1 one endpoint of the edge
     * @param p2 the other endpoint of the edge
     */
    public Edge2D(Point2D p1, Point2D p2) {
        super(p1, p2);
    }

    /**
     * Return one end point of this line.
     *
     * @return an end point of this line.
     * @see #getP2()
     */
    @Override public Vertex2D getP1() {
        return new Vertex2D(x1, y1);
    }

    /**
     * Return the other end point of this line.
     *
     * @return an end point of this line.
     * @see #getP1()
     */
    @Override public Vertex2D getP2() {
        return new Vertex2D(x2, y2);
    }

    /**
     * Calculate hash code from coordinates.
     *
     * @return a hash code derived from the coordinates
     */
    @Override public int hashCode() {
        // For the hash code we take some numbers that describe the
        // edge without respect for orientation, then we shake their
        // bits into a single int.
        long lsx = java.lang.Double.doubleToLongBits(x1 + x2);
        long ldx = java.lang.Double.doubleToLongBits(Math.abs(x1 - x2));
        long lsy = java.lang.Double.doubleToLongBits(y1 + y2);
        long ldy = java.lang.Double.doubleToLongBits(Math.abs(y1 - y2));
        int isx = (int)lsx ^ (int)(lsx >>> 32);
        int idx = (int)(ldx << 8) ^ (int)(ldx >>> 24) ^ (int)(ldx >>> 56);
        int isy = (int)(lsy << 16) ^ (int)(lsy >>> 16) ^ (int)(lsy >>> 48);
        int idy = (int)(ldy << 24) ^ (int)(ldy >>> 8) ^ (int)(ldy >>> 40);
        int idd = (x1 - x2)*(y1 - y2) > 0 ? 0x55555555 : 0xaaaaaaaa;
        return isx ^ idx ^ isy ^ idy ^ idd;
    }

    /**
     * Compare edge to other line.<p>
     *
     * The comparison is based on coordinates, not object identity.
     * Two edges are equal if both their endpoints have the same
     * coordinates. The endpoiunts can be equal in either order, the
     * orientation of the edge is not taken into account. Therefore
     * <code>e1.equals(e2)</code> for edges does not imply
     * <code>e1.getP1().equals(e2.getP1())</code> but does imply
     * <code>e1.getP1().equals(e2.getP1()) || e1.getP1().equals(e2.getP2())</code>.<p>
     *
     * The classes aren't compared either. Any <code>Line2D</code>
     * with the same coordinates will compare equal. <b>This is a
     * violation of symmetry!</b> While it may be handy to compare
     * edges with other lines where you know what you are doing,
     * mixing them in any container that compares its elements is a
     * bad idea, as it can lead to inconsistent behaviour.
     *
     * @param o another object
     * @return whether o is a <code>Line2D</code> with the same
     *         coordinates as this edge
     * @see Line2D
     */
    @Override public boolean equals(Object o) {
        if (!(o instanceof Line2D)) return false;
        if (this == o) return true;
        Line2D that = (Line2D)o;
        return (this.getX1() == that.getX1() && this.getY1() == that.getY1() &&
                this.getX2() == that.getX2() && this.getY2() == that.getY2()) ||
               (this.getX1() == that.getX2() && this.getY1() == that.getY2() &&
                this.getX2() == that.getX1() && this.getY2() == that.getY1());
    }

}
