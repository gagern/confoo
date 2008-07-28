package net.von_gagern.martin.cetm.mesh;

import java.util.AbstractList;

public class SimpleTriangle<C>
    extends AbstractList<C> implements CorneredTriangle<C> {

    private C c1, c2, c3;

    public SimpleTriangle(C c1, C c2, C c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public C getCorner(int index) {
        switch (index) {
        case 0: return c1;
        case 1: return c2;
        case 2: return c3;
        default: throw new IndexOutOfBoundsException();
        }
    }

    public C get(int index) {
        return getCorner(index);
    }

    public int size() {
        return 3;
    }

    @Override public int hashCode() {
        return c1.hashCode() ^ c2.hashCode() ^ c3.hashCode();
    }

    @Override public boolean equals(Object o) {
        return o instanceof SimpleTriangle && equal(this, (SimpleTriangle)o);
    }

    public static boolean
    equal(CorneredTriangle<?> t1, CorneredTriangle<?> t2) {
        ROTATIONS: for (int rotation = 0; rotation < 3; ++rotation) {
            for (int corner = 0; corner < 3; ++corner) {
                if (!t1.getCorner((corner + rotation)%3)
                    .equals(t2.getCorner(corner))) {
                    continue ROTATIONS;
                }
            }
            return true; // rotation matches completely
        }
        return false; // no rotation matches
    }

}
