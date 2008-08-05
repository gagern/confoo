package net.von_gagern.martin.confoo.conformal;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.ObjFormat;

public class TestEnergy extends AbstractTestCase {

    private final Logger logger = Logger.getLogger(TestEnergy.class);

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
        for (Vertex v: mesh.getVertices()) v.target = 60*DEG;
        mesh.getVertexMap().get(2).fixed = true;
        return mesh;
    }

    private int getId(Vertex v, InternalMesh<Integer> mesh) {
        return (Integer)v.rep;
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
            Vertex v = a.vertex;
            int id = getId(v, mesh) - 1;
            seenAngle |= (1 << id);
            if (Math.abs(angles[id] - a.angle/DEG) > angleTolerance) {
                logger.debug("Wrong angle " + id);
                logger.debug("nextEdge: " + a.nextEdge.length);
                logger.debug("prevEdge: " + a.prevEdge.length);
                logger.debug("oppositeEdge: " + a.oppositeEdge.length);
            }
            assertEquals("Angle " + id, angles[id], a.angle/DEG,
                         angleTolerance);
        }
        assertEquals(7, seenAngle);
    }

    @Test public void testOneRightIsoscelesGradient()
        throws IOException, MeshException
    {
        InternalMesh<Integer> mesh = oneRightIsoscelesToEquilateral();
        Map<Integer, Vertex> vm = mesh.getVertexMap();
        Energy e = new Energy(mesh);
        e.setArgument(new DenseVector(2));
        Vector g = e.gradient(null);
        assertEquals(2, g.size());
        assertEquals(60. - 90., g.get(vm.get(1).index)/DEG,
                     angleTolerance);
        assertEquals(60. - 45., g.get(vm.get(3).index)/DEG,
                     angleTolerance);
    }


}
