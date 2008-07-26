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

public class ObjFormat
    implements LocatedMesh<Integer>, Iterable<CorneredTriangle<Integer>>
{

    private static final String whitespaceStr = "[ \r\n\t]+";

    private static final String faceVertexStr = "(\\d+)(/\\d*){0,2}";

    private static Pattern whitespacePattern;

    private static Pattern faceVertexPattern;

    private static synchronized void init() {
        if (whitespacePattern == null)
            whitespacePattern = Pattern.compile(whitespaceStr);
        if (faceVertexPattern == null)
            faceVertexPattern = Pattern.compile(faceVertexStr);
    }

    private List<double[]> vs;

    private List<int[]> fs;

    private ObjFormat() {
        init();
        vs = new ArrayList<double[]>();
        fs = new ArrayList<int[]>();
    }

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

    public ObjFormat(Reader in) throws IOException {
        this(new BufferedReader(in));
    }

    public ObjFormat(InputStream in) throws IOException {
        this(new InputStreamReader(in, "ISO-8859-1"));
    }

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

    public void write(Writer out) throws IOException {
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

    public void write(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, "US-ASCII");
        writer = new BufferedWriter(writer);
        write(writer);
        writer.flush();
    }

    public double edgeLength(Integer v1, Integer v2) {
        double[] c1 = vs.get(v1 - 1), c2 = vs.get(v2 - 1);
        double res = 0;
        for (int i = 0; i < 3; ++i) {
            double d = c1[i] - c2[i];
            res += d*d;
        }
        return Math.sqrt(res);
    }

    public double getX(Integer v) {
        return vs.get(v - 1)[0];
    }

    public double getY(Integer v) {
        return vs.get(v - 1)[1];
    }

    public double getZ(Integer v) {
        return vs.get(v - 1)[2];
    }

    public MeshIterator<Integer> iterator() {
        return new Iter();
    }

    private class Iter extends AbstractMeshIterator<Integer> {

        private Iterator<int[]> i = fs.iterator();

        public boolean hasNext() {
            return i.hasNext();
        }

        public CorneredTriangle<Integer> next() {
            int[] cs = i.next();
            return new SimpleTriangle(cs[0], cs[1], cs[2]);
        }

    }

}
