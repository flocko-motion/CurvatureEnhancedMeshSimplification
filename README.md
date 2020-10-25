# Streaming 3MF Curvature Enhanced Mesh Simplification

## Project status

IN DEVELOPMENT

First runnable build. 

The program currently doesn't process 3MF files yet, but only the .model files which can be found
at 3D/3dmodel.model after unzipping the 3MF file. The .model file can then be optimized using this program
and then placed back inside the zip file.

Next development step is:
Stream 3MF file, unzip on-the-fly, optimize meshes and pack everything back into 3MF file.

# Project description 

Library for stream based simplification of 3MF files. Streams could origin from e.g. file, network connection or 
database and be streamed back into any sink. This allows for integrating this library in a variety of use cases.

The method presented is an iterative edge contraction algorithm based on the work of Garland and Heckberts 
described in "Mesh Simplification by Curvature-Enhanced Quadratic Error Metrics". 
The original algorithm is improved by enhancing the quadratic error metrics with a penalizing factor 
based on discrete gaussian curvature, which is estimated efficiently through the Gauss-Bonnet theorem, 
to account for the presence of fine details during the edge decimation process.

# Credits

The algorithm was created by Garland and Heckberts and is described in "Mesh Simplification by Curvature-Enhanced Quadratic Error Metrics". 

The initial implementation of the algorithm was done by Paolo Pellizzoni. 

