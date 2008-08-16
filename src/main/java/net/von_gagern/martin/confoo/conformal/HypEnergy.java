package net.von_gagern.martin.confoo.conformal;

import no.uib.cipr.matrix.LowerSPDPackMatrix;
import no.uib.cipr.matrix.Matrix;
import net.von_gagern.martin.confoo.fun.Clausen;
import org.apache.log4j.Logger;

/**
 * Convex energy funtion used to adjust edge lengths in hyperbolic
 * geometry.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.1
 */
class HypEnergy extends Energy {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(HypEnergy.class);

    /**
     * Construct hyperbolic energy function for given mesh.
     * @param mesh the mesh whose energy should be calculated
     */
    public HypEnergy(InternalMesh<?> mesh) {
        super(mesh);
    }

    @Override public void scale() {
        return; // Hyperbolic geometry always has fixed scale
    }

    @Override protected double lamdaToLength(double lamda) {
        return 2.*arsinh(Math.exp(lamda/2.));
    }

    /**
     * Value representing an length in the half-angle formula.
     * In hyperbolic geometry for a length l this is the value
     * sinh(l/2).
     * @param the length to be handled
     * @return the factor corresponding to that length
     */
    @Override protected double lengthAngleFactor(double length) {
        return Math.sinh(length/2.);
    }

    /**
     * Calculate individual terms whose sum make up the function
     * value. Clever handling of those terms allows for more precise
     * calculation of function values and especially differences of
     * such values.
     * @return terms whose sum make up the function value
     */
    @Override protected double[] valueTerms() {
        double[] terms = new double[4*angles.size() +
                                    triangles.size() +
                                    vertices.size()];
        int nterms = 0;
        for (Angle a: angles) {
            double alpha = a.angle;
            double lamda = a.oppositeEdge.logLength;
            double u = a.vertex.u;
            double beta = (Math.PI + alpha - a.nextAngle.angle -
                           a.nextAngle.nextAngle.angle)/2.;

            terms[nterms++] = -alpha*u;
            terms[nterms++] = beta*lamda;
            terms[nterms++] = Clausen.cl2(2*alpha)/2.;
            terms[nterms++] = Clausen.cl2(2*beta)/2.;
        }
        for (Triangle t: triangles) {
            double sum = Math.PI;
            for (Angle a: t.getAngles())
                sum -= a.angle;
            terms[nterms++] = Clausen.cl2(sum)/2.;
        }
        for (Vertex v: vertices) {
            terms[nterms++] = v.target*v.u;
        }
        assert nterms == terms.length: "Wrong number of terms";
        return terms;
    }

    /**
     * Calculate hessian.
     * @param h a preallocated matrix that may be used to receive the
     *          result, or <code>null</code>
     * @return the hessian of the energy
     */
    @Override public Matrix hessian(Matrix h) {
        if (h == null) h = new LowerSPDPackMatrix(getInputDimension());
        else h.zero();
        for (Angle a: angles) {
            double alpha = a.angle;
            if (alpha <= 0 || alpha >= Math.PI) continue;
            double beta = (Math.PI + alpha - a.nextAngle.angle -
                           a.nextAngle.nextAngle.angle)/2.;
            double l = a.oppositeEdge.length;
            double cot = Math.cos(beta)/Math.sin(beta);
            double tanh = Math.tanh(l/2);
            double tanhSq = tanh*tanh;
            double diag = cot*(tanhSq + 1);
            double nonDiag = cot*(tanhSq - 1);
            int i = a.nextVertex.index;
            int j = a.prevVertex.index;
            if (i >= 0)
                h.add(i, i, diag);
            if (j >= 0) {
                h.add(j, j, diag);
                if (i >= 0) {
                    h.add(i, j, nonDiag);
                    h.add(j, i, nonDiag);
                }
            }
        }
        return h;
    }

    /**
     * Area sine or inverse hyperbolic sine function.
     * The value is calculated as
     * arsinh(x) = ln(x + sqrt(x<sup>2</sup> + 1)).
     * @param x a value
     * @return arsinh x
     * @see Math#sinh(double)
     */
    public static double arsinh(double x) {
        return Math.log(x + Math.sqrt(x*x + 1.));
    }

}
