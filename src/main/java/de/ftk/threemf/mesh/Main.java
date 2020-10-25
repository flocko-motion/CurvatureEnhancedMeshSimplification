package de.ftk.threemf.mesh;


import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class Main
{
    public static void main(String[] args)
    {
        try{
            if (args.length != 4) {
                System.out.println("Usage: MeshOptimizer <infile> <outfile> <shrinkfactor> <triangles>");
                System.out.println("infile:       path to 3MF .model file");
                System.out.println("outfile:      path for writing 3MF .model file");
                System.out.println("shrinkfactor: (>0, <1) how much should we shrink each mesh?");
                System.out.println("triangles:    (>0) max allowed triangles per mesh");
                System.exit(1);
            }
            // read command line arguments
            String fileNameInput = args[0];
            String fileNameOutput = args[1];
            // shrinking parameters
            double shrinkFactor = Double.parseDouble(args[2]);
            long maxTrianglesPerMesh = Long.parseLong(args[3]);

            File fileInput = new File(fileNameInput);
            if(!fileInput.exists()) throw new FileNotFoundException("Input file not found: " + fileNameInput);
            InputStream inputStream = new FileInputStream(fileInput);

            File fileOutput = new File(fileNameOutput);
            OutputStream outputStream = new FileOutputStream(fileOutput);

            Simplifier simplifier = new Simplifier();
            if(shrinkFactor > 0) simplifier.setShrinkFactor(shrinkFactor);
            if(maxTrianglesPerMesh > 0) simplifier.setMaxTrianglesPerMesh(maxTrianglesPerMesh);

            Lib3MF.processStream(simplifier, inputStream, outputStream);

        } catch(Exception e){e.printStackTrace();}
    }

    private static String fileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if(index > 0) return fileName.substring(index + 1).toLowerCase();
        return null;
    }
}
