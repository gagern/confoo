package net.von_gagern.martin.confoo.conformal;

import java.awt.geom.Point2D;

class HypEdgePos {

    private Vertex v;

    private double a;

    private double b;

    private double c;

    private double d;

    public HypEdgePos() {
    }

    public HypEdgePos(double a, double b, double c, double d) {
        assign(a, b, c, d);
    }

    public Vertex getVertex() {
        return v;
    }

    public void setVertex(Vertex v) {
        this.v = v;
    }

    public HypEdgePos assign(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        return this;
    }

    public HypEdgePos assignProduct(HypEdgePos h1, HypEdgePos h2) {
        assign(h1.a*h2.c - h1.b*h2.d + h1.c*h2.a + h1.d*h2.b,
               h1.a*h2.d + h1.b*h2.c + h1.c*h2.b - h1.d*h2.a,
               h1.a*h2.a + h1.b*h2.b + h1.c*h2.c - h1.d*h2.d,
               h1.a*h2.b - h1.b*h2.a + h1.c*h2.d + h1.d*h2.c);
        return this;
    }

    public HypEdgePos concatenate(HypEdgePos that) {
        return assignProduct(this, that);
    }

    public HypEdgePos preConcatenate(HypEdgePos that) {
        return assignProduct(that, this);
    }

    public Point2D dehomogenize() {
        double denom = c*c + d*d;
        return new Point2D.Double((a*c + b*d)/denom, (b*c - a*d)/denom);
    }

    private static final double EPS_NORMALIZE = 1e-10;

    public void normalize() {
        double det = c*c + d*d - a*a - b*b;
        if (Math.abs(det - 1.) > EPS_NORMALIZE) {
            double denom = Math.sqrt(det);
            a /= denom;
            b /= denom;
            c /= denom;
            d /= denom;
        }
    }

    public HypEdgePos assignTranslation(double d) {
	if (Double.isInfinite(d))
	    assign(d > 0 ? 1. : -1., 0., 1., 0.);
	else
	    assign(Math.expm1(d), 0., Math.exp(d) + 1., 0.);
        normalize();
	return this;
    }

    public HypEdgePos assignTransRot(double d) {
	if (Double.isInfinite(d))
	    assign(0., d > 0 ? 1. : -1., 0., 1.);
	else
	    assign(0., Math.expm1(d), 0., Math.exp(d) + 1.);
        normalize();
	return this;
    }

    public static HypEdgePos getTransRot(double d) {
        return (new HypEdgePos()).assignTransRot(d);
    }

}
