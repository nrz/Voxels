package voxels.ChunkManager;



import java.util.concurrent.ConcurrentHashMap;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class ChunkCoordinateCreator {

    private int currentChunkX;
    private int currentChunkZ;
    private int maxDistance = Voxels.chunkCreationDistance;
    private int dx = 1;
    private int dz = -1;
    private int x = 0;
    private int z = 0;
    private int currentLength = 0;
    private int length = 1;
    private int count = 0;
    private int turnCount = 0;
    private boolean needMiddleChunk = true;
    private ConcurrentHashMap<Integer, byte[]> map;
    
    

    public ChunkCoordinateCreator() {

    }

    ChunkCoordinateCreator(ConcurrentHashMap<Integer, byte[]> map) {
        this.map = map;
    }

    Coordinates getNewCoordinates() {
        if (needMiddleChunk) {
            needMiddleChunk = false;
            if (!map.containsKey(new Pair(currentChunkX, currentChunkZ).hashCode()))
                return new Coordinates(currentChunkX, null, currentChunkZ);
        }

        while (notAtMax()) {
            if (dz != 0) {
                z += dz;
                currentLength++;
                if (currentLength == length) {
                    currentLength = 0;
                    dx = dz;
                    dz = 0;
                    turnCount++;
                    if (turnCount == 2)
                        length++;
                }
            }
            else {
                x += dx;
                currentLength++;
                if (currentLength == length) {
                    currentLength = 0;
                    dz = -dx;
                    dx = 0;
                    turnCount++;
                    if (turnCount == 2) {
                        length++;
                        turnCount = 0;
                    }
                }
            }
            if (!map.containsKey(new Pair(x + currentChunkX, z + currentChunkZ).hashCode()))
                return new Coordinates(x + currentChunkX, null, z + currentChunkZ);
        }

        return null;
    }

    public void setCurrentChunkX(int currentChunkX) {
        if (currentChunkX != this.currentChunkX)
            reset();
        this.currentChunkX = currentChunkX;
    }

    public void setCurrentChunkZ(int currentChunkZ) {
        if (currentChunkZ != this.currentChunkZ)
            reset();
        this.currentChunkZ = currentChunkZ;
    }

    private void reset() {
        dx = 1;
        dz = -1;
        x = 0;
        z = 0;
        currentLength = 0;
        length = 1;
        count = 0;
        turnCount = 0;
        needMiddleChunk = true;
    }

    public boolean notAtMax() {
        return Math.abs(x) <= maxDistance && Math.abs(z) <= maxDistance;
    }
}