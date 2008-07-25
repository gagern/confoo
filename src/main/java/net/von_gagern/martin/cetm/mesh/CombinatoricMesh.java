package net.von_gagern.martin.cetm.mesh;

import java.util.Iterator;

public interface CombinatoricMesh<V> {

    public Iterator<? extends CorneredTriangle<? extends V>> iterator();

}
