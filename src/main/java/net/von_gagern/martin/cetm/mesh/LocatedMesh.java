package net.von_gagern.martin.cetm.mesh;

public interface LocatedMesh<V> extends MetricMesh<V> {

    public double getX(V v);

    public double getY(V v);

    public double getZ(V v);

}
