package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
class Mesh
{
    ArrayList<Triangle> triangles;

    public Mesh(ArrayList<Triangle> t)
    {
        triangles = t;
    }

    public Mesh() {
        triangles = new ArrayList<>();
    }

    public Triangle addTriangle(Triangle triangle) {
        triangles.add(triangle);
        return triangle;
    }

    public UniqueVertices getUniqueVertices() {
        UniqueVertices uniqueVertices = new UniqueVertices();
        triangles.stream()
            .map(Triangle::getVertices)
            .flatMap(Arrays::stream)
            .map(uniqueVertices::putVertex)
            .count();
        return uniqueVertices;
    }
}