package de.ftk.threemf.mesh;


//==================================================================================

import java.util.ArrayList;
import java.util.HashSet;
// import java.util.Vector;


class EdgePair implements Comparable<EdgePair>
{
    public EdgePair(Edge e, Matrix quadric)
    {
        this.length = e.getLength();
        this.e = e;
        Vector bv = quadric.getBestVector();
        if(bv == null)
            bestV = new Vertex(Vector.mean(e.v1.v, e.v2.v));
        else
            bestV = new Vertex(bv);

        curv = computeCurvature();
        error = quadric.quadraticForm(bestV.v)*(1 - Math.pow(2, -curv*EdgePair.alpha));
    }

    public int compareTo(EdgePair ep)
    {
        if(Math.abs(this.error - ep.error) > 10e-15)
            return (this.error - ep.error)>0 ? 1 : -1;
        return e.hashCode() - ep.hashCode(); //random
    }

    public boolean equals(Object o)
    {
        if(o instanceof EdgePair)
            return (this.compareTo((EdgePair)o) == 0);
        return false;
    }

    public String toString()
    {
        return length+" "+e;
    }

    private double computeCurvatureTr()
    {
        ArrayList<Triangle> intr = new ArrayList<>();
        HashSet<Triangle> union = new HashSet<>();
        for(Triangle t : e.v1.triangles){
            union.add(t);
            if(e.v2.triangles.contains(t))
                intr.add(t);
        }
        for(Triangle t : e.v2.triangles)
            union.add(t);

        double curv = 0;
        for(Triangle f : union)
            for(Triangle n : intr)
                curv = Math.max(curv, f.normal.dot(n.normal));
        return curv*e.getLength();
    }

    private double computeCurvature()
    {
        double curv1 = Math.abs((Math.PI*2 - e.v1.angle)/e.v1.area);
        double curv2 = Math.abs((Math.PI*2 - e.v2.angle)/e.v2.area);
        return (curv1+curv2)/2;
    }

    double length;
    double error;
    double curv, curv2;
    Edge e;
    Vertex bestV;
    boolean isRemoved = false;
    static double alpha = 0;
}
