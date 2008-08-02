/**
 * Interfaces and classes to deal with triangular meshes.<p>
 *
 * The interfaces in this package are intended as a minimalistic and
 * easy to implement set of methods for dealing with triangle
 * meshes. The class used for vertices is given as a type parameter,
 * so it should be easy to adapt any mesh dat astructure to this
 * interface. Most meshes will probably implement the
 * {@link net.von_gagern.martin.cetm.mesh.LocatedMesh LocatedMesh}
 * interface.<p>
 *
 * The 2D classes provide an implementation for meshes in two
 * dimensions. While classes like
 * {@link net.von_gagern.martin.cetm.mesh.Triangle2D Triangle2D}
 * represent new geometric objects,
 * {@link net.von_gagern.martin.cetm.mesh.Edge2D Edge2D} and
 * {@link net.von_gagern.martin.cetm.mesh.Vertex2D Vertex2D} only add
 * equality comparisons to existing line and point implementations.<p>
 *
 * @author <a href="mailto:Martin.vGagern@gmx.net">Martin von Gagern</a>
 * @since 1.0
 */
package net.von_gagern.martin.cetm.mesh;
