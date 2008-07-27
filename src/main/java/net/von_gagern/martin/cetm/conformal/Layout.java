package net.von_gagern.martin.cetm.conformal;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

class Layout implements Runnable {

    private final Logger logger = Logger.getLogger(Layout.class);

    private final List<Triangle> triangles;

    public Layout(InternalMesh mesh) {
        triangles = mesh.getTriangles();
    }

    public void layout() {
        // Sadly ArrayDeque was added only in java 1.6, so we won't use it yet
        Queue<Triangle> q = new LinkedList<Triangle>();
        clearIterFlags();
        Triangle start = findStart();
        logger.debug("Start triangle: " + start);
        layoutStart(start);
        q.add(start);
        clearIterFlags();
        while (!q.isEmpty()) {
            Triangle t1 = q.remove();
            t1.setIterFlag();
            for (Edge e: t1.getEdges()) {
                Triangle t2 = e.getOtherTriangle(t1);
                if (t2 == null || t2.getIterFlag()) continue;
                q.add(t2);
                t2.setIterFlag();
                layoutEdge(e, t2);
            }
        }
    }

    private void clearIterFlags() {
        for (Triangle t: triangles)
            t.clearIterFlag();
    }

    private Triangle findStart() {
        // Sadly ArrayDeque was added only in java 1.6, so we won't use it yet
        Queue<Triangle> q = new LinkedList<Triangle>();
        int unqueued = 0;
        for (Triangle t: triangles) {
            if (t.isBoundary()) {
                q.add(t);
            }
            else {
                ++unqueued;
            }
        }
        if (q.isEmpty() || unqueued == 0) {
            return triangles.get(triangles.size()/2);
        }
        for (;;) {
            Triangle t1 = q.remove();
            t1.setIterFlag();
            for (Edge e: t1.getEdges()) {
                Triangle t2 = e.getOtherTriangle(t1);
                if (t2 == null || t2.getIterFlag()) continue;
                q.add(t2);
                t2.setIterFlag();
                --unqueued;
                if (unqueued == 0)
                    return t2;
            }
        }
    }

    private void layoutStart(Triangle t) {
        Angle a = t.getAngles().get(0);
        Vertex v1 = a.vertex(), v2 = a.nextVertex(), v3 = a.prevVertex();
        Edge e12 = a.nextEdge(), e13 = a.prevEdge(), e23 = a.oppositeEdge();
        double l12 = e12.length(), l13 = e13.length(), l23 = e23.length();
        double alpha = a.angle();
        Angle b = t.getNextAngle(v1);
        double beta = b.angle();

        // set locations
        v1.setLocation(0, 0);
        v2.setLocation(l12, 0);
        v3.setLocation(l13*Math.cos(alpha), l13*Math.sin(alpha));
        logger.trace("Vertex " + v1.getRep() + " at location (0,0)");
        logger.trace("Vertex " + v2.getRep() + " at location (" +
                     v2.getX() + ",0)");
        logger.trace("Vertex " + v3.getRep() + " at location (" +
                     v3.getX() + "," + v3.getY() + ")");

        // set angles
        if (e12.getV1() == v1) e12.setAngle(0);
        else e12.setAngle(Math.PI);
        if (e13.getV1() == v1) e13.setAngle(alpha);
        else e13.setAngle(alpha - Math.PI);
        if (e23.getV1() == v2) e23.setAngle(Math.PI - beta);
        else e23.setAngle(-beta);
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
    private void layoutEdge(Edge e, Triangle t) {
        /* Imagine t is an oriented triangle ABC. We entered the triangle
         * through the unoriented edge [AB], so e is either [AB] or [BA].
         * We want to find the coordinates for C, based on those of A.
         * Thus we determine the Angle BAC and the edge [AC].
         * Each edge has an associated orientation, but we have to take
         * care or the orientation of these edges. If both edges point
         * towards A or both away from A, we can simply add angles.
         * Otherwise we have to add PI in order to invert orientation.
         */

        Vertex c = t.getOppositeVertex(e);
        Angle bac = t.getNextAngle(c), cba = t.getPrevAngle(c);
        Vertex a = bac.vertex(), b = cba.vertex();
        Edge ca = bac.prevEdge(), bc = cba.nextEdge();
        double alpha = bac.angle(), beta = cba.angle();
        double abAngle = e.getAngle();

        double caAngle = edgeAngle(abAngle + alpha, e, ca, a);
        double bcAngle = edgeAngle(abAngle - beta, e, bc, b);
        ca.offerAngle(caAngle);
        bc.offerAngle(bcAngle);

        double caLen = ca.length(), bcLen = bc.length();
        if (ca.getV1() != a) caLen = -caLen;
        if (bc.getV1() != b) bcLen = -bcLen;
        double caX = a.getX() + caLen*Math.cos(caAngle);
        double caY = a.getY() + caLen*Math.sin(caAngle);
        double bcX = b.getX() + bcLen*Math.cos(bcAngle);
        double bcY = b.getY() + bcLen*Math.sin(bcAngle);
        double x = (caX + bcX)/2, y = (caY + bcY)/2;
        c.offerLocation(x, y);
        logger.trace("layoutEdge(" + e + ", " + t + ") set " + c +
                     " to (" + x + ", " + y + ")");
    }

    private double edgeAngle(double angle, Edge e1, Edge e2, Vertex v) {
        if ((e1.getV1() == v) != (e2.getV1() == v)) {
            if (angle > 0) angle -= Math.PI;
            else angle += Math.PI;
        }
        while (angle > Math.PI) angle -= 2*Math.PI;
        while (angle <= -Math.PI) angle += 2*Math.PI;
        return angle;
    }

    public void run() {
        layout();
    }

}
