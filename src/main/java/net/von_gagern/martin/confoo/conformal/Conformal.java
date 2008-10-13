package net.von_gagern.martin.confoo.conformal;

import java.util.Map;
import java.util.concurrent.Callable;
import net.von_gagern.martin.confoo.mesh.CorneredTriangle;
import net.von_gagern.martin.confoo.mesh.LocatedMesh;
import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.MetricMesh;
import net.von_gagern.martin.confoo.mesh.TriangleInequalityException;
import net.von_gagern.martin.confoo.opt.Newton;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import org.apache.log4j.Logger;

/**
 * Conformal transformations of triangle meshes.<p>
 *
 * A typical code snippet would look like this:
 * <pre>
 * Conformal&lt;VertexType&gt; c = {@link #getInstance Conformal.getInstance}(mesh);
 * c.{@link #fixedBoundaryCurvature fixedBoundaryCurvature}(angles);
 * c.{@link #transform() transform}();
 * </pre>
 *
 * @param <V> the class used to represent vertices of the mesh
 * @see <a href="http://dx.doi.org/10.1145/1399504.1360676">Conformal Equivalence of Triangle Meshes by Springborn, Schröder and Pinkall</a>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class Conformal<V> implements Callable<ResultMesh<V>> {

    /*********************************************************************
     * Member variables
     ********************************************************************/

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(Conformal.class);

    /**
     * Representation of the mesh being transformed.
     */
    private InternalMesh<V> mesh;

    /**
     * Boundary condition to apply.
     */
    private BoundaryCondition boundaryCondition;

    /**
     * Bound for the maximum angle error.
     */
    private double angleErrorBound = 2e-14;

    /**
     * Geometry of the input mesh.
     */
    private Geometry inGeometry = Geometry.EUCLIDEAN;

    /**
     * Geometry of the output mesh.
     */
    private Geometry outGeometry = Geometry.EUCLIDEAN;

    /**
     * Start triangle for layout process.
     */
    private Triangle layoutStart;


    /*********************************************************************
     * Costruction
     ********************************************************************/

    /**
     * Construct transformer for given mesh.
     *
     * @param mesh the mesh to be transformed
     */
    private Conformal(MetricMesh<V> mesh) throws MeshException {
        this.mesh = new InternalMesh(mesh);
    }

    /**
     * Convenience factory method.
     * As the result type of this method can be derived from the
     * argument type, you can do without the type argument, in
     * contrast to a direct invocation of the constructor.
     * @param <V> the class used to represent vertices of the mesh
     * @param mesh the mesh to be transformed
     * @return a transformer for the given mesh
     */
    public static <V> Conformal<V> getInstance(MetricMesh<V> mesh)
        throws MeshException
    {
        return new Conformal<V>(mesh);
    }

    /*********************************************************************
     * Configuration
     ********************************************************************/

    /**
     * Set the maximal angle error during transformation.
     * The default is 2e-14.
     * @param epsilon the new error bound
     * @see #getAngleErrorBound()
     */
    public void setAngleErrorBound(double epsilon) {
        angleErrorBound = epsilon;
    }

    /**
     * Get the maximal angle error during transformation.
     * @return the bound for the maximum angle error
     * @see #setAngleErrorBound(double)
     */
    public double getAngleErrorBound() {
        return angleErrorBound;
    }

    /**
     * Set boundary condition to given angles.<p>
     *
     * All vertices contained as keys in the map will be assigned a
     * target angle according to the map value, given in
     * radians. Other vertices will be assigned a target angle of
     * 2&#960; if they in the interior of the mesh or &#960; if they
     * lie on the boundary of the mesh. The result will be a polygon
     * with corner angles specified by the map.
     *
     * @param angles the map of fixed corner angles
     * @see #isometricBoundaryCondition()
     */
    public void fixedBoundaryCurvature(Map<? extends V, Double> angles) {
        boundaryCondition = new FixedBoundaryCurvature<V>(angles,
                                                          mesh.getVertexMap());
    }

    /**
     * Set boundary condition for preserved boundary edge lengths.<p>
     *
     * All boundary and corner vertices will be fixed in order to keep the
     * lengths of boundary edges unmodified. Internal vertices will be
     * assigned a target angle sum of 2&#960;, resulting in a flat mesh.
     *
     * @see #fixedBoundaryCurvature(Map)
     */
    public void isometricBoundaryCondition() {
        boundaryCondition = new IsometricBoundaryCondition();
    }

    /**
     * Get the currently configured geometry of the input mesh.
     * @since 1.1
     * @see #setInputGeometry
     * @see #getOutputGeometry
     */
    public Geometry getInputGeometry() {
        return inGeometry;
    }

    /**
     * Set the geometry of the input mesh.
     * @since 1.1
     * @throws UnsupportedOperationException if the geometry is neither
     *         <code>EUCLIDEAN</code> nor <code>HYPERBOLIC</code>
     * @see #getInputGeometry
     * @see #setOutputGeometry
     */
    public void setInputGeometry(Geometry inputGeometry) {
        switch (inputGeometry) {
        case EUCLIDEAN:
        case HYPERBOLIC:
            inGeometry = inputGeometry;
            break;
        default:
            throw new UnsupportedOperationException("Input geometry " +
                inputGeometry + " not supported yet");
        }
    }

    /**
     * Get the currently configured geometry of the output mesh.
     * @since 1.1
     * @see #setOutputGeometry
     * @see #getInputGeometry
     */
    public Geometry getOutputGeometry() {
        return outGeometry;
    }

    /**
     * Set the geometry of the output mesh.
     * @since 1.1
     * @throws UnsupportedOperationException if the geometry is neither
     *         <code>EUCLIDEAN</code> nor <code>HYPERBOLIC</code>
     * @see #getOutputGeometry
     * @see #setInputGeometry
     */
    public void setOutputGeometry(Geometry outputGeometry) {
        switch (outputGeometry) {
        case EUCLIDEAN:
        case HYPERBOLIC:
            outGeometry = outputGeometry;
            break;
        default:
            throw new UnsupportedOperationException("Output geometry " +
                outputGeometry + " not supported yet");
        }
    }

    /**
     * Set the triangle to be layed out first.
     *
     * This triangle should be chosen to be pretty much in the center
     * of the mesh, as errors increase with distance from this center.
     * If not set, or set to <code>null</code>, a central triangle is
     * automatically determined.
     *
     * @param start the triangle first to be layed out
     * @since 1.1
     */
    public void setLayoutStartTriangle(CorneredTriangle<? extends V> start) {
        layoutStart = null;
        if (start == null) return;
        for (Triangle t: mesh.getTriangles()) {
            ROTATIONS: for (int rotation = 0; rotation < 3; ++rotation) {
                for (int corner = 0; corner < 3; ++corner) {
                    if (!start.getCorner((corner + rotation)%3)
                        .equals(t.getCorner(corner).rep))
                        continue ROTATIONS;
                }
                // rotation matches completely
                layoutStart = t;
                return;
            }
        }
        throw new IllegalArgumentException("Not a triangle of the mesh");
    }


    /*********************************************************************
     * Calculate conformal mapping
     ********************************************************************/

    /**
     * Perform transformation.
     *
     * This is the main method of the class. Before this is invoked,
     * you can tune the transformation process by setting various
     * parameters.<p>
     *
     * The returned object is defined to be a <code>ResultMesh</code>
     * only since version 1.1. Before that it was only specified to be
     * a <code>LocatedMesh</code>.
     *
     * @return the transformed mesh
     * @throws IllegalStateException if no boundary condition was set
     * @throws MeshException if the input mesh is malformed or too degenerate
     * @throws TriangleInequalityException if the result would violate
     *         the triangle inequality
     * @throws NoSuchVertexException if a part of the boundary condition
     *         doesn't apply
     */
    public ResultMesh<V> transform()
        throws MeshException, TriangleInequalityException,
               NoSuchVertexException
    {
        initLamdas();
        boundary();
        lengths();
        triangleInequalities();
        layout();
        return new ResultMesh<V>(mesh);
    }

    /**
     * Initialize logarithmic lengths, taking input geometry into account.
     * @since 1.1
     */
    private void initLamdas() {
        switch (inGeometry) {
        case EUCLIDEAN:
            for (Edge e: mesh.getEdges())
                e.initLamdas(2*Math.log(e.origLength));
            break;
        case HYPERBOLIC:
            for (Edge e: mesh.getEdges())
                e.initLamdas(2*Math.log(Math.sinh(e.origLength/2)));
            break;
        default:
            // should have been prevented by setInputGeometry
            throw new IllegalStateException();
        }
    }

    /**
     * Apply boundary condition.
     * @throws IllegalStateException if no boundary condition was set
     * @throws MeshException if the boundary condition throws this exception
     * @throws NoSuchVertexException if a part of the boundary condition
     *         doesn't apply
     */
    private void boundary() throws MeshException, NoSuchVertexException {
        logger.debug("Assigning boundary conditions");
        if (boundaryCondition == null)
            throw new IllegalStateException("No boundary condition set");
        boundaryCondition.setTargets(mesh, outGeometry);
    }

    /**
     * Adjust edge lengths.
     * This is done by finding the critical part of an energy
     * function.
     * @see #Energy
     * @throws MeshException if the mesh is too degenerate
     */
    private void lengths() throws MeshException {
        logger.debug("Optimizing edge lengths");
        Energy energy = createEnergy();
        Newton newton = Newton.getInstance(energy);
        configureNewton(newton);
        try {
            newton.optimize();
        }
        catch (IterativeSolverNotConvergedException e) {
            throw new MeshException("Could not find optimal solution: " +
                                    e.getReason(), e);
        }
        if (!boundaryCondition.fixedScale())
            energy.scale();
        if (logger.isTraceEnabled())
            for (Edge e: mesh.getEdges())
                logger.trace("Edge length " + e + ": " + e.length);
    }

    /**
     * Check triangle inequalities are fulfilled.
     * @throws TriangleInequalityException if the inequality is violated
     */
    private void triangleInequalities() throws TriangleInequalityException {
        logger.debug("Checking triangle inequalities");
        for (Triangle t: mesh.getTriangles()) {
            for (Angle a: t.getAngles()) {
                if (a.oppositeEdge.length >
                    a.prevEdge.length + a.nextEdge.length) {
                    Object o1 = a.vertex.rep;
                    Object o2 = a.nextVertex.rep;
                    Object o3 = a.prevVertex.rep;
                    throw new TriangleInequalityException(o1, o2, o3);
                }
            }
        }
    }

    /**
     * Turn edge lengths into a consistent layout with vertex
     * coordinates.
     * @throws MeshException if the mesh could not be layed out
     * @see Layout
     */
    private void layout() throws MeshException {
        logger.debug("Creating layout");
        Layout layout = createLayout();
        if (layoutStart != null)
            layout.setStartTriangle(layoutStart);
        layout.layout();
    }

    /**
     * Configure the optimizer used for the transformation.
     * @param newton the optimizer to be configured
     */
    protected void configureNewton(Newton newton) {
        newton.setNorm(Newton.ExitCondition.GRADIENT, Vector.Norm.Infinity);
        newton.setEpsilon(Newton.ExitCondition.GRADIENT, angleErrorBound);
        newton.setEpsilon(Newton.ExitCondition.ESTIMATE, 0);
        newton.setEpsilon(Newton.ExitCondition.DELTA, 0);
        newton.setMaxIterations(128);
    }

    /*********************************************************************
     * Callable interface and error handling
     ********************************************************************/

    /**
     * Cached exception intercepted by <code>call</code>
     */
    private MeshException meshException;

    /**
     * Callable interface to the <code>transform</code> method.<p>
     *
     * This method allows performing the transformation in a different
     * thread. However, as a <code>call</code> method may not throw
     * any checked exceptions, special care has to be taken to catch
     * these exceptions later on. The main thread that was waiting for
     * the result should call <code>throwInterceptedExceptions</code>
     * to re-throw any exceptions that occurred during execution in a
     * different thread.<p>
     *
     * Any application not using multiple threads should rather call
     * <code>transform</code> directly to deal with exceptions more
     * easily.<p>
     *
     * The returned object is defined to be a <code>ResultMesh</code>
     * only since version 1.1. Before that it was only specified to be
     * a <code>LocatedMesh</code>.
     *
     * @throws IllegalStateException if there is an uncleared
     *         exception from a previous invocation
     * @see #transform()
     * @see #throwInterceptedExceptions()
     */
    public ResultMesh<V> call() {
        if (meshException != null)
            throw new IllegalStateException("Uncleared exceptions from " +
                                            "previous run");
        try {
            return transform();
        }
        catch (MeshException e) {
            meshException = e;
            return null;
        }
    }

    /**
     * Re-throw exceptions intercepted by <code>call</code>.
     * This method must be called after every invocation of
     * <code>call</code> in order to clear and re-throw any exceptions
     * which occurred during the execution of <code>transform</code>.
     * @see #call()
     */
    public void throwInterceptedExceptions() throws MeshException {
        MeshException meshE = meshException;
        if (meshE == null) return;
        meshException = null;
        throw meshE;
    }

    /*********************************************************************
     * Geometry-specific object factories
     ********************************************************************/

    /**
     * Create energy function.
     * @return an energy function for the current output geometry
     */
    private Energy createEnergy() {
        switch (outGeometry) {
        case EUCLIDEAN:
            return new Energy(mesh);
        case HYPERBOLIC:
            return new HypEnergy(mesh);
        default:
            // should have been prevented by setOutputGeometry
            throw new IllegalStateException();
        }
    }

    /**
     * Create layouting object.
     * @return a layouting object for the current output geometry
     */
    private Layout createLayout() {
        switch (outGeometry) {
        case EUCLIDEAN:
            return new Layout(mesh);
        case HYPERBOLIC:
            return new HypLayout(mesh);
        default:
            // should have been prevented by setOutputGeometry
            throw new IllegalStateException();
        }
    }

    /*********************************************************************
     * Debug-related stuff
     ********************************************************************/

    /**
     * Access internal mesh.
     * This method is provided for tests so they won't need any ugly
     * code to access a private member variable.
     */
    /* package-private */ InternalMesh<V> getInternalMesh() {
        return mesh;
    }

}
