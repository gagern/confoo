package net.von_gagern.martin.cetm.mesh;

public class SimpleTriangle<C> implements CorneredTriangle<C> {

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

}
