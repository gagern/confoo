package net.von_gagern.martin.confoo.conformal;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.MetricMesh;

/**
 * Internal mesh representation.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class InternalMesh<V> implements LocatedMesh<Vertex> {

    /**
     * Flag to control whether returned lists are unmodifiable.
     * Having this set to <code>true</code> during development ensures
     * that no code accidentially modifies the list. Setting it to
     * <code>false</code> for production use might result in better
     * performance, as it can do with one less layer of indirection.
     */
    private static final boolean unmodifiable = true;
    // TODO: set unmodifiable to false once the code is stable enough

    /**
     * List of all vertices.
     */
    private final List<Vertex> vs;

    /**
     * List of all edges.
     */
    private final List<Edge> es;

    /**
     * List of all triangles.
     */
    private final List<Triangle> ts;

    /**
     * List of all angles.
     */
    private final List<Angle> as;

    /**
     * Map from input vertices to internbal vertices.
     */
    private final Map<V, Vertex> vm;

    /**
     * Map from vertex pairs to edge objects.
     */
    private final Map<VertexPair, Edge> em;

    /**
     * Construct internal mesh from metric mesh.
     * @throws MeshException if the internal mesh is malformed
     */
    public InternalMesh(MetricMesh<V> mesh) throws MeshException {
        vs = new ArrayList<Vertex>();
        es = new ArrayList<Edge>();
        ts = new ArrayList<Triangle>();
        as = new ArrayList<Angle>();
        vm = new HashMap<V, Vertex>();
        em = new HashMap<VertexPair, Edge>();

        Object[] tcs = new Object[3];
        Vertex[] tvs = new Vertex[3];
        Edge[] tes = new Edge[3];
        Angle[] tas = new Angle[3];

        Iterator<? extends CorneredTriangle<? extends V>> iter;
        iter = mesh.iterator();
        while (iter.hasNext()) {
            CorneredTriangle<? extends V> triangle = iter.next();

            // handle vertices
            for (int i = 0; i < 3; ++i) {
                V c = triangle.getCorner(i);
                Vertex v = vm.get(c);
                if (v == null) {
                    v = new Vertex(c);
                    vs.add(v);
                    vm.put(c, v);
                }
                tcs[i] = c;
                tvs[i] = v;
            }

            // handle triangle
            Triangle t = new Triangle();
            ts.add(t);

            // handle edges
            for (int i = 0; i < 3; ++i) {
                Vertex v1 = tvs[(i+1)%3], v2 = tvs[(i+2)%3];
                VertexPair vp = new VertexPair(v1, v2);
                Edge e = em.get(vp);
                if (e == null) {
                    V c1 = (V)tcs[(i+1)%3], c2 = (V)tcs[(i+2)%3];
                    e = new Edge(v1, v2, t, mesh.edgeLength(c1, c2));
                    es.add(e);
                    em.put(vp, e);
                }
                else {
                    e.addTriangle(v2, v1, t);
                }
                tes[i] = e;
            }

            // handle angles
            for (int i = 0; i < 3; ++i) {
                Angle a = new Angle(tvs[i], tvs[(i+1)%3], tvs[(i+2)%3],
                                    tes[i], tes[(i+1)%3], tes[(i+2)%3]);
                as.add(a);
                tas[i] = a;
            }
            for (int i = 0; i < 3; ++i) {
                tas[i].nextAngle = tas[(i+1)%3];
            }
            t.setAngles(tas[0], tas[1], tas[2]);

        }

        for (Edge e: es) {
            Vertex v1 = e.v1, v2 = e.v2;
            Vertex.Kind k1 = v1.kind, k2 = v2.kind;
            if (e.isBoundary()) {
                if (k1 == null)
                    v1.kind = Vertex.Kind.CORNER;
                if (k1 == Vertex.Kind.INTERIOR)
                    v1.kind = Vertex.Kind.BOUNDARY;
                if (k2 == null)
                    v2.kind = Vertex.Kind.CORNER;
                if (k2 == Vertex.Kind.INTERIOR)
                    v2.kind = Vertex.Kind.BOUNDARY;
            }
            else {
                if (k1 == null)
                    v1.kind = Vertex.Kind.INTERIOR;
                if (k1 == Vertex.Kind.CORNER)
                    v1.kind = Vertex.Kind.BOUNDARY;
                if (k2 == null)
                    v2.kind = Vertex.Kind.INTERIOR;
                if (k2 == Vertex.Kind.CORNER)
                    v2.kind = Vertex.Kind.BOUNDARY;
            }
        }
    }

    /**
     * Get vertex list. The reurned list should not be modified.
     * @return the list of all vertices
     */
    public List<Vertex> getVertices() {
        if (unmodifiable) return Collections.unmodifiableList(vs);
        else return vs;
    }

    /**
     * Get edge list. The reurned list should not be modified.
     * @return the list of all edges
     */
    public List<Edge> getEdges() {
        if (unmodifiable) return Collections.unmodifiableList(es);
        else return es;
    }

    /**
     * Get triangle list. The reurned list should not be modified.
     * @return the list of all triangles
     */
    public List<Triangle> getTriangles() {
        if (unmodifiable) return Collections.unmodifiableList(ts);
        else return ts;
    }

    /**
     * Get angle list. The reurned list should not be modified.
     * @return the list of all angles
     */
    public List<Angle> getAngles() {
        if (unmodifiable) return Collections.unmodifiableList(as);
        else return as;
    }

    /**
     * Get map from input to internal vertices.
     * The reurned map should not be modified.
     * @return a map from input vertices to internal vertices
     */
    public Map<V, Vertex> getVertexMap() {
        if (unmodifiable) return Collections.unmodifiableMap(vm);
        else return vm;
    }

    /**
     * Get edge incident to given vertex pair.
     * @param v1 one vertex
     * @param v2 a second vertex
     * @return the edge incident to both vertices of <code>null</code>
     *         if the vertices are not adjacent to one another
     */
    public Edge getEdge(Vertex v1, Vertex v2) {
        return em.get(new VertexPair(v1, v2));
    }

    /**
     * Get iterator over internal triangles.
     * @return an iterator over the list of triangles
     */
    public Iterator<Triangle> iterator() {
        return ts.iterator();
    }

    /**
     * Get the length of a given edge.
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @return the length of the edge between these two vertices
     * @throws NullPointerException if the vertices are not adjacent
     *         to one another
     */
    public double edgeLength(Vertex v1, Vertex v2) {
        return getEdge(v1, v2).length;
    }

    /**
     * Get x coordinate of a vertex.
     * @param v a vertex of the mesh
     * @return the x coordinate of the vertex
     * @throws NullPointerException if the mesh has not been layed out
     *         yet
     */
    public double getX(Vertex v) {
        return v.getX();
    }

    /**
     * Get y coordinate of a vertex.
     * @param v a vertex of the mesh
     * @return the y coordinate of the vertex
     * @throws NullPointerException if the mesh has not been layed out
     *         yet
     */
    public double getY(Vertex v) {
        return v.getY();
    }

    /**
     * Get z coordinate of a vertex.
     * @param v a vertex of the mesh
     * @return zero
     */
    public double getZ(Vertex v) {
        return 0;
    }

}
