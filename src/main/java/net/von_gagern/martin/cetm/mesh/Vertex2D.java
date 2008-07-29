package net.von_gagern.martin.cetm.mesh;

import java.awt.geom.Point2D;

public class Vertex2D extends Point2D.Double {

    public Vertex2D(Point2D p) {
        super(p.getX(), p.getY());
    }

    public Vertex2D(double x, double y) {
        super(x, y);
    }

    @Override public int hashCode() {
        long xl = java.lang.Double.doubleToLongBits(x);
        long yl = java.lang.Double.doubleToLongBits(y);
        int xi = (int)xl ^ (int)(xl >>> 32);
        int yi = (int)(yl << 16) ^ (int)(yl >>> 16) ^ (int)(yl >>> 48);
        return xi ^ yi;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point2D)) return false;
        if (o == this) return true;
        Point2D p = (Point2D)o;
        return x == p.getX() && y == p.getY();
    }

}
