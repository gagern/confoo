package net.von_gagern.martin.cetm.mesh;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mesh2D implements LocatedMesh<Vertex2D>, Iterable<Triangle2D> {

    private List<Triangle2D> ts;

    public Iterator<Triangle2D> iterator() {
        return ts.iterator();
    }

    public double edgeLength(Vertex2D v1, Vertex2D v2) {
        return v1.distance(v2);
    }

    public double getX(Vertex2D v) {
        return v.getX();
    }

    public double getY(Vertex2D v) {
        return v.getY();
    }

    public double getZ(Vertex2D v) {
        return 0;
    }

    public List<Triangle2D> getTriangles() {
        return ts;
    }

    public Set<Edge2D> getEdges() {
        Set<Edge2D> es = new HashSet<Edge2D>(ts.size()*3);
        for (Triangle2D t: ts) {
            for (int i = 0; i < 3; ++i) {
                es.add(new Edge2D(t.getCorner(i), t.getCorner((i + 1)%3)));
            }
        }
        return es;
    }

    public Shape getBoundary() {
        Set<Edge2D> edges = new HashSet<Edge2D>(ts.size()*3);
        Set<Edge2D> interior = new HashSet<Edge2D>(ts.size()*3/2);
        for (Triangle2D t: ts) {
            for (int i = 0; i < 3; ++i) {
                Edge2D e = new Edge2D(t.getCorner(i), t.getCorner((i + 1)%3));
                if (!edges.add(e)) interior.add(e);
            }
        }
        edges.removeAll(interior);
        interior = null;

        Map<Vertex2D, PathSegment> segmentEndpoints =
            new HashMap<Vertex2D, PathSegment>(2*edges.size());
        List<PathSegment> completedSegments = new ArrayList<PathSegment>(1);
        for (Edge2D e: edges) {
            Vertex2D p1 = e.getP1(), p2 = e.getP2();
            PathSegment s = new PathSegment(p1, p2);
            PathSegment s1 = segmentEndpoints.remove(p1);
            PathSegment s2 = segmentEndpoints.remove(p2);
            boolean completed = false;
            if (s1 != null) s.join(s1, p1);
            else segmentEndpoints.put(p1, s);
            if (s2 != null) completed = s.join(s2, p2);
            else segmentEndpoints.put(p2, s);
            if (completed) completedSegments.add(s);
        }
        assert segmentEndpoints.isEmpty(): "All segments should get completed";

        // Path2D was introduced in Java 1.6, so we stick to GeneralPath
        // for now in order to keep compatibility with 1.5.
        GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        for (PathSegment s1: completedSegments) {
            path.moveTo((float)s1.p1.getX(), (float)s1.p1.getY());
            for (PathSegment s2 = s1.s2; s2 != s1; s2 = s2.s2) {
                path.lineTo((float)s2.p1.getX(), (float)s2.p1.getY());
            }
            path.closePath();
        }
        return path;
    }

    private static class PathSegment {

        private Vertex2D p1;

        private Vertex2D p2;

        private PathSegment s1;

        private PathSegment s2;

        private PathSegment unionRep;

        public PathSegment(Vertex2D p1, Vertex2D p2) {
            this.p1 = p1;
            this.p2 = p2;
            unionRep = this;
        }

        public boolean join(PathSegment s, Vertex2D p) {
            halfJoin(s, p);
            s.halfJoin(this, p);
            return union(s2);
        }

        private void halfJoin(PathSegment s, Vertex2D p) {
            if (p.equals(p1)) {
                s1 = s;
            }
            else {
                assert p.equals(p2): "Endpoint contained in segment";
                s2 = s;
            }
        }

        private boolean union(PathSegment that) {
            PathSegment u1 = this.find(), u2 = that.find();
            if (u1 == u2) return true;
            u1.unionRep = u2;
            return false;
        }

        private PathSegment find() {
            if (unionRep != this)
                unionRep = unionRep.find();
            return unionRep;
        }

    }

}
