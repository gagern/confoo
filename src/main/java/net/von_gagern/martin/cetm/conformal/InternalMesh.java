package net.von_gagern.martin.cetm.conformal;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import net.von_gagern.martin.cetm.mesh.CorneredTriangle;
import net.von_gagern.martin.cetm.mesh.LocatedMesh;
import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.MetricMesh;

class InternalMesh<V> implements LocatedMesh<Vertex> {

    private static final boolean unmodifiable = true;

    private final List<Vertex> vs;

    private final List<Edge> es;

    private final List<Triangle> ts;

    private final List<Angle> as;

    private final Map<V, Vertex> vm;

    private final Map<VertexPair, Edge> em;

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
                    v = new Vertex();
                    vs.add(v);
                    vm.put(c, v);
                }
                tcs[i] = c;
                tvs[i] = v;
            }

            // handle triangle
            Triangle t = new Triangle(tvs[0], tvs[1], tvs[2]);
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
            t.setEdges(tes[0], tes[1], tes[2]);

            // handle angles
            for (int i = 0; i < 3; ++i) {
                Angle a = new Angle(tvs[i], tvs[(i+1)%3], tvs[(i+2)%3],
                                    tes[i], tes[(i+1)%3], tes[(i+2)%3]);
                as.add(a);
                tas[i] = a;
            }
            t.setAngles(tas[0], tas[1], tas[2]);

        }

        for (Edge e: es) {
            Vertex v1 = e.getV1(), v2 = e.getV2();
            Vertex.Kind k1 = v1.getKind(), k2 = v2.getKind();
            if (e.isBoundary()) {
                if (k1 == null)
                    v1.setKind(Vertex.Kind.CORNER);
                if (k1 == Vertex.Kind.INTERIOR)
                    v1.setKind(Vertex.Kind.BOUNDARY);
                if (k2 == null)
                    v2.setKind(Vertex.Kind.CORNER);
                if (k2 == Vertex.Kind.INTERIOR)
                    v2.setKind(Vertex.Kind.BOUNDARY);
            }
            else {
                if (k1 == null)
                    v1.setKind(Vertex.Kind.INTERIOR);
                if (k1 == Vertex.Kind.CORNER)
                    v1.setKind(Vertex.Kind.BOUNDARY);
                if (k2 == null)
                    v2.setKind(Vertex.Kind.INTERIOR);
                if (k2 == Vertex.Kind.CORNER)
                    v2.setKind(Vertex.Kind.BOUNDARY);
            }
        }
    }

    public List<Vertex> getVertices() {
        if (unmodifiable) return Collections.unmodifiableList(vs);
        else return vs;
    }

    public List<Edge> getEdges() {
        if (unmodifiable) return Collections.unmodifiableList(es);
        else return es;
    }

    public List<Triangle> getTriangles() {
        if (unmodifiable) return Collections.unmodifiableList(ts);
        else return ts;
    }

    public List<Angle> getAngles() {
        if (unmodifiable) return Collections.unmodifiableList(as);
        else return as;
    }

    public Map<V, Vertex> getVertexMap() {
        if (unmodifiable) return Collections.unmodifiableMap(vm);
        else return vm;
    }

    public Edge getEdge(Vertex v1, Vertex v2) {
        return em.get(new VertexPair(v1, v2));
    }

    public Iterator<Triangle> iterator() {
        return ts.iterator();
    }

    public double edgeLength(Vertex v1, Vertex v2) {
        return getEdge(v1, v2).length();
    }

    public double getX(Vertex v) {
        return v.getX();
    }

    public double getY(Vertex v) {
        return v.getY();
    }

    public double getZ(Vertex v) {
        return 0;
    }

}
