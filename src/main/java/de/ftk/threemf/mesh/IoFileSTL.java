package de.ftk.threemf.mesh;

import java.io.*;
import java.nio.file.*;
import java.nio.*;
import java.util.*;

class IoFileSTL {
    /**
     * Read mesh from file
     *
     * @param path
     * @return mesh
     * @throws IOException
     */
    public static Mesh readMesh(String path) throws IOException {
        byte[] allBytes = Files.readAllBytes(Paths.get(path));
        // bytes -> triangles -> mesh
        return new Mesh(readBinary(allBytes));
    }

    public static ArrayList<Triangle> readBinary(byte[] allBytes) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(allBytes));
        ArrayList<Triangle> triangles = new ArrayList<>();
        // collection of unique vertices
        HashMap<de.ftk.threemf.mesh.Vector, Vertex> verticesUnique = new HashMap<de.ftk.threemf.mesh.Vector, Vertex>();
        try {
            // skip the header
            byte[] header = new byte[80];
            in.read(header);
            // get number triangles (not really needed)
            // WARNING: STL FILES ARE SMALL-ENDIAN
            int numberTriangles = Integer.reverseBytes(in.readInt());
            triangles.ensureCapacity(numberTriangles);
            // parse data
            try {
                while (in.available() > 0) {
                    // read next normal vector (unused)
                    float[] xyz = new float[3];
                    for (int i = 0; i < xyz.length; i++) {
                        xyz[i] = Float.intBitsToFloat(Integer.reverseBytes(in.readInt()));
                    }
                    de.ftk.threemf.mesh.Vector normal = new de.ftk.threemf.mesh.Vector(xyz[0], xyz[1], xyz[2]);

                    // read next three vertices
                    Vertex[] verticesTriangle = new Vertex[3];
                    for (int v = 0; v < verticesTriangle.length; v++) {
                        for (int d = 0; d < xyz.length; d++) {
                            xyz[d] = Float.intBitsToFloat(Integer.reverseBytes(in.readInt()));
                        }
                        de.ftk.threemf.mesh.Vector vertexCoordinates = new Vector(xyz[0], xyz[1], xyz[2]);
                        // check, if vertex with these coordinates exists
                        if (!verticesUnique.containsKey(vertexCoordinates))
                            // if not, add it to the collection of vertices
                            verticesUnique.put(vertexCoordinates, new Vertex(vertexCoordinates));
                        verticesTriangle[v] = verticesUnique.get(vertexCoordinates);

                    }
                    Triangle t = new Triangle(verticesTriangle[0], verticesTriangle[1], verticesTriangle[2]);
                    triangles.add(t);

                    // read attribute (unused)
                    short attribute = Short.reverseBytes(in.readShort());
                }
            } catch (Exception ex) {
            }
        } catch (IOException ex) {
        }

        return triangles;
    }

    // write mesh into file
    public static void writeMesh(Mesh mesh, String path) {
        try {
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);

            ByteBuffer buf = ByteBuffer.allocate(200);
            byte[] header = new byte[80];
            buf.get(header, 0, 80);

            boolean isColored = true;
            String tmp = "STLB ASM 218.00.00.0000 COLOR=....";
            for (int i = 0; isColored && i < 80; i++) {
                if (i < tmp.length()) {
                    header[i] = (byte) tmp.charAt(i);
                    if (header[i] == 0x2e)
                        header[i] = 0x19;
                } else if (i == tmp.length())
                    header[i] = -1;
                else
                    header[i] = 0x20;

            }
            out.write(header);

            buf.rewind();
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(mesh.triangles.size());
            buf.rewind();
            buf.get(header, 0, 4);
            out.write(header, 0, 4);

            buf.rewind();
            buf.clear();

            header = new byte[50];         //blue...
            int[] colors = new int[]{(1024 * 24 + 32 * 0 + 0), (1024 * 31 + 32 * 0 + 0), (1024 * 31 + 32 * 15 + 0), (1024 * 31 + 32 * 31 + 0), (1024 * 15 + 32 * 31 + 0),
                    (1024 * 0 + 32 * 31 + 0), (1024 * 0 + 32 * 31 + 15), (1024 * 0 + 32 * 31 + 31), (1024 * 0 + 32 * 15 + 31), (1024 * 0 + 32 * 0 + 31)}; //...red


            for (Triangle t : mesh.triangles) {

                buf.rewind();

                buf.putFloat((float) t.normal.x);
                buf.putFloat((float) t.normal.y);
                buf.putFloat((float) t.normal.z);


                buf.putFloat((float) t.vertices[0].v.x);
                buf.putFloat((float) t.vertices[0].v.y);
                buf.putFloat((float) t.vertices[0].v.z);

                buf.putFloat((float) t.vertices[1].v.x);
                buf.putFloat((float) t.vertices[1].v.y);
                buf.putFloat((float) t.vertices[1].v.z);

                buf.putFloat((float) t.vertices[2].v.x);
                buf.putFloat((float) t.vertices[2].v.y);
                buf.putFloat((float) t.vertices[2].v.z);
                buf.putShort((short) colors[Math.min(Math.max((int) (10 * (1 - Math.pow(2, -t.curv() * EdgePair.alpha))), 0), 9)]);

                buf.rewind();
                buf.get(header);
                out.write(header);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}