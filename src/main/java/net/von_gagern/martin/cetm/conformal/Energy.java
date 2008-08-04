package net.von_gagern.martin.cetm.conformal;

import java.util.Arrays;
import java.util.Collection;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSPDPackMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import net.von_gagern.martin.cetm.fun.Clausen;
import net.von_gagern.martin.cetm.opt.Functional;
import net.von_gagern.martin.cetm.opt.Newton;
import org.apache.log4j.Logger;

/**
 * Convex energy funtion used to adjust edge lengths.<p>
 *
 * This implementation will give a result that is by a factor of two
 * larger than the one given by Springborn, Schröder and Pinkall in
 * their paper. This has no effect on the argmin of the critical
 * point, but will save a few floating point operations.<p>
 *
 * In <code>setArgument(Vector)</code> the internal representation of
 * the mesh is updated to give new lengths and angles. The access
 * functions for value, gradient and hessian then iterate over the all
 * vertices, all angles or both in order to determine the requested
 * values.<p>
 *
 * @see <a href="http://www.multires.caltech.edu/pubs/ConfEquiv.pdf">Conformal Equivalence of Triangle Meshes by Springborn, Schröder and Pinkall</a>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class Energy implements Functional {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(Energy.class);

    /**
     * Collection of all mesh vertices.
     */
    private final Collection<Vertex> vertices;

    /**
     * Collection of all mesh edges.
     */
    private final Collection<Edge> edges;

    /**
     * Collection of all mesh angles.
     */
    private final Collection<Angle> angles;

    /**
     * Input dimension of energy function, equal to number of unfixed vertices.
     */
    private final int size;

    /**
     * Intermediate result from last call to <code>value()</code>.
     * Used for precise difference calculation in
     * <code>valueChange()</code>.
     */
    private double[] lastValueTerms;

    /**
     * Final result from last call to <code>value()</code>.
     * This is here only for debugging purposes, as it is no longer
     * used for the actual calculation of the difference. So it may be
     * reomved once the debug code that uses it is gone.
     */
    private double oldValue;

    /**
     * Construct energy function for given mesh.
     * @param mesh the mesh whose energy should be calculated
     */
    public Energy(InternalMesh<?> mesh) {
        vertices = mesh.getVertices();
        edges = mesh.getEdges();
        angles = mesh.getAngles();

        int index = 0;
        for (Vertex v: vertices) {
            if (v.isFixed()) v.setIndex(-1);
            else v.setIndex(index++);
        }
        if (logger.isDebugEnabled()) {
            for (java.util.Map.Entry<?, Vertex> entry:
                 mesh.getVertexMap().entrySet()) {
                Vertex v = entry.getValue();
                logger.debug(entry.getKey() + " -> " + v.getIndex() +
                             " ( " + v.getKind() + ", " +
                             v.getTarget()*(180./Math.PI) + ")");
            }
        }
        size = index;
    }

    public int getInputDimension() {
        return size;
    }

    /**
     * Set function agrument.<p>
     *
     * This implementation updates the internal mesh representation
     * accordingly, so that subsequent requests can be efficiently
     * answered from those data structures.
     *
     * @param u the function argument vector
     */
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

    /**
     * Calculate value.
     * @return the function value
     */
    public double value() {
        double[] terms = valueTerms();
        lastValueTerms = (double[])terms.clone();
        oldValue = preciseSum(terms);
        return oldValue;
    }

    /**
     * Calculate change in value.
     * @return the change in value since the last call to <code>value</code>
     */
    public double valueChange() {
        // For evaluation purposes we still give the results of both
        // the precise and the dumb calculation of value difference.
        // TODO: For performance this should be cleaned up at some
        // point in the future.
        double[] terms = valueTerms();
        double simpleChange = preciseSum((double[])terms.clone()) - oldValue;
        for (int i = 0; i < terms.length; ++i)
            terms[i] -= lastValueTerms[i];
        double preciseChange = preciseSum(terms);
        logger.debug("valueChange simple: " + simpleChange + ", " +
                     "precise: " + preciseChange);
        return preciseChange;
    }

    /**
     * Calculate gradient.
     * @param g a preallocated vector that may be used to receive the
     *          result, or <code>null</code>
     * @return the gradient of the energy
     */
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

    /**
     * Calculate hessian.
     * @param h a preallocated matrix that may be used to receive the
     *          result, or <code>null</code>
     * @return the hessian of the energy
     */
    public Matrix hessian(Matrix h) {
        if (h == null) h = new LowerSPDPackMatrix(getInputDimension());
        else h.zero();
        for (Angle a: angles) {
            double alpha = a.angle();
            double cot = Math.cos(alpha)/Math.sin(alpha);
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

    /**
     * Calculate individual terms whose sum make up the function
     * value. Clever handling of those terms allows for more precise
     * calculation of function values and especially differences of
     * such values.
     * @return terms whose sum make up the function value
     */
    private double[] valueTerms() {
        double[] terms = new double[angles.size() + vertices.size()];
        int nterms = 0;
        for (Angle a: angles) {
            double alpha = a.angle();
            double lamda = a.oppositeEdge().logLength();
            double cl2 = Clausen.cl2(2*alpha);
            double u = a.vertex().getU();
            double term = alpha*lamda + cl2 - Math.PI*u;
            assert !Double.isInfinite(term): "infinite term in value";
            assert !Double.isNaN(term): "NaN term in value";
            terms[nterms++] = term;
        }
        for (Vertex v: vertices) {
            double term = v.getTarget()*v.getU();
            assert !Double.isInfinite(term): "infinite term in value";
            assert !Double.isNaN(term): "NaN term in value";
            terms[nterms++] = term;
        }
        assert nterms == terms.length: "Wrong number of terms";
        return terms;
    }

    /**
     * Calculate sum of an array of terms in a clever way.
     * The sum is calculates in such a way as to keep errors for lack
     * of precision low and have large terms cancel each other early
     * on. The parameter array will be modified (i.e. sorted).
     * @param terms the terms to sum over
     * @return the sum of these terms
     */
    private double preciseSum(double[] terms) {
        Arrays.sort(terms);
        double sum = 0;
        if (terms[0] >= 0) { // all non-negative
            for (int i = 0; i < terms.length; ++i)
                sum += terms[i];
        }
        else if (terms[terms.length - 1] <= 0) { // all non-positive
            for (int i = terms.length - 1; i >= 0; --i)
                sum += terms[i];
        }
        else { // mixed sign
            for (int left = 0, right = terms.length; left < right; ) {
                if (sum >= 0) sum += terms[left++];
                else sum += terms[--right];
            }
        }
        return sum;
    }

}
