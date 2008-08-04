package net.von_gagern.martin.confoo.opt;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import org.apache.log4j.Logger;

/**
 * Newton method for Convex Optimization.<p>
 *
 * This class implements Newton method for convex optimization with
 * backtracking line search. The details can be found in the book
 * Convex Optimization by Boyd and Vanderberghe which is available
 * online.
 *
 * @see <a href="http://www.stanford.edu/~boyd/cvxbook/">Convex Optimization by Boyd and Vandenberghe</a>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class Newton implements Runnable {

    /**
     * Enumeration of possible reasons for the termination of an
     * optimization.<p>
     *
     * This enumeration identifies the possible reasons that can cause
     * the optimization to terminate without throwing an exception.
     *
     * @see Newton#getExitCondition()
     *
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     * @since 1.0
     */
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

    /**
     * Default value to be used for all error bounds.
     * @see #setEpsilon
     */
    private static final double EPSILON = 1e-14;

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(Newton.class);

    /**
     * The functional to optimize.
     */
    private final Functional f;

    /**
     * The starting point for the optimization.
     */
    private Vector startingPoint;

    /**
     * The solver used to solve systems of linear equaltions.
     */
    private IterativeSolver solver;

    /**
     * Parameter for backtracking line search.
     * This factor gives the proportion of the value change predicted
     * by the first order derivative that a line search value must be
     * below in order for the search to terminate. 0 < alpha < 0.5.
     */
    private double alpha = 0.25;

    /**
     * Backtracking line search step factor.
     * This factor determines the step size for backtracking line
     * search. The step size will be multiplied by this factor in each
     * iteration. 0 < beta < 1.
     */
    private double beta = 0.75;

    /**
     * Minimum backtracking line search step size factor.
     * This determines the minimum step size used during backtracking
     * line search, in units of the undamped newton step.
     * 0 <= gamma <= beta.
     */
    private double gamma = 0.01;

    /**
     * The norm used to evaluate the residual gradient value.
     * @see #setNorm
     * @see ExitCondition#GRADIENT
     */
    private Vector.Norm gradNorm = Vector.Norm.Two;

    /**
     * The norm used to evaluate the step size in input space.
     * @see #setNorm
     * @see ExitCondition#DELTA
     */
    private Vector.Norm deltaNorm = Vector.Norm.Two;

    /**
     * The boundary on gradient value below which optimization
     * terminates.
     * @see #setEpsilon
     * @see ExitCondition#GRADIENT
     */
    private double gradEpsilon = EPSILON;

    /**
     * The boundary on the estimated value change below which
     * optimization terminates.
     * @see #setEpsilon
     * @see ExitCondition#ESTIMATE
     */
    private double estimateEpsilon = EPSILON;

    /**
     * The boundary on the optimization step size below which
     * optimization terminates.
     * @see #setEpsilon
     * @see ExitCondition#DELTA
     */
    private double deltaEpsilon = EPSILON;

    /**
     * The maximum number of iterations to perform.
     * @see #setMaxIterations
     * @see ExitCondition#ITERATIONS
     */
    private int maxIterations = Integer.MAX_VALUE;

    /**
     * The condition that caused the last optimization to terminate.
     */
    private ExitCondition exitCondition;

    /**
     * The residual error from the most recent optimization.
     */
    private double exitError;

    /**
     * The final argument that resulted in the approximately optimal
     * solution of the most recent optimization.
     */
    private Vector argmin;

    /**
     * Construct new optimizer for given functional.<p>
     *
     * As future implementations might introduce specialized derived
     * classes for special kinds of problems, this constructor is
     * protected. Users should instead call the factory Method
     * <code>getInstance</code> to construct an appropriate instance.
     *
     * @param f the functional to be optimized
     * @see #getInstance
     */
    private Newton(Functional f) {
        this.f = f;
        int size = f.getInputDimension();
        solver = new CG(new DenseVector(size));
    }

    /**
     * Construct new optimizer for given functional.<p>
     *
     * @param f the functional to be optimized
     * @return an optimizer for the given functional
     */
    public static Newton getInstance(Functional f) {
        return new Newton(f);
    }

    /**
     * Find approximatively optimal solution.<p>
     *
     * This is the main method of the optimizer. It uses settings made
     * by previous call to varous configuration methods, and makes its
     * results available to various result access methods.<p>
     *
     * When this method terminates, the most recent call to
     * <code>setArgument</code> for the underlying functional will
     * have been for the found optimum. Thus the functional itself can
     * be queried to get the optimal value, residual gradient and so
     * on.
     *
     * @throws IterativeSolverNotConvergedException if the Newton step
     *         could not be determined, e.g. because the functional is
     *         not convex.
     * @see Functional#setArgument(Vector)
     */
    public void optimize() throws IterativeSolverNotConvergedException {
        int size = f.getInputDimension();
        Vector x = new DenseVector(size);
        Vector x2 = new DenseVector(size);
        Vector delta = new DenseVector(size);
        Vector g = null;
        Matrix h = null;

        // initialization
        setExitCondition(null, Double.NaN);
        if (startingPoint != null)
            x.set(startingPoint);

        logger.debug("Starting optimization");
        f.setArgument(x);                             // working on f(x) now
        for (int i = 1; i <= maxIterations; ++i) {
            logger.debug("Iteration " + i);
            g = f.gradient(g);                        // g = grad f(x)
            double gradNormValue = g.norm(gradNorm);
            logger.debug("Gradient norm: " + gradNormValue);
            if (gradNormValue <= gradEpsilon) {
                setExitCondition(ExitCondition.GRADIENT, gradNormValue);
                argmin = x;
                return;
            }
            h = f.hessian(h);                         // h = Hess f(x)
            g = g.scale(-1);                          // g = - grad f(x)
            double v = f.value();                     // v = f(x)
            logger.debug("Function value: " + v);
            delta = solver.solve(h, g, delta.zero()); // h*delta = g
            double lamdaSq = g.dot(delta);            // lamda² = <g, delta>
            logger.debug("lambda^2: " + lamdaSq);
            if (lamdaSq/2 <= estimateEpsilon) {
                setExitCondition(ExitCondition.ESTIMATE, lamdaSq/2);
                argmin = x;
                return;
            }

            // Backtracking line search
            double deltaNormValue = delta.norm(deltaNorm);
            logger.debug("Delta norm: " + deltaNormValue);
            for (double t = 1; true; t *= beta) {
                if (t < gamma) t = gamma;
                logger.debug("Line search t: " + t);
                if (t*deltaNormValue <= deltaEpsilon) {
                    setExitCondition(ExitCondition.DELTA, t*deltaNormValue);
                    argmin = x;
                    return;
                }
                x2.set(x);
                x2.add(t, delta);                     // x2 = x + t*delta
                f.setArgument(x2);
                double change = f.valueChange();
                if (change <= alpha*t*lamdaSq || t == gamma)
                    break;
            }
            Vector x1 = x2;                           // swap x and x2
            x2 = x;
            x = x1;
            // f.setArgument(x2) was already called inside line search, so we
            // don't need to set the argument again for the next iteration
        }
        setExitCondition(ExitCondition.ITERATIONS, maxIterations);
        argmin = x;
    }

    /**
     * Internal helper method to register exit condition.
     * @param condition the condition that caused the optimization to
     *                  terminate
     * @param error the residual error when the optimization terminated
     */
    private void setExitCondition(ExitCondition condition, double error) {
        logger.info("Condition: " + condition +", error: " + error);
        exitCondition = condition;
        exitError = error;
    }

    /**
     * Get cause for the termination of the most recent optimization.
     * @return enum value representing the cause of termination
     */
    public ExitCondition getExitCondition() {
        return exitCondition;
    }

    /**
     * Get residual error for exit condition.
     *
     * The meaning of this value depends on the exit condition:
     * <dl>
     * <dt>GRADIENT</dt><dd>Norm of the gradient</dd>
     * <dt>ESTIMATE</dt><dd>Estimated residual value error</dd>
     * <dt>DELTA</dt><dd>Norm of the step in input space</dd>
     * <dt>ITERATIONS</dt><dd>Number of iterations actually
     * performed</dd>
     * </dl>
     * @return residual error as specified above
     * @see #getExitCondition()
     */
    public double getExitError() {
        return exitError;
    }

    /**
     * Get position of critical point.
     * @return the argument that lead to the found solution
     */
    public Vector getArgMin() {
        return argmin;
    }

    /**
     * Set error bound for given termination condition.
     * @param cond one of <code>GRADIENT</code>, <code>DELTA</code> or
     * <code>ESTIMATE</code>
     * @param epsilon the new bound to be set
     * @throws IllegalArgumentException for other conditions
     */
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

    /**
     * Set the norm used to compare a vector with an error bound.
     * @param cond one of <code>GRADIENT</code> or <code>DELTA</code>
     * @param norm the norm to evaluate for the specified vectors
     * @see #setEpsilon
     */
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

    /**
     * Set maximum number of iterations.
     * @param max the new maximum number of iterations
     * @see ExitCondition#ITERATIONS
     */
    public void setMaxIterations(int max) {
        if (max < 1)
            throw new IllegalArgumentException("max must be at least 1");
        maxIterations = max;
    }

    /**
     * Set parameters for backtracking line search.<p>
     *
     * Once the Newton step is determined, the actual change in
     * function value at the end of the step is compared with the
     * change predicted by the gradient. The actual difference has to
     * be at least alpha times the predicted difference. Otherwise the
     * step size is reduced by multiplication with beta. The parameter
     * gamma defines a minimal proportion of the original step size;
     * after this has been reached the line search will terminate no
     * matter what.
     *
     * @param alpha factor by which to multiply predicted change.
     *              0 < alpha < 0.5
     * @param beta factor to reduce step size. 0 < beta < 1
     * @param gamma minimum step size factor. 0 <= gamma <= beta
     */
    public void lineSearchParameters(double alpha, double beta, double gamma) {
        if (alpha <= 0 || alpha >= 0.5)
            throw new IllegalArgumentException("0 < alpha < 0.5");
        if (beta <= 0 || beta >= 1)
            throw new IllegalArgumentException("0 < beta < 1");
        if (gamma < 0 || gamma > beta)
            throw new IllegalArgumentException("0 <= gamma <= beta");
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    /**
     * Runnable interface to the <code>optimize</code> method.<p>
     *
     * This method allows performing the optimization in a different
     * thread. However, as a <code>run</code> method may not throw any
     * checked exceptions, special care has to be taken to catch these
     * exceptions later on. The main thread that was waiting for the
     * result should call <code>throwInterceptedExceptions</code> to
     * re-throw any exceptions that occurred during execution in a
     * different thread.<p>
     *
     * Any application not using multiple threads should rather call
     * <code>optimize</code> directly to deal with exceptions more
     * easily.
     *
     * @throws IllegalStateException if there is an uncleared
     *         exception from a previous invocation
     * @see #optimize()
     * @see #throwInterceptedExceptions()
     */
    public void run() {
        if (isncException != null)
            throw new IllegalStateException("Uncleared exceptions from " +
                                            "previous run");
        try {
            optimize();
        }
        catch (IterativeSolverNotConvergedException e) {
            isncException = e;
        }
    }

    /**
     * Cached exception intercepted by <code>run</code>
     */
    private IterativeSolverNotConvergedException isncException;

    /**
     * Re-throw exceptions intercepted by <code>run</code>.
     * This method must be called after every invocation of
     * <code>run</code> in order to clear and re-throw any exceptions
     * which occurred during the execution of <code>optimize</code>.
     * @see #run()
     */
    public void throwInterceptedExceptions()
        throws IterativeSolverNotConvergedException
    {
        IterativeSolverNotConvergedException isncE = isncException;
        if (isncE == null) return;
        isncException = null;
        throw isncE;
    }

}
