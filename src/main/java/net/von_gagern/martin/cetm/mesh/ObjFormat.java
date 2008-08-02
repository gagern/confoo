package net.von_gagern.martin.cetm.mesh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bare bones interoperability with obj file format.<p>
 *
 * This class facililiates interaction with the Object File Format
 * introduced by Wavefront Technologies for the description of 3D
 * objects, including meshes.<p>
 *
 * The implementation provides both read and write access to said
 * format, but only considers meshes at the moment. Other lines will
 * be disregarded on input and not generated on output.
 *
 * @see <a href="http://local.wasp.uwa.edu.au/~pbourke/dataformats/obj/">Obj Specification</a>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public class ObjFormat
    implements LocatedMesh<Integer>, Iterable<CorneredTriangle<Integer>>
{

    /**
     * Regular expression representing whitespace.
     */
    private static final String whitespaceStr = "[ \r\n\t]+";

    /**
     * Regular expression of a single vertex description of a face line.
     */
    private static final String faceVertexStr = "(\\d+)(/\\d*){0,2}";

    /**
     * Compiled whitespace regular expression pattern.
     */
    private static Pattern whitespacePattern;

    /**
     * Compiled face vertex regular expression pattern.
     */
    private static Pattern faceVertexPattern;

    /**
     * Perform class initialization. Called by every constructor.
     */
    private static synchronized void init() {
        if (whitespacePattern == null)
            whitespacePattern = Pattern.compile(whitespaceStr);
        if (faceVertexPattern == null)
            faceVertexPattern = Pattern.compile(faceVertexStr);
    }

    /**
     * List of vertices. Each element is an array of three coordinates.
     */
    private List<double[]> vs;

    /**
     * List of faces. Each element is an array of three vertex indices.
     */
    private List<int[]> fs;

    /**
     * Basic internal constructor. Called by the other constructors.
     */
    private ObjFormat() {
        init();
        vs = new ArrayList<double[]>();
        fs = new ArrayList<int[]>();
    }

    /**
     * Construct from object file text.
     * @param in a reader over some object file
     */
    public ObjFormat(BufferedReader in) throws IOException {
        this();
        for (String line = in.readLine(); line != null;
             line = in.readLine()) {
            if (line.startsWith("v "))
                readVertex(line);
            else if (line.startsWith("f "))
                readFace(line);
        }
    }

    /**
     * Construct from object file text.
     * @param in a reader over some object file
     */
    public ObjFormat(Reader in) throws IOException {
        this(new BufferedReader(in));
    }

    /**
     * Construct from object file input stream.
     * Input will be decoded using latin1 encoding, as this will avoid
     * encoding related errors and texts elements will be disregarded
     * in any case.
     * @param in an input stream over some object file
     */
    public ObjFormat(InputStream in) throws IOException {
        this(new InputStreamReader(in, "ISO-8859-1"));
    }

    /**
     * Construct from arbitrary located mesh.<p>
     *
     * A map can be provided to map vertices to indices. It must
     * assign indices continuously, starting from 1. Vertices not
     * present in the map will be added to the map and cann therefore
     * be obtained from the map after this function returns. If the
     * caller is not interested in this information, he may pass
     * <code>null</code> as the <code>vertexMap</code>.
     *
     * @param mesh the mesh to construct the object file from
     * @param vertexMap the vertex map as described above or <code>null</code>
     */
    public <V> ObjFormat(LocatedMesh<V> mesh, Map<V, Integer> vertexMap) {
        this();
        if (vertexMap == null)
            vertexMap = new HashMap<V, Integer>();
        Iterator<? extends CorneredTriangle<? extends V>> iter;
        iter = mesh.iterator();
        while (iter.hasNext()) {
            CorneredTriangle<? extends V> t = iter.next();
            int[] cs = new int[3];
            for (int i = 0; i < 3; ++i) {
                V c = t.getCorner(i);
                Integer ci = vertexMap.get(c);
                if (ci == null) {
                    ci = vertexMap.size() + 1;
                    vertexMap.put(c, ci);
                }
                cs[i] = ci;
            }
            fs.add(cs);
        }
        vs.addAll(Collections.nCopies(vertexMap.size(), (double[])null));
        for (Map.Entry<V, Integer> entry: vertexMap.entrySet()) {
            V v = entry.getKey();
            double x = mesh.getX(v);
            double y = mesh.getY(v);
            double z = mesh.getZ(v);
            vs.set(entry.getValue() - 1, new double[] {x, y, z});
        }
    }

    /**
     * Construct from integer mesh.<p>
     *
     * The integers identifying vertices in the undrlying mesh must
     * start at 1 and form a continuous range.
     *
     * @param mesh the mesh to construct the object file from
     */
    public ObjFormat(LocatedMesh<Integer> mesh) {
        this();
        Iterator<? extends CorneredTriangle<? extends Integer>> iter;
        iter = mesh.iterator();
        int maxVertex = 0;
        while (iter.hasNext()) {
            CorneredTriangle<? extends Integer> t = iter.next();
            int[] cs = new int[3];
            for (int i = 0; i < 3; ++i) {
                int c = t.getCorner(i);
                cs[i] = c;
                if (maxVertex < c)
                    maxVertex = c;
            }
            fs.add(cs);
        }
        for (int v = 1; v <= maxVertex; ++v) {
            double x = mesh.getX(v);
            double y = mesh.getY(v);
            double z = mesh.getZ(v);
            vs.add(new double[] {x, y, z});
        }
    }

    /**
     * Helper method to read a single vertex line from an obj file.
     * @param str the input line
     */
    private void readVertex(String str) throws IOException {
        String[] parts = whitespacePattern.split(str);
        if (parts.length != 4)
            throw new IOException("Invalid vertex line:\n" + str);
        double[] coords = new double[3];
        for (int i = 0; i < 3; ++i) {
            coords[i] = Double.parseDouble(parts[i + 1]);
        }
        vs.add(coords);
    }

    /**
     * Helper method to read a single face line from an obj file.
     * @param str the input line
     */
    private void readFace(String str) throws IOException {
        String[] parts = whitespacePattern.split(str);
        if (parts.length != 4)
            throw new IOException("Invalid triangle line:\n" + str);
        int[] corners = new int[3];
        for (int i = 0; i < 3; ++i) {
            Matcher m = faceVertexPattern.matcher(parts[i + 1]);
            if (!m.matches())
                throw new IOException("Invalid triangle line:\n" + str);
            corners[i] = Integer.parseInt(m.group(1));
        }
        fs.add(corners);
    }

    /**
     * Write out object file text.
     * @param out the appendable to which the object file will be written
     * @throws IOException if an I/O error occurs
     */
    public void write(Appendable out) throws IOException {
        for (double[] v: vs) {
            out.append('v');
            for (int i = 0; i < 3; ++i)
                out.append(' ').append(Double.toString(v[i]));
            out.append('\n');
        }
        for (int[] f: fs) {
            out.append('f');
            for (int i = 0; i < 3; ++i)
                out.append(' ').append(Integer.toString(f[i]));
            out.append('\n');
        }
    }

    /**
     * Write out object file stream.
     * @param out the strem to which the object file will be written
     * @throws IOException if an I/O error occurs
     */
    public void write(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, "US-ASCII");
        writer = new BufferedWriter(writer);
        write(writer);
        writer.flush();
    }

    /**
     * Determine edge length.
     * @param v1 index of first vertex
     * @param v2 index of second vertex
     * @return the distance as calculated from the vertex coordinates
     */
    public double edgeLength(Integer v1, Integer v2) {
        double[] c1 = vs.get(v1 - 1), c2 = vs.get(v2 - 1);
        double res = 0;
        for (int i = 0; i < 3; ++i) {
            double d = c1[i] - c2[i];
            res += d*d;
        }
        return Math.sqrt(res);
    }

    /**
     * Get x coordinate of vertex.
     * @param v index of a vertex
     * @return the x coordinate of that vertex
     */
    public double getX(Integer v) {
        return vs.get(v - 1)[0];
    }

    /**
     * Get y coordinate of vertex.
     * @param v index of a vertex
     * @return the y coordinate of that vertex
     *
     */
    public double getY(Integer v) {
        return vs.get(v - 1)[1];
    }

    /**
     * Get z coordinate of vertex.
     * @param v index of a vertex
     * @return the z coordinate of that vertex
     */
    public double getZ(Integer v) {
        return vs.get(v - 1)[2];
    }

    /**
     * Get iterator over all triangles.
     * @return an iterator over all triangles in the mesh
     */
    public MeshIterator<Integer> iterator() {
        return new Iter();
    }

    /**
     * Implementation of the mesh iterator for object files.
     *
     * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
     */
    private class Iter extends AbstractMeshIterator<Integer> {

        /**
         * Iterator over the faces.
         */
        private Iterator<int[]> i = fs.iterator();

        /**
         * Determine whether there are any more faces.
         * @return whether there are any more faces to iterate
         */
        public boolean hasNext() {
            return i.hasNext();
        }

        /**
         * Get next triangle.
         * @return a triangle representing the next face
         */
        public CorneredTriangle<Integer> next() {
            int[] cs = i.next();
            return new SimpleTriangle(cs[0], cs[1], cs[2]);
        }

    }

}
