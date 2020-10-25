package de.ftk.threemf.mesh;


import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;

public abstract class File3D {

    final ArrayList<Mesh> meshes;
    final ArrayList<Node> meshNodes;

    public File3D() {
        meshes = new ArrayList<>();
        meshNodes = new ArrayList<>();
    }

    abstract public void toFile(File file);
}
