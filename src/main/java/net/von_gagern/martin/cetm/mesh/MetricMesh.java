package net.von_gagern.martin.cetm.mesh;

public interface MetricMesh<V> extends CombinatoricMesh<V> {

    public double edgeLength(V v1, V v2);

}
