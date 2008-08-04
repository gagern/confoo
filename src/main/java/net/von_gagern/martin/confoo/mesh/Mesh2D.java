package net.von_gagern.martin.confoo.mesh;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A 2D triangle mesh.<p>
 *
 * Vertices of this mesh are identified by their coordinates, not
 * object identity.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class Mesh2D implements LocatedMesh<Vertex2D>, Iterable<Triangle2D> {

    /**
     * List of all mesh triangles.
     */
    private List<Triangle2D> ts;

    /**
     * Construct 2D mesh from a located mesh.
     * The z coordinates of the input mesh will be simply ignored.
     * After construction the two meshes will be independent.
     * @param mesh the mesh to be copied
     */
    public <V> Mesh2D(LocatedMesh<V> mesh) {
        ts = new ArrayList<Triangle2D>();
        Map<V, Vertex2D> vm = new HashMap<V, Vertex2D>();
        Vertex2D[] tv = new Vertex2D[3];
        Iterator<? extends CorneredTriangle<? extends V>> i = mesh.iterator();
        while (i.hasNext()) {
            CorneredTriangle<? extends V> t = i.next();
            for (int j = 0; j < 3; ++j) {
                V c = t.getCorner(j);
                Vertex2D v = vm.get(c);
                if (v == null) {
                    v = new Vertex2D(mesh.getX(c), mesh.getY(c));
                    vm.put(c, v);
                }
                tv[j] = v;
            }
            ts.add(new Triangle2D(tv[0], tv[1], tv[2]));
        }
    }

    /**
     * Construct 2D mesh from collection of triangles.
     * After construction the mesh will be independent.
     * @param mesh the triangles making up the mesh
     */
    public Mesh2D(Collection<? extends CorneredTriangle<? extends Point2D>>
                  mesh) {
        ts = new ArrayList<Triangle2D>();
        // We still use a map to keep the number of vertex objects low
        Map<Vertex2D, Vertex2D> vm = new HashMap<Vertex2D, Vertex2D>();
        Vertex2D[] tv = new Vertex2D[3];
        Iterator<? extends CorneredTriangle<? extends Point2D>> i;
        i = mesh.iterator();
        while (i.hasNext()) {
            CorneredTriangle<? extends Point2D> t = i.next();
            for (int j = 0; j < 3; ++j) {
                Vertex2D c = new Vertex2D(t.getCorner(j));
                Vertex2D v = vm.get(c);
                if (v == null) {
                    vm.put(c, c);
                }
                tv[j] = c;
            }
            ts.add(new Triangle2D(tv[0], tv[1], tv[2]));
        }
    }

    /**
     * Return iterator over all triangles.
     * @return an iterator over the triangles of the mesh
     */
    public Iterator<Triangle2D> iterator() {
        return ts.iterator();
    }

    /**
     * Determine edge length.
     * @param v1 one vertex
     * @param v2 a second vertex
     * @return the distance between the vertices
     */
    public double edgeLength(Vertex2D v1, Vertex2D v2) {
        return v1.distance(v2);
    }

    /**
     * Get x coordinate of vertex.
     * @param v a vertex
     * @return the x coordinate of <code>v</code>
     */
    public double getX(Vertex2D v) {
        return v.getX();
    }

    /**
     * Get y coordinate of vertex.
     * @param v a vertex
     * @return the y coordinate of <code>v</code>
     */
    public double getY(Vertex2D v) {
        return v.getY();
    }

    /**
     * Get z coordinate of vertex.
     * As this is a 2D mesh, this method will always return zero.
     * @param v a vertex
     * @return zero
     */
    public double getZ(Vertex2D v) {
        return 0;
    }

    /**
     * Retrieve collection of triangles.
     * @return the list of all triangles
     */
    public List<Triangle2D> getTriangles() {
        return ts;
    }

    /**
     * Retrieve collection of all edges.
     * @return the set of all edges
     */
    public Set<Edge2D> getEdges() {
        Set<Edge2D> es = new HashSet<Edge2D>(ts.size()*3);
        for (Triangle2D t: ts) {
            for (int i = 0; i < 3; ++i) {
                es.add(new Edge2D(t.getCorner(i), t.getCorner((i + 1)%3)));
            }
        }
        return es;
    }

    /**
     * Retrieve collection of all interior edges.
     * Interior edges are edges incident to more than one triangle.
     * @return the set of all interior edges
     */
    public Set<Edge2D> getInteriorEdges() {
        Set<Edge2D> edges = new HashSet<Edge2D>(ts.size()*3);
        Set<Edge2D> interior = new HashSet<Edge2D>(ts.size()*3/2);
        for (Triangle2D t: ts) {
            for (int i = 0; i < 3; ++i) {
                Edge2D e = new Edge2D(t.getCorner(i), t.getCorner((i + 1)%3));
                if (!edges.add(e)) interior.add(e);
            }
        }
        return interior;
    }

    /**
     * Retrieve collection of all boundary edges.
     * Boundary edges are edges incident to only one triangle.
     * @return the set of all boundary edges
     */
    public Set<Edge2D> getBoundaryEdges() {
        Set<Edge2D> edges = new HashSet<Edge2D>(ts.size()*3);
        Set<Edge2D> interior = new HashSet<Edge2D>(ts.size()*3/2);
        for (Triangle2D t: ts) {
            for (int i = 0; i < 3; ++i) {
                Edge2D e = new Edge2D(t.getCorner(i), t.getCorner((i + 1)%3));
                if (!edges.add(e)) interior.add(e);
            }
        }
        edges.removeAll(interior);
        return edges;
    }

    /**
     * Retrieve boundary shape.
     * This is the shape consisting of all polygones made up by
     * boundary edges.
     * @return the boundary shape
     */
    public Shape getBoundary() {
        Set<Edge2D> edges = getBoundaryEdges();
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

    /**
     * Helper class used to order and manage boundary segments.
     * @see Mesh2D#getBoundary()
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
    private static class PathSegment {

        /**
         * First vertex.
         */
        private Vertex2D p1;

        /**
         * Second vertex.
         */
        private Vertex2D p2;

        /**
         * Preceding segment.
         */
        private PathSegment s1;

        /**
         * Following segment.
         */
        private PathSegment s2;

        /**
         * Union representant.
         */
        private PathSegment unionRep;

        /**
         * Construct path segment for two endpoints.
         * @param p1 first endpoint
         * @param p2 second endpoint
         */
        public PathSegment(Vertex2D p1, Vertex2D p2) {
            this.p1 = p1;
            this.p2 = p2;
            unionRep = this;
        }

        /**
         * Join two path segments
         * @param s the path segment to join to this one
         * @param p the vertex common to both segments
         * @return whether a polygon was closed by this join
         */
        public boolean join(PathSegment s, Vertex2D p) {
            halfJoin(s, p);
            s.halfJoin(this, p);
            return union(s);
        }

        /**
         * Perform one half of a join.
         * @param s the path segment to join to this one
         * @param p the vertex common to both segments
         */
        private void halfJoin(PathSegment s, Vertex2D p) {
            if (p.equals(p1)) {
                s1 = s;
            }
            else {
                assert p.equals(p2): "Endpoint contained in segment";
                s2 = s;
            }
        }

        /**
         * Unite two sequences of path segments.
         * @param that a segment from the sequence to be united with this one
         * @boolean whether the two sequences already were united
         */
        private boolean union(PathSegment that) {
            PathSegment u1 = this.find(), u2 = that.find();
            if (u1 == u2) return true;
            u1.unionRep = u2;
            return false;
        }

        /**
         * Find representant for sequence of path segments.
         * @return a unique object representing the whole united sequence
         */
        private PathSegment find() {
            if (unionRep != this)
                unionRep = unionRep.find();
            return unionRep;
        }

    }

}
