package net.von_gagern.martin.cetm.mesh;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.NoSuchElementException;

public class Triangle2D extends SimpleTriangle<Vertex2D> implements Shape {

    public Triangle2D(Vertex2D p1, Vertex2D p2, Vertex2D p3) {
        super(p1, p2, p3);
    }

    public Triangle2D(Point2D p1, Point2D p2, Point2D p3) {
        super(new Vertex2D(p1), new Vertex2D(p2), new Vertex2D(p3));
    }

    public Triangle2D(double x1, double y1, double x2, double y2,
                      double x3, double y3) {
        super(new Vertex2D(x1, y2), new Vertex2D(x2, y2), new Vertex3D(x3, y3));
    }

    public Rectangle getBounds() {
        Rectangle2D r = getBounds2D();
        int left = (int)Math.floor(r.getMinX());
        int right = (int)Math.ceil(r.getMaxX());
        int top = (int)Math.floor(r.getMinY());
        int bottom = (int)Math.ceil(r.getMaxY());
        return new Rectangle(left, top, right - left, top - bottom);
    }

    public Rectangle2D getBounds2D() {
        Point2D p = getCorner(0);
        Rectangle2D r = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
        r.add(getCorner(1));
        r.add(getCorner(2));
        return r;
    }

    public boolean contains(double x, double y) {
        for (int i = 0; i < 3; ++i) {
            Point2D p1 = getCorner(i);
            Point2D p2 = getCorner((i + 1)%3);
            Point2D p3 = getCorner((i + 2)%3);
            double x1 = p1.getX(), y1 = p1.getY();
            double x2 = p2.getX(), y2 = p2.getY();
            double x3 = p3.getX(), y3 = p3.getY();
            // not contained if p3 and (x, y) lie on different sides of
            // triangle edge p1--p2, which can be seen from determinants:
            if (det(x1, y1, x2, y2, x3, y3)*det(x1, y1, x2, y2, x, y) < 0)
                return false;
        }
        return true;
    }

    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    public boolean contains(double x, double y, double w, double h) {
        // a triangle contains a rectangle if it contains all its corners
        double[] xr = { x, x+w, x, x+w }, yr = { y, y+h, y+h, y };
        for (int i = 0; i < 3; ++i) {
            Point2D p1 = getCorner(i);
            Point2D p2 = getCorner((i + 1)%3);
            Point2D p3 = getCorner((i + 2)%3);
            double x1 = p1.getX(), y1 = p1.getY();
            double x2 = p2.getX(), y2 = p2.getY();
            double x3 = p3.getX(), y3 = p3.getY();
            // not contained if p3 and a rectangle corner point
            // lie on different sides of triangle edge p1--p2
            double tdet = det(x1, y1, x2, y2, x3, y3);
            for (int j = 0; j < 4; ++j)
                if (tdet*det(x1, y1, x2, y2, xr[j], yr[j]) < 0)
                    return false;
        }
        return true;
    }

    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean intersects(double x, double y, double w, double h) {
        // a cheap bounding box check first off, before we start the real work
        if (!getBounds2D().intersects(x, y, w, h)) return false;
        return intersectsImpl(new Rectangle2D.Double(x, y, w, h));
    }

    public boolean intersects(Rectangle2D r)  {
        // a cheap bounding box check first off, before we start the real work
        if (!getBounds2D().intersects(r)) return false;
        return intersectsImpl(r);
    }

    private boolean intersectsImpl(Rectangle2D r)  {
        // a triangle intersects a rectangle if
        // 1) the rectangle contains the triangle, i.e. all its corners
        // 2) the triangle contains the rectangle, i.e. all its corners
        // 3) a triangle edge intersects a rectangle edge

        rectContainsTriangle: do {
            for (int i = 0; i < 3; ++i)
                if (!r.contains(getCorner(i)))
                    break rectContainsTriangle;
            return true;
        } while (false);

        if (this.contains(r)) return true;

        for (int i = 0; i < 3; ++i) {
            Point2D p1 = getCorner(i), p2 = getCorner((i + 1)%3);
            if (r.intersectsLine(p1.getX(), p1.getY(), p2.getX(), p2.getY()))
                return true;
        }

        return false;
    }

    private double det(double x1, double y1,
                       double x2, double y2,
                       double x3, double y3) {
        return x1*y2 + x2*y3 + x3*y1 - x1*y3 - x2*y1 - x3*y2;
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return new Iter(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new Iter(at);
    }

    private class Iter implements PathIterator {

        private AffineTransform at;

        private int i = 0;

        public Iter(AffineTransform at) {
            this.at = at;
        }

        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        public boolean isDone() {
            return i == 4;
        }

        public void next() {
            if (isDone())
                throw new NoSuchElementException("End of path reached");
            ++i;
        }

        public int currentSegment(double[] coords) {
            if (i == 3) return SEG_CLOSE;
            Point2D p = getCorner(i);
            coords[0] = p.getX();
            coords[1] = p.getY();
            if (at != null) at.transform(coords, 0, coords, 0, 1);
            if (i == 0) return SEG_MOVETO;
            else return SEG_LINETO;
        }

        public int currentSegment(float[] coords) {
            if (i == 3) return SEG_CLOSE;
            Point2D p = getCorner(i);
            coords[0] = (float)p.getX();
            coords[1] = (float)p.getY();
            if (at != null) at.transform(coords, 0, coords, 0, 1);
            if (i == 0) return SEG_MOVETO;
            else return SEG_LINETO;
        }

    }

}
