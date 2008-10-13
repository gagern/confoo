package net.von_gagern.martin.confoo.conformal;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;

/**
 * Calculate hyperbolic vertex coordinates from edge lengths.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.1
 */
class HypLayout extends Layout {

    /**
     * Log4j logger for customizable logging and reporting.
     */
    private final Logger logger = Logger.getLogger(HypLayout.class);

    /**
     * Construct layouter for given hyperbolic mesh.
     * @param mesh the mesh to be layed out.
     * @see Layout#layout()
     */
    public HypLayout(InternalMesh mesh) {
        super(mesh);
    }

    /**
     * Layout the initial triangle.
     * @param t the first triangle to be layed out
     */
    protected void layoutStart(Triangle t) {
        Angle a = t.getAngles().get(0);
        Vertex v1 = a.vertex, v2 = a.nextVertex, v3 = a.prevVertex;
        Edge e12 = a.nextEdge;
        double l12 = e12.length;

        // set locations and edge positions
        HypEdgePos pos1 = new HypEdgePos();
        pos1.setVertex(v1);
        v1.offerLocation(0, 0);
        e12.offerHypPos(pos1);
        Point2D p2 = pos1.derive(v2, l12).dehomogenize();
        v2.offerLocation(p2.getX(), p2.getY());
        layoutEdge(e12, t);
    }

    /**
     * Lay out third vertex in triangle.<p>
     *
     * This method is called when triangle was entered in order to fix
     * the location of the vertex opposite the entering angle.
     *
     * @param e the edge by which the triangle was entered
     * @param t the triangle just entered
     */
    protected void layoutEdge(Edge e, Triangle t) {
        /* Imagine t is an oriented triangle ABC. We entered the triangle
         * through the unoriented edge [AB], so e is either [AB] or [BA].
         * We want to find the coordinates for C, based on those of A.
         * Thus we determine the Angle BAC and the edge [AC].
         * Each edge has an associated orientation, but we have to take
         * care of the orientation of these edges. If both edges point
         * towards A or both away from A, we can simply add angles.
         * Otherwise we have to add PI in order to invert orientation.
         */

        Vertex c = t.getOppositeVertex(e);
        Angle bac = t.getNextAngle(c), cba = t.getPrevAngle(c);
        Vertex a = bac.vertex, b = cba.vertex;
        Edge ca = bac.prevEdge, bc = cba.nextEdge;
        double alpha = bac.angle, beta = cba.angle;

        HypEdgePos abPos = e.getHypPos();
        HypEdgePos caPos = abPos.derive(a, e.length, alpha);
        HypEdgePos bcPos = abPos.derive(b, e.length, -beta);
        logger.trace("ab = " + abPos);
        logger.trace("ca = " + caPos);
        logger.trace("bc = " + bcPos);
        caPos = ca.offerHypPos(caPos);
        bcPos = bc.offerHypPos(bcPos);
        logger.trace("ca = " + caPos);
        logger.trace("bc = " + bcPos);

        Point2D caC = caPos.derive(c, ca.length).dehomogenize();
        Point2D bcC = bcPos.derive(c, bc.length).dehomogenize();
        double x = (caC.getX() + bcC.getX())/2;
        double y = (caC.getY() + bcC.getY())/2;
        assert !Double.isNaN(x): "x must not be NaN";
        assert !Double.isNaN(y): "y must not be NaN";
        assert !Double.isInfinite(x): "x must be finite";
        assert !Double.isInfinite(y): "y must be finite";
        c.offerLocation(x, y);
        logger.trace("layoutEdge(" + e + ", " + t + ") set " + c +
                     " to (" + x + ", " + y + ")");
    }

}
