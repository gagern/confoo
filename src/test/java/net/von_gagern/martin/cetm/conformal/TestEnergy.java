package net.von_gagern.martin.cetm.conformal;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.LowerSPDPackMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.cetm.mesh.LocatedMesh;
import net.von_gagern.martin.cetm.mesh.MeshException;
import net.von_gagern.martin.cetm.mesh.ObjFormat;

public class TestEnergy extends AbstractTestCase {

    private Logger logger = Logger.getLogger(TestEnergy.class);

    private double ATOL = 1/3600.; // one arc second

    @Test public void zeroInitializedVector() {
        Vector u = new DenseVector(2);
        assertEquals(2, u.size());
        assertEquals(0., u.get(0), 0.);
        assertEquals(0., u.get(1), 0.);
    }

    private InternalMesh<Integer> oneRightIsoscelesToEquilateral()
        throws IOException, MeshException
    {
        ObjFormat obj = objResource("oneRightIsosceles.obj");
        InternalMesh<Integer> mesh = new InternalMesh<Integer>(obj);
        for (Vertex v: mesh.getVertices()) v.setTarget(60*DEG);
        mesh.getVertices().get(1).setFixed(true);
        return mesh;
    }

    private int getId(Vertex v, InternalMesh<Integer> mesh) {
        Map<Integer, Vertex> map = mesh.getVertexMap();
        for (int i = 1; i <= map.size(); ++i)
            if (map.get(i) == v)
                return i;
        throw new NoSuchElementException();
    }

    @Test public void testOneRightIsoscelesAngles()
        throws IOException, MeshException
    {
        InternalMesh<Integer> mesh = oneRightIsoscelesToEquilateral();
        Energy e = new Energy(mesh);
        e.setArgument(new DenseVector(2));
        int seenAngle = 0;
        double[] angles = { 90, 45, 45 };
        for (Angle a: mesh.getAngles()) {
            Vertex v = a.vertex();
            int id = getId(v, mesh) - 1;
            seenAngle |= (1 << id);
            if (Math.abs(angles[id] - a.angle()/DEG) > ATOL) {
                logger.debug("Wrong angle " + id);
                logger.debug("nextEdge: " + a.nextEdge().length());
                logger.debug("prevEdge: " + a.prevEdge().length());
                logger.debug("oppositeEdge: " + a.oppositeEdge().length());
            }
            assertEquals("Angle " + id, angles[id], a.angle()/DEG, ATOL);
        }
        assertEquals(7, seenAngle);
    }

    @Test public void testOneRightIsoscelesGradient()
        throws IOException, MeshException
    {
        InternalMesh<Integer> mesh = oneRightIsoscelesToEquilateral();
        Energy e = new Energy(mesh);
        e.setArgument(new DenseVector(2));
        Vector g = e.gradient(null);
        assertEquals(2, g.size());
        assertEquals(60. - 90., g.get(0)/DEG, ATOL);
        assertEquals(60. - 45., g.get(1)/DEG, ATOL);
    }


}
