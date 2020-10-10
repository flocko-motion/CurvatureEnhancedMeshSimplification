package de.ftk.threemf.mesh;

import java.util.*;

class Vertex implements Comparable<Vertex>
{
    Vector v;
    HashSet<Triangle> triangles;
    HashSet<Edge> edges;
    Matrix quadric;
    double angle = 0, area = 0, curv = -1;

    public Vertex(Vector _v)
    {
        v = _v;
        triangles = new HashSet<>();
        edges = new HashSet<>();
        quadric = new Matrix();
    }

    public int hashCode()
    {
        return v.hashCode();
    }

    public int compareTo(Vertex v)
    {
        return this.v.compareTo(v.v);
    }

    public boolean equals(Object o)
    {
        if(o instanceof Vertex)
            return this.v.equals(((Vertex)o).v);
        return false;
    }

    static double maxCurv = 0;
}
