package de.ftk.threemf.mesh;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.UnknownFormatConversionException;

public class Main
{
    public static void main(String[] args)
    {
        try{
            // read command line arguments
            String fileNameInput = args[0];
            String fileNameOutput = args[1];
            // ??
            double f = Double.parseDouble(args[2]);
            // ??
            double alp = Double.parseDouble(args[3]);

            // read input file
            File fileInput = new File(fileNameInput);
            if(!fileInput.exists()) throw new FileNotFoundException("Input file not found: " + fileNameInput);
            Mesh m;
            if(fileNameInput.toLowerCase().endsWith(".stl"))
                // TODO: change fileNameInput to fileInput
                m = IoFileSTL.readMesh(fileNameInput);
            else if(fileNameInput.toLowerCase().endsWith(".model"))
                m = IoFile3MF.readMesh(fileInput);
            else
                throw new Exception("Unknown input filetype: " + fileNameInput);

            if(m != null) {
                EdgePair.alpha = alp;
                Mesh newM = Simplifier.simplifyMesh(m, f);
                IoFileSTL.writeMesh(newM, fileNameOutput);
            }

        } catch(Exception e){e.printStackTrace();}
    }

    private static String fileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if(index > 0) return fileName.substring(index + 1).toLowerCase();
        return null;
    }
}
