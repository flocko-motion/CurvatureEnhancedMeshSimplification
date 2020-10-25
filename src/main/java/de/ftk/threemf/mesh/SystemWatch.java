package de.ftk.threemf.mesh;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemWatch {

    static String heapStatus() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        long heapUsedSize = heapSize - heapFreeSize;

        return ("Heap: "
                + String.format("%,8d", Math.round(heapUsedSize / 1024 / 1024)) + "/"
                + String.format("%,8d", Math.round(heapMaxSize / 1024 / 1024)) + " MB ("
                + String.format("%.1f", Float.valueOf(heapUsedSize) / Float.valueOf(heapMaxSize) * 100.0) + "%)"
        );
    }

    static float memUsage() {
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapUsedSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return Float.valueOf(heapUsedSize) / Float.valueOf(heapMaxSize);
    }
}
