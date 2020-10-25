package de.ftk.threemf.mesh;

import java.util.*;

public class Triangle implements Comparable<Triangle>
{
    final Vertex[] vertices;
    final de.ftk.threemf.mesh.Vector normal;
    final int hash;
    final double area;
    final double[] angle;

    public int id;

    public Triangle(Vertex v1, Vertex v2, Vertex v3){
        // triangle id is set by importer and used for debugging
        id = -1;
        //
        vertices = new Vertex[3];
        angle = new double[3];
        vertices[0] = v1;
        vertices[1] = v2;
        vertices[2] = v3;
        de.ftk.threemf.mesh.Vector edge1 = vertices[1].v.sub(vertices[0].v);
        de.ftk.threemf.mesh.Vector edge2 = vertices[2].v.sub(vertices[0].v);
        de.ftk.threemf.mesh.Vector edge3 = vertices[1].v.sub(vertices[2].v);
        de.ftk.threemf.mesh.Vector cross = de.ftk.threemf.mesh.Vector.cross(edge1, edge2);
        area = cross.length();
        normal = cross.normalize();
        angle[0] = de.ftk.threemf.mesh.Vector.getAngle(edge1, edge2);
        angle[1] = de.ftk.threemf.mesh.Vector.getAngle(edge1, edge3);
        angle[2] = de.ftk.threemf.mesh.Vector.getAngle(edge3, edge2.mul(-1));
        hash = Arrays.deepHashCode(vertices);
    }

    public Triangle(Vertex[] vertices) {
        this(vertices[0], vertices[1], vertices[2]);
    }

    @Override
    public String toString() {
        return "Triangle_" + id + "(" + vertices[0].toString() + ", " + vertices[1].toString() + ", " + vertices[2].toString() + ")";
    }


    public double curv()
    {
        return Math.abs((Math.PI*2 - vertices[0].angle)/vertices[0].area) +
        Math.abs((Math.PI*2 - vertices[1].angle)/vertices[1].area) +
        Math.abs((Math.PI*2 - vertices[2].angle)/vertices[2].area);
    }
    
    public Vertex[] getVertices(){
        return vertices;
    }

    public Vector getNormal(){
        return normal;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Triangle other = (Triangle) obj;
        if (!Arrays.deepEquals(this.vertices, other.vertices)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }


    public boolean isDegenerate()
    {
        return ((vertices[0].equals(vertices[1]))||
                (vertices[1].equals(vertices[2]))||
                (vertices[2].equals(vertices[0])));
    }

    public int compareTo(Triangle t){
        if(!vertices[0].equals(t.vertices[0]))
            return vertices[0].compareTo(t.vertices[0]);
        if(!vertices[1].equals(t.vertices[1]))
            return vertices[1].compareTo(t.vertices[1]);
        return vertices[2].compareTo(t.vertices[2]);
    }

    public Matrix errorQuadric()
    {
        double a = normal.x;
        double b = normal.y;
        double c = normal.z;
        double d = -(vertices[0].v.x*a + vertices[0].v.y*b + vertices[0].v.z*c);
        return new Matrix(
                a*a, a*b, a*c, a*d,
                b*a, b*b, b*c, b*d,
                c*a, c*b, c*c, c*d,
                d*a, d*b, d*c, d*d
        );
    }
}
