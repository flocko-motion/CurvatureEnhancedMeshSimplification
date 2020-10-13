# 3MF Curvature Enhanced Mesh Simplification

IN DEVELOPMENT

This project is currently not runnable - it's under heavy development and will be fully functional around 2021-02.

Mesh optimization for 3MF files as a webservice.

The method presented is an iterative edge contraction algorithm based on the work of Garland and Heckberts 
described in "Mesh Simplification by Curvature-Enhanced Quadratic Error Metrics". 
The original algorithm is improved by enhancing the quadratic error metrics with a penalizing factor 
based on discrete gaussian curvature, which is estimated efficiently through the Gauss-Bonnet theorem, 
to account for the presence of fine details during the edge decimation process.

The initial implementation of the algorithm was done by Paolo Pellizzoni. 