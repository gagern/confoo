package net.von_gagern.martin.cetm.opt;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

public interface Functional {

    public int getInputDimension();

    public void setArgument(Vector x);

    public double value();

    public Vector gradient(Vector g);

    public Matrix hessian(Matrix h);

}
