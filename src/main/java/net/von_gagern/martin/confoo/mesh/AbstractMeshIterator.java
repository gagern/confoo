package net.von_gagern.martin.confoo.mesh;

/**
 * Abstract base class for mesh iterators.<p>
 *
 * This class throws an <code>UnsupportedOperationException</code>
 * when an application tries to modify the mesh through the
 * <code>remove()</code> method. It is thus a useful base class for
 * read-only access to meshes. Implementors yet have to implement
 * {@link java.util.Iterator#hasNext() hasNext()} and
 * {@link java.util.Iterator#next() next()}.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public abstract class AbstractMeshIterator<V> implements MeshIterator<V> {

    /**
     * Throws <code>UnsupportedOperationException</code>
     *
     * @throws UnsupportedOperationException unless overridden
     */
    public void remove() {
        throw new UnsupportedOperationException(
            "Mesh not modifiable through iterator");
    }

}
