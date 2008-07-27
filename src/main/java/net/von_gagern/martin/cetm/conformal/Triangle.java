package net.von_gagern.martin.cetm.conformal;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import net.von_gagern.martin.cetm.mesh.CorneredTriangle;

class Triangle implements CorneredTriangle<Vertex> {

    // private final List<Vertex> vs;

    // private List<Edge> es;

    private List<Angle> as;

    private boolean iterFlag;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        // vs = Arrays.asList(v1, v2, v3);
    }

    public void setEdges(Edge e1, Edge e2, Edge e3) {
        // es = Arrays.asList(e1, e2, e3);
    }

    public void setAngles(Angle a1, Angle a2, Angle a3) {
        as = Arrays.asList(a1, a2, a3);
    }

    public Vertex getCorner(int i) {
        // return vs.get(i);
        return as.get(i).vertex();
    }

    public List<Vertex> getVertices() {
        // return vs;
        return new VertexList();
    }

    public List<Edge> getEdges() {
        // return es;
        return new EdgeList();
    }

    public List<Angle> getAngles() {
        return as;
    }

    public boolean getIterFlag() {
        return iterFlag;
    }

    public void setIterFlag() {
        iterFlag = true;
    }

    public void clearIterFlag() {
        iterFlag = false;
    }

    public boolean isBoundary() {
        for (Edge e: getEdges())
            if (e.getOtherTriangle(this) == null)
                return true;
        return false;
    }

    public Vertex getOppositeVertex(Edge e) {
        for (Angle a: as)
            if (a.oppositeEdge() == e)
                return a.vertex();
        throw new IllegalArgumentException("given edge not part of triangle");
    }

    public Angle getNextAngle(Vertex v) {
        for (Angle a: as)
            if (a.prevVertex() == v)
                return a;
        throw new IllegalArgumentException("given vertex not part of triangle");
    }

    public Angle getPrevAngle(Vertex v) {
        for (Angle a: as)
            if (a.nextVertex() == v)
                return a;
        throw new IllegalArgumentException("given vertex not part of triangle");
    }

    private class EdgeList extends AbstractList<Edge> {

        public Edge get(int index) {
            return as.get(index).oppositeEdge();
        }

        public int size() {
            return as.size();
        }

    }

    private class VertexList extends AbstractList<Vertex> {

        public Vertex get(int index) {
            return as.get(index).vertex();
        }

        public int size() {
            return as.size();
        }

    }

    @Override public String toString() {
        StringBuilder buf = new StringBuilder("Triangle(");
        buf.append(as.get(0).vertex().getRep().toString()).append(", ");
        buf.append(as.get(1).vertex().getRep().toString()).append(", ");
        buf.append(as.get(2).vertex().getRep().toString()).append(")");
        return buf.toString();
    }

}
