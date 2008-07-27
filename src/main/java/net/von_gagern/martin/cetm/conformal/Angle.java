package net.von_gagern.martin.cetm.conformal;

class Angle {

    private final Vertex vertex;

    private final Vertex nextVertex;

    private final Vertex prevVertex;

    private final Edge oppositeEdge;

    private final Edge nextEdge;

    private final Edge prevEdge;

    private double angle;

    public Angle(Vertex vertex, Vertex nextVertex, Vertex prevVertex,
                 Edge oppositeEdge, Edge prevEdge, Edge nextEdge) {
        this.vertex = vertex;
        this.nextVertex = nextVertex;
        this.prevVertex = prevVertex;
        this.oppositeEdge = oppositeEdge;
        this.prevEdge = prevEdge;
        this.nextEdge = nextEdge;
        this.angle = Double.NaN;
    }

    public void update() {
        double lo = oppositeEdge.length();
        double ln = nextEdge.length();
        double lp = prevEdge.length();
        double nom = (ln + lo - lp)*(lo + lp - ln);
        double denom = (lp + ln - lo)*(lo + lp + ln);
        if (nom <= denom)
            angle = 2.*Math.atan(Math.sqrt(nom/denom));
        else
            angle = Math.PI - 2.*Math.atan(Math.sqrt(denom/nom));
        assert !Double.isInfinite(angle): "angle is infinite";
        assert !Double.isNaN(angle): "angle is NaN";
    }

    public Vertex vertex() {
        return vertex;
    }

    public Vertex nextVertex() {
        return nextVertex;
    }

    public Vertex prevVertex() {
        return prevVertex;
    }

    public Edge nextEdge() {
        return nextEdge;
    }

    public Edge prevEdge() {
        return prevEdge;
    }

    public Edge oppositeEdge() {
        return oppositeEdge;
    }

    public double angle() {
        return angle;
    }

}
