package net.von_gagern.martin.confoo.conformal;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

import net.von_gagern.martin.confoo.mesh.MeshException;
import net.von_gagern.martin.confoo.mesh.ObjFormat;

public class TestInternalMesh extends AbstractTestCase {

    @Test public void testCardinality() throws IOException, MeshException {
        ObjFormat obj = objResource("test1.obj");
        InternalMesh<Integer> mesh = new InternalMesh<Integer>(obj);
        assertEquals("Vertex count", 9, mesh.getVertices().size());
        assertEquals("Face count", 8, mesh.getTriangles().size());
        assertEquals("Edge count", 16, mesh.getEdges().size());
    }

    @Test public void testEdgeBoundary() throws IOException, MeshException {
        ObjFormat obj = objResource("test1.obj");
        InternalMesh<Integer> mesh = new InternalMesh<Integer>(obj);
        int countBoundary = 0, countNonBoundary = 0;
        for (Edge e: mesh.getEdges()) {
            if (e.isBoundary()) countBoundary++;
            else countNonBoundary++;
        }
        assertEquals("Boundary edges ", 8, countBoundary);
        assertEquals("Non-boundary edges ", 8, countNonBoundary);
    }

    @Test public void testKind() throws IOException, MeshException {
        ObjFormat obj = objResource("test1.obj");
        InternalMesh<Integer> mesh = new InternalMesh<Integer>(obj);
        Map<Integer, Vertex> vm = mesh.getVertexMap();
        for (int i = 1; i <= 9; ++i) {
            Vertex.Kind kind = vm.get(i).kind;
            switch (i) {
            case 1:
            case 3:
                assertEquals("Vertex " + i, Vertex.Kind.CORNER, kind);
                break;
            case 9:
                assertEquals("Vertex " + i, Vertex.Kind.INTERIOR, kind);
                break;
            default:
                assertEquals("Vertex " + i, Vertex.Kind.BOUNDARY, kind);
            }
        }
    }

}
