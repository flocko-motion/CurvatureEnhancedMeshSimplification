package de.ftk.threemf.mesh;

public class Edge implements Comparable<Edge>
{
    Vertex v1, v2;
    boolean isRemoved = false;
    final int hash;

    public Edge(Vertex v, Vertex w)
    {
        if(v.compareTo(w) < 0){
            this.v1 = v;
            this.v2 = w;
        }
        else
        {
            this.v1 = w;
            this.v2 = v;
        }
        hash = v1.hashCode()+10031*v2.hashCode();
    }

    public double getLength()
    {
        return (v1.v.sub(v2.v)).length();
    }

    public int compareTo(Edge e)
    {
        int c = (this.v1).compareTo(e.v1);
        if(c != 0)
            return c;
        else return (this.v2).compareTo(e.v2);
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Edge){
            return (this.compareTo((Edge)o) == 0);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    public String toString()
    {
        return "[ "+v1+" - "+v2+" ]";
    }
}
