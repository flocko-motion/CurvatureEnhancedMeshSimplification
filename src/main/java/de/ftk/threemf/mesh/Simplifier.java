package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Simplifier
{
    private long maxTrianglesPerMesh;
    private double shrinkFactor;
    private double alpha;

    public Simplifier() {
        // default values
        maxTrianglesPerMesh = 1000;
        shrinkFactor = 0.5;
        alpha = 0.5;
    }

    public long getMaxTrianglesPerMesh() {
        return maxTrianglesPerMesh;
    }

    public void setMaxTrianglesPerMesh(long maxTrianglesPerMesh) {
        this.maxTrianglesPerMesh = Math.max(1, maxTrianglesPerMesh);
    }

    public double getShrinkFactor() {
        return shrinkFactor;
    }

    public void setShrinkFactor(double shrinkFactor) {
        this.shrinkFactor = Math.max(0, shrinkFactor);
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = Math.max(0, alpha);
    }

    public Mesh simplify(Mesh m) {
        return Simplifier.simplifyMesh(m, this);
    }

    private static Mesh simplifyMesh(Mesh m, Simplifier config)
    {
        Long maxTriangles = Math.min(config.getMaxTrianglesPerMesh(), Math.round(config.getShrinkFactor() * m.triangles.size()));

        HashSet<Triangle> newMesh = new HashSet<Triangle>();
        HashMap<Edge, EdgePair> edgeMap = new HashMap<Edge, EdgePair>();
        IndexedPriorityQueue<EdgePair> edgeQueue = new IndexedPriorityQueue<EdgePair>();

        // we will need a lot of memory - run garbage collector before optimizing the mesh
        // to have a clean start
        System.gc();
        
        for(Triangle t : m.triangles)
        {
            t.vertices[0].quadric = t.vertices[0].quadric.plus(t.errorQuadric());
            t.vertices[1].quadric = t.vertices[1].quadric.plus(t.errorQuadric());
            t.vertices[2].quadric = t.vertices[2].quadric.plus(t.errorQuadric());
            t.vertices[0].angle += t.angle[0];
            t.vertices[1].angle += t.angle[1];
            t.vertices[2].angle += t.angle[2];
            t.vertices[0].area += t.area;
            t.vertices[1].area += t.area;
            t.vertices[2].area += t.area;
        }

        for(Triangle t : m.triangles)
        {
            newMesh.add(t); //adds temporarily t to the new mesh

            Edge e0 = new Edge(t.vertices[0], t.vertices[1]);
            Edge e1 = new Edge(t.vertices[1], t.vertices[2]);
            Edge e2 = new Edge(t.vertices[2], t.vertices[0]);

            if(!edgeMap.containsKey(e0)){
                EdgePair ep0 = new EdgePair(
                            e0,
                            t.vertices[0].quadric.plus(t.vertices[1].quadric));
                edgeMap.put(e0, ep0);
                edgeQueue.add(ep0);
            }
            if(!edgeMap.containsKey(e1)){
                EdgePair ep1 = new EdgePair(
                            e1,
                            t.vertices[1].quadric.plus(t.vertices[2].quadric));
                edgeMap.put(e1, ep1);
                edgeQueue.add(ep1);
            }
            if(!edgeMap.containsKey(e2)){
                EdgePair ep2 = new EdgePair(
                            e2,
                            t.vertices[0].quadric.plus(t.vertices[2].quadric));
                edgeMap.put(e2, ep2);
                edgeQueue.add(ep2);
            }
            
            t.vertices[0].triangles.add(t);
            t.vertices[1].triangles.add(t);
            t.vertices[2].triangles.add(t);

            t.vertices[0].edges.add(e0);
            t.vertices[0].edges.add(e2);
            t.vertices[1].edges.add(e0);
            t.vertices[1].edges.add(e1);
            t.vertices[2].edges.add(e1);
            t.vertices[2].edges.add(e2);
        }
        
        // start to decimate here
        int best = newMesh.size();
        long time0 = System.currentTimeMillis();
        long triangles0 = newMesh.size();
        logProgress(newMesh.size(), maxTriangles, 0);

        while(newMesh.size() > maxTriangles)
        {
            long size = newMesh.size();
            if(size % 10000 == 0 && best != size && triangles0 != size) {

                long time1 = System.currentTimeMillis();
                long trianglesPerSecond = Math.round(Float.valueOf(size - triangles0)
                        / Float.valueOf(time1 - time0) * 1000);
                time0 = time1;
                triangles0 = size;
                logProgress(size, maxTriangles, trianglesPerSecond);
            }
            boolean valid = true;

            EdgePair p =  edgeQueue.poll();
            if(p.isRemoved)
                continue;

            p.isRemoved = true;
            //add triangles that are connected to the edge p
            HashSet<Triangle> trgToDel = new HashSet<Triangle>();
            for(Triangle t : p.e.v1.triangles)
                trgToDel.add(t);
            for(Triangle t : p.e.v2.triangles)
                trgToDel.add(t);
            //add edges that are connected to the edge p
            HashSet<Edge> edgToDel = new HashSet<Edge>();
            for(Edge e : p.e.v1.edges)
                edgToDel.add(e);
            for(Edge e : p.e.v2.edges)
                edgToDel.add(e);

            HashSet<Triangle> newTrgs = new HashSet<Triangle>();

            Vertex v = p.bestV;
            v.quadric = p.e.v1.quadric.plus(p.e.v2.quadric);

            for(Triangle t : trgToDel){
                Vertex v0 = t.vertices[0];
                Vertex v1 = t.vertices[1];
                Vertex v2 = t.vertices[2];

                if(v0.equals(p.e.v1) || v0.equals(p.e.v2))
                    v0 = v;
                if(v1.equals(p.e.v1) || v1.equals(p.e.v2))
                    v1 = v;
                if(v2.equals(p.e.v1) || v2.equals(p.e.v2))
                    v2 = v;

                Triangle newFace = new Triangle(v0, v1, v2);

                if(newFace.isDegenerate())
                    continue;
                if(t.getNormal().dot(newFace.getNormal()) < 1e-9) //has the normal flipped?
                {
                    valid = false;
                    break;
                }

                newTrgs.add(newFace);
            }

            if(!valid)
                continue;
            

            // removes old trgs
            for(Triangle t : trgToDel){
                newMesh.remove(t);

                t.vertices[0].triangles.remove(t);
                t.vertices[0].angle -= t.angle[0];
                t.vertices[0].area -= t.area;
                t.vertices[1].triangles.remove(t);
                t.vertices[1].angle -= t.angle[1];
                t.vertices[1].area -= t.area;
                t.vertices[2].triangles.remove(t);
                t.vertices[2].angle -= t.angle[2];
                t.vertices[2].area -= t.area;
            }
            // and add new ones
            for(Triangle t : newTrgs){
                newMesh.add(t);

                t.vertices[0].triangles.add(t);
                t.vertices[0].angle += t.angle[0];
                t.vertices[0].area += t.area;
                t.vertices[1].triangles.add(t);
                t.vertices[1].angle += t.angle[1];
                t.vertices[1].area += t.area;
                t.vertices[2].triangles.add(t);
                t.vertices[2].angle += t.angle[2];
                t.vertices[2].area += t.area;
            }

            for(Edge e : edgToDel){
                EdgePair ep = edgeMap.get(e);
                Vertex v1 = e.v1, v2 = e.v2;
                //...and from vertexToEdge
                v1.edges.remove(e);
                v2.edges.remove(e);

                if(v1.equals(p.e.v1) || v1.equals(p.e.v2))
                    v1 = v;
                if(v2.equals(p.e.v1) || v2.equals(p.e.v2))
                    v2 = v;

                ep.isRemoved = true;
                edgeQueue.remove(ep); //logn
                if(!v1.equals(v2)){
                    Edge newE = new Edge(v1, v2);
                    if(!edgeMap.containsKey(newE)){
                        EdgePair newEp = new EdgePair(
                                        newE,
                                        v1.quadric.plus(v2.quadric));
                        edgeMap.put(newE, newEp);
                        //add newE to the queue...
                        edgeQueue.add(newEp);
                        //...and to vertexToEdge
                        v1.edges.add(newE);
                        v2.edges.add(newE);
                    }
                }
            }

        }
        logProgress(newMesh.size(), maxTriangles, 0);
        ArrayList<Triangle> newTrianglesList = new ArrayList<Triangle>();
        for(Triangle t : newMesh)
            newTrianglesList.add(t);
        return new Mesh(newTrianglesList);
    }

    static void logProgress(long currentTriangles, long maxTriangles, long trianglesPerSecond) {
        log.info("Triangles: "
                + String.format("%,8d", currentTriangles)
                + "/" + String.format("%,8d",maxTriangles)
                + " (" + String.format("%,8d",trianglesPerSecond) + "/second)"
                + ", " + SystemWatch.heapStatus());
    }
}
