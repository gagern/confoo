package net.von_gagern.martin.cetm.conformal;

import java.awt.geom.Point2D;

class Vertex {

    private Object rep;

    public Vertex(Object rep) {
        this.rep = rep;
    }

    public Object getRep() {
        return rep;
    }


    private int index = -1;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    private double target = 2*Math.PI;

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }


    private double u = 0;

    public double getU() {
        return u;
    }

    public void setU(double u) {
        this.u = u;
    }


    private boolean fixed = false;

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }


    public enum Kind {

        CORNER,

        BOUNDARY,

        INTERIOR,

    }

    private Kind kind = null;

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }


    private Point2D location;

    public boolean hasLocation() {
        return location != null;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public void setLocation(double x, double y) {
        location = new Point2D.Double(x, y);
    }

    public void offerLocation(double x, double y) {
        if (location == null)
            location = new Point2D.Double(x, y);
    }

    @Override public String toString() {
        return rep.toString();
    }

}
