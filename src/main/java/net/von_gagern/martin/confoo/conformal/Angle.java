package net.von_gagern.martin.confoo.conformal;

/**
 * Internal angle representation. An angle in this sense is a
 * geometric object, one third of a triangle.<p>
 *
 * Consider a triangle ABC and in that triangle the angle BAC,
 * i.e. the angle located vertex A measuring in positive direction
 * from edge BA to edge AC. These letters will be used to illustrate
 * the components of this class in this documentation.
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
class Angle {

    /**
     * The vertex A. This is the vertex at the center of the angle.
     */
    final Vertex vertex;

    /**
     * The vertex B. This is the vertex following the center of the
     * angle in the cyclic traversal order of the triangle.
     */
    final Vertex nextVertex;

    /**
     * The vertex C. This is the vertex preceding the center of the
     * angle in the cyclic traversal order of the triangle.
     */
    final Vertex prevVertex;

    /**
     * The edge BC. This is the edge opposite to the angle.
     */
    final Edge oppositeEdge;

    /**
     * The edge AB. This is the edge between the center of the angle
     * and the vertex following that center in the cyclic traversal
     * order of the triangle.
     */
    final Edge nextEdge;

    /**
     * The edge CA. This is the edge between the center of the angle
     * and the vertex preceding that center in the cyclic traversal
     * order of the triangle.
     */
    final Edge prevEdge;

    /**
     * The angle, measured in radians.
     */
    double angle;

    /**
     * Construct an angle.
     * The letters below describe the construction the angle at vertex
     * A of a triangle ABC.
     *
     * @param vertex the vertex A
     * @param nextVertex the vertex B
     * @param prevVertex the vertex C
     * @param oppositeEdge the edge BC
     * @param prevEdge the edge CA
     * @param nextEdge the edge AB
     */
    Angle(Vertex vertex, Vertex nextVertex, Vertex prevVertex,
          Edge oppositeEdge, Edge prevEdge, Edge nextEdge) {
        this.vertex = vertex;
        this.nextVertex = nextVertex;
        this.prevVertex = prevVertex;
        this.oppositeEdge = oppositeEdge;
        this.prevEdge = prevEdge;
        this.nextEdge = nextEdge;
        this.angle = Double.NaN;
    }

    /**
     * Update angle value from edge lengths.
     */
    void update() {
        double lo = oppositeEdge.length;
        double ln = nextEdge.length;
        double lp = prevEdge.length;

        // handle violations of triangle inequality
        if (lo >= ln + lp) {
            angle = Math.PI;
            return;
        }
        if (ln >= lo + lp || lp >= lo + ln) {
            angle = 0;
            return;
        }

        // calculate angle using half-angle formula
        double nom = (ln + lo - lp)*(lo + lp - ln);
        double denom = (lp + ln - lo)*(lo + lp + ln);
        if (nom <= denom)
            angle = 2.*Math.atan(Math.sqrt(nom/denom));
        else
            angle = Math.PI - 2.*Math.atan(Math.sqrt(denom/nom));
        assert !Double.isInfinite(angle): "angle is infinite";
        assert !Double.isNaN(angle): "angle is NaN";
    }

}
