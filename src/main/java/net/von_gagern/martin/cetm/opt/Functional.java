package net.von_gagern.martin.cetm.opt;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 * Interface of a functional.<p>
 *
 * An implementation of this interface represents a function taking a
 * fixed number of real input values to compute a single real output
 * value. The gradient (first derivative) and hessian (second
 * derivative) of the function can be calculated as well.<p>
 *
 * The design of this class is intended to faciliate optimization. To
 * calculate the function itself, its gradient or its hessian at a
 * given location in input space, one first has to move the whole
 * function to that location using {@link #setArgument}. Subsequent
 * calls to return given values will always assume that location to be
 * the function input. It is up to the function implementation to
 * split work between <code>setArgument</code> and the other methods
 * in a suitable fashion. An implementation may throw an
 * <code>IllegalStateException</code> if there was no call to
 * <code>setArgument</code> preceding any of the result calculation
 * methods, but is not required to do so.<p>
 *
 * A common way to use this interface woule look like this:
 * <pre>
 * Functional functional = constructFunctional();
 * int size = functional.getInputDimension(size);
 * Vector x = constructInitialValue(size);
 * double value;
 * Vector gradient = null;
 * Matrix hessian = null;
 * while (/&#42; condition &#42;/) {
 *     functional.setArgument(x);
 *     value = functional.value();
 *     gradient = functional.gradient(gradient);
 *     hessian = functional.hessian(hessian);
 *     // work with results, modify x appropriately
 * }
 * </pre>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public interface Functional {

    /**
     * Return the dimension of the input space. This is the number of
     * real argument values the functional takes as an input.
     *
     * @return the number of expected argument values
     */
    public int getInputDimension();

    /**
     * Set function arguments for subsequent calls.
     *
     * @param x the argument vecotor of the function
     */
    public void setArgument(Vector x);

    /**
     * Calculate function value.<p>
     *
     * Calculates the function value at the position given by the most
     * recent call to {@link #setArgument(Vector)}.
     *
     * @return the function value
     * @throws IllegalStateException if there was no preceding call to
     *         <code>setArgument</code>
     */
    public double value();

    /**
     * Calculate change in function value.<p>
     *
     * Calculates the difference between the function value at the
     * position given by the most recent call to
     * {@link #setArgument(Vector)} and the function value returned by
     * the last call to {@link #value}.
     *
     * @return the function value
     * @throws IllegalStateException if there was no preceding call to
     *         <code>setArgument</code> or <code>value</code>
     */
    public double valueChange();

    /**
     * Calculate gradient.<p>
     *
     * Calculates the gradient of the function at the position given
     * by the most recent call to {@link #setArgument(Vector)}.<p>
     *
     * The caller may provide a preallocated vector as an argument,
     * and the implementation of the function may choose whether or
     * not to use that object instead of allocating a new one. Passing
     * <code>null</code> will cause the implementation to always
     * allocate a suitable vector object. The caller is encouraged to
     * pass the result of a previous invocation to reuse such objects.
     *
     * @param g a preallocated vector that may be used for the result
     * @return the gradient of the function
     * @throws IllegalStateException if there was no preceding call to
     *         <code>setArgument</code>
     */
    public Vector gradient(Vector g);

    /**
     * Calculate hessian.<p>
     *
     * Calculates the function value at the position given by the most
     * recent call to {@link #setArgument(Vector)}.
     *
     * The caller may provide a preallocated matrix as an argument,
     * and the implementation of the function may choose whether or
     * not to use that object instead of allocating a new one. Passing
     * <code>null</code> will cause the implementation to always
     * allocate a suitable matrix object. The caller is encouraged to
     * pass the result of a previous invocation to reuse such objects.
     *
     * @param h a preallocated matrix that may be used for the result
     * @return the hessian of the function
     * @throws IllegalStateException if there was no preceding call to
     *         <code>setArgument</code>
     */
    public Matrix hessian(Matrix h);

}
