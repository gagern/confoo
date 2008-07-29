package net.von_gagern.martin.cetm.mesh;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Edge2D extends Line2D.Double {

    public Edge2D(Point2D p1, Point2D p2) {
        super(p1, p2);
    }

    @Override public Vertex2D getP1() {
        return new Vertex2D(x1, y1);
    }

    @Override public Vertex2D getP2() {
        return new Vertex2D(x2, y2);
    }

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
