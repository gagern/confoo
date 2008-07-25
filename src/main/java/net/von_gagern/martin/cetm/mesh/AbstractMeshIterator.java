package net.von_gagern.martin.cetm.mesh;

public abstract class AbstractMeshIterator<V> implements MeshIterator<V> {

    public void remove() {
        throw new UnsupportedOperationException(
            "Mesh not modifiable through iterator");
    }

}
