package net.von_gagern.martin.cetm.conformal;

import java.util.Collection;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSPDPackMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import net.von_gagern.martin.cetm.fun.Clausen;
import net.von_gagern.martin.cetm.opt.Functional;
import net.von_gagern.martin.cetm.opt.Newton;

class Energy implements Functional {

    private final Collection<Vertex> vertices;

    private final Collection<Edge> edges;

    private final Collection<Angle> angles;

    private final int size;

    public Energy(InternalMesh<?> mesh) {
        vertices = mesh.getVertices();
        edges = mesh.getEdges();
        angles = mesh.getAngles();

        int index = 0;
        for (Vertex v: vertices) {
            if (v.isFixed()) v.setIndex(-1);
            else v.setIndex(index++);
        }
        size = index;
    }

    public int getInputDimension() {
        return size;
    }

    public void setArgument(Vector u) {
        for (Vertex v: vertices) {
            int i = v.getIndex();
            if (i >= 0)
                v.setU(u.get(i));
        }
        for (Edge e: edges) {
            e.update();
        }
        for (Angle a: angles) {
            a.update();
        }
    }

    public double value() {
        double res = 0;
        for (Angle a: angles) {
            double alpha = a.angle();
            double lamda = a.oppositeEdge().logLength();
            double cl2 = Clausen.cl2(2*alpha);
            double u = a.vertex().getU();
            res += alpha*lamda + cl2 + Math.PI*u;
        }
        for (Vertex v: vertices) {
            res += v.getTarget()*v.getU();
        }
        return res;
    }

    public Vector gradient(Vector g) {
        if (g == null) g = new DenseVector(getInputDimension());
        else g.zero();
        for (Vertex v: vertices) {
            int i = v.getIndex();
            if (i >= 0)
                g.add(i, v.getTarget());
        }
        for (Angle a: angles) {
            int i = a.vertex().getIndex();
            if (i >= 0)
                g.add(i, -a.angle());
        }
        return g;
    }

    public Matrix hessian(Matrix h) {
        if (h == null) h = new LowerSPDPackMatrix(getInputDimension());
        else h.zero();
        for (Angle a: angles) {
            double alpha = a.angle();
            double cot = Math.sin(alpha)/Math.cos(alpha);
            double cot2 = cot/2;
            int i = a.nextVertex().getIndex();
            int j = a.prevVertex().getIndex();
            if (i >= 0)
                h.add(i, i, cot2);
            if (j >= 0) {
                h.add(j, j, cot2);
                if (i >= 0) {
                    h.add(i, j, -cot2);
                    h.add(j, i, -cot2);
                }
            }
        }
        return h;
    }

}
