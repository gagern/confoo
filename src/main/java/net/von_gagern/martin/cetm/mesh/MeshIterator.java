package net.von_gagern.martin.cetm.mesh;

import java.util.Iterator;

/**
 * Convenience interface for an iterator over mesh triangles.<p>
 *
 * Although the mesh interfaces are defined in terms of generic
 * iterators over cornered triangles, using this interface can help to
 * keep code shorter and easier to read. Deverlopers implementing
 * their own iterators over the triangles of a mesh are encouraged to
 * implement this interface.<p>
 *
 * @param <V> the class used to represent triangle vertices
 * @see CombinatoricMesh#iterator
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
public interface MeshIterator<V> extends Iterator<CorneredTriangle<V>> {

}
