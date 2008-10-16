package net.von_gagern.martin.confoo.conformal;

import java.util.Arrays;
import java.util.Collection;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSPDPackMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import net.von_gagern.martin.confoo.fun.Clausen;
import net.von_gagern.martin.confoo.opt.Functional;
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
 * @see <a href="http://dx.doi.org/10.1145/1399504.1360676">Conformal Equivalence of Triangle Meshes by Springborn, Schröder and Pinkall</a>
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
    protected final Collection<Vertex> vertices;

    /**
     * Collection of all mesh edges.
     */
    protected final Collection<Edge> edges;

    /**
     * Collection of all mesh angles.
     */
    protected final Collection<Angle> angles;

    /**
     * Collection of all mesh triangles.
     */
    protected final Collection<Triangle> triangles;

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
        triangles = mesh.getTriangles();

        int index = 0;
        for (Vertex v: vertices) {
            if (v.fixed) v.index = -1;
            else v.index = index++;
        }
        if (logger.isDebugEnabled()) {
            for (java.util.Map.Entry<?, Vertex> entry:
                 mesh.getVertexMap().entrySet()) {
                Vertex v = entry.getValue();
                logger.debug(entry.getKey() + " -> " + v.index +
                             " ( " + v.kind + ", " +
                             v.target*(180./Math.PI) + ")");
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
            int i = v.index;
            if (i >= 0)
                v.u = u.get(i);
        }
        for (Edge e: edges) {
            update(e);
        }
        for (Angle a: angles) {
            update(a);
        }
    }

    /**
     * Calculate value.
     * @return the function value
     */
    public double value() {
        double[] terms = valueTerms();
        oldValue = preciseSum(terms);
        lastValueTerms = terms;
        return oldValue;
    }

    /**
     * Calculate change in value.
     * @return the change in value since the last call to <code>value</code>
     */
    public double valueChange() {
        // For evaluation purposes we still give the results of both
        // the precise and the dumb calculation of value difference.
        double[] terms = valueTerms();
        Arrays.sort(terms);
        double simpleChange = Double.NaN;
        if (logger.isDebugEnabled())
            simpleChange = preciseSum(terms) - oldValue;
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
            int i = v.index;
            if (i >= 0)
                g.add(i, v.target);
        }
        for (Angle a: angles) {
            int i = a.vertex.index;
            if (i >= 0)
                g.add(i, -a.angle);
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
            double alpha = a.angle;
            if (alpha <= 0 || alpha >= Math.PI) continue;
            double cot = Math.cos(alpha)/Math.sin(alpha);
            double cot2 = cot/2;
            int i = a.nextVertex.index;
            int j = a.prevVertex.index;
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
     * Scale solution.
     * Some boundary conditions lead to an arbitrarily scaled
     * solution. A nicer solution would be one that gives zero as the
     * sum over all <i>u</i>, i.e. all logarithmic length
     * changes. This postprocessing step achieves the correct scale.
     */
    public void scale() {
        logger.debug("Scaling result");
        double sum = 0;
        for (Vertex v: vertices)
            sum += v.u;
        double diff = -sum/vertices.size();
        for (Vertex v: vertices)
            v.u += diff;
        for (Edge e: edges)
            update(e);
        for (Angle a: angles)
            update(a);
    }

    /**
     * Update edge length from vertex length factors.
     * @param e the edge to be updated
     */
    protected void update(Edge e) {
        e.logLength = e.origLogLength + e.v1.u + e.v2.u;
        assert !Double.isInfinite(e.logLength): "logLength is infinite";
        assert !Double.isNaN(e.logLength): "logLength is NaN";
        e.length = lamdaToLength(e.logLength);
        assert e.length > 0: "length must stay positive (" + e.logLength + ")";
    }

    protected double lamdaToLength(double lamda) {
        return Math.exp(lamda/2.);
    }

    /**
     * Update angle value from edge lengths.
     * @param a the angle to be updated
     */
    protected void update(Angle a) {
        double lo = a.oppositeEdge.length;
        double ln = a.nextEdge.length;
        double lp = a.prevEdge.length;

        // handle violations of triangle inequality
        if (lo >= ln + lp) {
            a.angle = Math.PI;
            return;
        }
        if (ln >= lo + lp || lp >= lo + ln) {
            a.angle = 0;
            return;
        }

        // calculate angle using half-angle formula
        double nom = lengthAngleFactor(ln + lo - lp);
        nom *= lengthAngleFactor(lo + lp - ln);
        double denom = lengthAngleFactor(lp + ln - lo);
        denom *= lengthAngleFactor(lo + lp + ln);
        if (nom <= denom)
            a.angle = 2.*Math.atan(Math.sqrt(nom/denom));
        else
            a.angle = Math.PI - 2.*Math.atan(Math.sqrt(denom/nom));
        assert !Double.isInfinite(a.angle): "angle is infinite";
        assert !Double.isNaN(a.angle): "angle is NaN";
    }

    /**
     * Value representing an length in the half-angle formula.
     * In euclidean geometry this is the passed value itself.
     * @param the length to be handled
     * @return the factor corresponding to that length
     */
    protected double lengthAngleFactor(double length) {
        return length;
    }

    /**
     * Calculate individual terms whose sum make up the function
     * value. Clever handling of those terms allows for more precise
     * calculation of function values and especially differences of
     * such values.
     * @return terms whose sum make up the function value
     */
    protected double[] valueTerms() {
        double[] terms = new double[3*angles.size() + vertices.size()];
        int nterms = 0;
        for (Angle a: angles) {
            double alpha = a.angle;
            double lamda = a.oppositeEdge.logLength;
            double cl2 = Clausen.cl2(2*alpha);
            double u = a.vertex.u;
            terms[nterms++] = alpha*lamda;
            terms[nterms++] = cl2;
            terms[nterms++] = -Math.PI*u;
        }
        for (Vertex v: vertices) {
            double term = v.target*v.u;
            terms[nterms++] = term;
        }
        assert nterms == terms.length: "Wrong number of terms";
        return terms;
    }

    /**
     * Calculate sum of an array of terms in a clever way.
     * The sum is calculated in such a way as to keep errors for lack
     * of precision low and have large terms cancel each other early
     * on. The parameter array will be modified (i.e. sorted).
     * @param terms the terms to sum over
     * @return the sum of these terms
     */
    private double preciseSum(double[] terms) {
        Arrays.sort(terms);
        assert !Double.isInfinite(terms[0]);
        assert !Double.isInfinite(terms[terms.length - 1]);
        assert !Double.isNaN(terms[terms.length - 1]);
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
