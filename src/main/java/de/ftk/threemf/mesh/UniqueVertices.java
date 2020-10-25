package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility class to construct a list of unique vertices out of a list of vertices containing duplicates.
 */
@Slf4j
public class UniqueVertices {

    // map of unique vertices and their position in the vertices list
    final private HashMap<Vector, Pair<Vertex, Integer>> hashMap;

    // list of unique vertices
    final private ArrayList<Vertex> vertices;

    // List of all added vertices in the order they were added
    final private ArrayList<Vertex> verticesInInputOrder;

    public UniqueVertices() {
        hashMap = new HashMap<>();
        vertices = new ArrayList<>();
        verticesInInputOrder = new ArrayList<>();
    }

    /**
     * Hand over a potentially duplicate vertex to this method and get a vertex with identical coordinates out
     * of the pool of unique vertices.
     *
     * @param vertex
     * @return
     */
    public Vertex putVertex(Vertex vertex) {
        // store vertex unique
        Vector vector = vertex.getVector();
        if (!hashMap.containsKey(vector)) {
            hashMap.put(vector, new Pair<>(vertex, hashMap.size()));
            vertices.add(vertex);
        } else {
            vertex = hashMap.get(vector).getValue0();
        }
        this.verticesInInputOrder.add(vertex);
        //
        return vertex;
    }

    public Vertex putVertex(Vector vector) {
        return putVertex(new Vertex(vector));
    }

    public Vertex getVertex(Vector vector) {
        return hashMap.get(vector).getValue0();
    }

    public Vertex getVertex(Integer index) {
        return verticesInInputOrder.get(index);
    }

    public int getVertexId(Vertex vertex) {
        return (int) hashMap.get(vertex.getVector()).getValue(1);
    }

    public int size() {
        return vertices.size();
    }

    public Stream<Vertex> stream() {
        return vertices.stream();
    }

}
