package net.von_gagern.martin.cetm.opt;

import java.util.concurrent.Callable;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import org.apache.log4j.Logger;

public class Newton implements Callable<Vector> {

    public enum ExitCondition {

        /**
         * Exit due to a small gradient.
         * Optimization terminates for this reason if the norm of the
         * gradient comes within the specified bound.
         */
        GRADIENT,

        /**
         * Exit due to a low estimate of residual value error.
         * Optimization terminates for this reason if the estimation
         * of residual error value based on the second order
         * approximation comes within the specified bound.
         */
        ESTIMATE,

        /**
         * Exit due to a small change in parameter space.
         * Optimization terminates for this reason if the change of
         * the function argument vector comes within the specified
         * bound.
         */
        DELTA,

        /**
         * Exit due to maximum iteration count.
         * Optimization terminates for this reason if the maximum
         * number of iterations has been reached.
         */
        ITERATIONS,

    }

    private static final double EPSILON = 1e-14;

    private final Logger logger = Logger.getLogger(Newton.class);

    private final Functional f;

    private Vector startingPoint;

    private IterativeSolver solver;

    private double alpha = 0.25;

    private double beta = 0.75;

    private Vector.Norm gradNorm = Vector.Norm.Two;

    private Vector.Norm deltaNorm = Vector.Norm.Two;

    private double gradEpsilon = EPSILON;

    private double estimateEpsilon = EPSILON;

    private double deltaEpsilon = EPSILON;

    private int maxIterations = Integer.MAX_VALUE;

    private ExitCondition exitCondition;

    private double exitError;

    private Vector argmin;

    private Newton(Functional f) {
        this.f = f;
        int size = f.getInputDimension();
        solver = new CG(new DenseVector(size));
    }

    public static Newton getInstance(Functional f) {
        return new Newton(f);
    }

    public Vector optimize() throws IterativeSolverNotConvergedException {
        int size = f.getInputDimension();
        Vector x = new DenseVector(size);
        Vector x2 = new DenseVector(size);
        Vector delta = new DenseVector(size);
        Vector g = null;
        Matrix h = null;

        // initialization
        if (startingPoint != null)
            x.set(startingPoint);

        logger.debug("Starting optimization");
        f.setArgument(x);                             // working on f(x) now
        for (int i = 1; i <= maxIterations; ++i) {
            logger.debug("Iteration " + i);
            g = f.gradient(g);                        // g = ∇f(x)
            double gradNormValue = g.norm(gradNorm);
            logger.debug("Gradient norm: " + gradNormValue);
            if (gradNormValue <= gradEpsilon) {
                setExitCondition(ExitCondition.GRADIENT, gradNormValue);
                return argmin = x;
            }
            h = f.hessian(h);                         // h = ∇²f(x)
            g = g.scale(-1);                          // g = -∇f(x)
            double v = f.value();                     // v = f(x)
            logger.debug("Function value: " + v);
            delta = solver.solve(h, g, delta.zero()); // h*Δ = g
            double lamdaSq = g.dot(delta);            // λ² = <g, Δ>
            logger.debug("lambda^2: " + lamdaSq);
            if (lamdaSq/2 <= estimateEpsilon) {
                setExitCondition(ExitCondition.ESTIMATE, lamdaSq/2);
                return argmin = x;
            }

            // Backtracking line search
            double deltaNormValue = delta.norm(deltaNorm);
            logger.debug("Delta norm: " + deltaNormValue);
            for (double t = 1; true; t *= beta) {
                logger.debug("Line search t: " + t);
                if (t*deltaNormValue <= deltaEpsilon) {
                    setExitCondition(ExitCondition.DELTA, t*deltaNormValue);
                    return argmin = x;
                }
                x2.set(x);
                x2.add(t, delta);                     // x2 = x + tΔ
                f.setArgument(x2);
                double v2 = f.value();
                if (v2 < v + alpha*t*lamdaSq)
                    break;
            }
            Vector x1 = x2;                           // swap x and x2
            x2 = x;
            x = x1;
            // f.setArgument(x2) was already called inside line search, so we
            // don't need to set the argument again for the next iteration
        }
        setExitCondition(ExitCondition.ITERATIONS, maxIterations);
        return argmin = x;
    }

    private void setExitCondition(ExitCondition condition, double error) {
        logger.info("Condition: " + condition +", error: " + error);
        exitCondition = condition;
        exitError = error;
    }

    public ExitCondition getExitCondition() {
        return exitCondition;
    }

    public double getExitError() {
        return exitError;
    }

    public Vector getArgMin() {
        return argmin;
    }

    public void setEpsilon(ExitCondition cond, double epsilon) {
        if (epsilon < 0)
            throw new IllegalArgumentException("Epsilon may not be negative");
        switch(cond) {
        case GRADIENT:
            gradEpsilon = epsilon;
            break;
        case DELTA:
            deltaEpsilon = epsilon;
            break;
        case ESTIMATE:
            estimateEpsilon = epsilon;
            break;
        default:
            throw new IllegalArgumentException("Exit condition " + cond +
                " has no associated epsilon bound");
        }
    }

    public void setNorm(ExitCondition cond, Vector.Norm norm) {
        if (norm == null)
            throw new NullPointerException("norm must not be null");
        switch(cond) {
        case GRADIENT:
            gradNorm = norm;
            break;
        case DELTA:
            deltaNorm = norm;
            break;
        default:
            throw new IllegalArgumentException("Exit condition " + cond +
                " has no associated norm");
        }
    }

    public void setMaxIterations(int max) {
        if (max < 1)
            throw new IllegalArgumentException("max must be at least 1");
        maxIterations = max;
    }

    public void lineSearchParameters(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public Vector call() {
        try {
            return optimize();
        }
        catch (IterativeSolverNotConvergedException e) {
            error = e;
            return null;
        }
    }

    private IterativeSolverNotConvergedException error;

    public void throwInterceptedError()
        throws IterativeSolverNotConvergedException
    {
        IterativeSolverNotConvergedException error = this.error;
        if (error == null) return;
        this.error = null;
        throw error;
    }

    public Throwable getError() {
        return error;
    }

    public void clearError() {
        error = null;
    }

}
