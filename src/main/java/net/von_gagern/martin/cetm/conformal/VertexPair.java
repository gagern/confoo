package net.von_gagern.martin.cetm.conformal;

class VertexPair {

    private Vertex v1;

    private Vertex v2;

    public VertexPair(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override public int hashCode() {
        return v1.hashCode() ^ v2.hashCode();
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof VertexPair)) return false;
        VertexPair that = (VertexPair)o;
        return (this.v1.equals(that.v1) && this.v2.equals(that.v2)) ||
               (this.v1.equals(that.v2) && this.v2.equals(that.v1));
    }

}
