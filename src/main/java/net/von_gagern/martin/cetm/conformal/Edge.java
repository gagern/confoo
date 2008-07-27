package net.von_gagern.martin.cetm.conformal;

import net.von_gagern.martin.cetm.mesh.MeshException;

class Edge {

    private final Vertex v1;

    private final Vertex v2;

    private final Triangle t1;

    private Triangle t2;

    private final double origLength;

    private final double origLogLength;

    private double logLength;

    private double length;

    private double angle = Double.NaN;

    public Edge(Vertex v1, Vertex v2, Triangle t1, double length) {
        this.v1 = v1;
        this.v2 = v2;
        this.t1 = t1;
        this.t2 = null;
        this.origLength = this.length = length;
        this.origLogLength = this.logLength = 2*Math.log(length);
        assert length > 0: "length must be positive";
    }

    public void addTriangle(Vertex v1, Vertex v2, Triangle t2)
        throws MeshException
    {
        if (!(this.v1.equals(v1) && this.v2.equals(v2))) {
            if (this.v1.equals(v2) && this.v2.equals(v1))
                throw new MeshException("inconsistent triangle orientation");
            else
                throw new RuntimeException("invalid vertex pair");
        }
        if (this.t2 != null)
            throw new MeshException("More than two triangles adjacent "+
                                    "to a single edge");
        this.t2 = t2;
    }

    public void update() {
        logLength = origLogLength + v1.getU() + v2.getU();
        assert !Double.isInfinite(logLength): "logLength is infinite";
        assert !Double.isNaN(logLength): "logLength is NaN";
        length = Math.exp(logLength/2);
        assert length > 0: "length must stay positive (" + logLength + ")";
    }

    public double logLength() {
        return logLength;
    }

    public double length() {
        return length;
    }

    public Triangle getOtherTriangle(Triangle t) {
        if (t == t1) return t2;
        else return t1;
    }

    public boolean isBoundary() {
        return t2 == null;
    }

    public Vertex getV1() {
        return v1;
    }

    public Vertex getV2() {
        return v2;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void offerAngle(double angle) {
        if (Double.isNaN(this.angle))
            this.angle = angle;
    }

    @Override public String toString() {
        return v1.getRep().toString() + "--" + v2.getRep().toString();
    }

}
