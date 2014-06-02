package voxels.ChunkManager;

import java.io.Serializable;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 16;
    public static final int WATER_HEIGHT = -1;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int xId;
    public final int zId;
    public final int yId;

    public Block[][][] blocks;
    public int[][] maxHeights;

    public Chunk(int xId, int yId, int zId) {
        this.xId = xId;
        this.zId = zId;
        this.yId = yId;
        xCoordinate = xId * CHUNK_WIDTH;
        zCoordinate = zId * CHUNK_WIDTH;
        initMaxHeights();
        setBlocks();
    }

    private void initMaxHeights() {
        maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int z = 0; z < CHUNK_WIDTH; z++) {
                maxHeights[x][z] = Voxels.getNoise(x + xCoordinate, z + zCoordinate);
            }
        }
    }

    private void setBlocks() {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (Voxels.USE_3D_NOISE) {
                        float noise1 = Voxels.get3DNoise(x + xCoordinate, y, z + zCoordinate) / 255f;
                        float noise2 = Voxels.get3DNoise(x + xCoordinate + 10000, y + 10000, z + zCoordinate + 10000) / 255f;

                        if (noise1 > 0.45f && noise1 < 0.55f && noise2 > 0.45f && noise2 < 0.55f)
                            blocks[x][y][z] = new Block(Type.DIRT);
                        else
                            blocks[x][y][z] = new Block(Type.AIR);
                    }
                    else {
                        if (y + Chunk.CHUNK_HEIGHT * yId > maxHeights[x][z] && y <= Chunk.WATER_HEIGHT) {
                            blocks[x][y][z] = new Block(Type.WATER);
                        }
                        else if (y + Chunk.CHUNK_HEIGHT * yId <= maxHeights[x][z])
                            blocks[x][y][z] = new Block(Type.DIRT);
                        else
                            blocks[x][y][z] = new Block(Type.AIR);
                    }
                }
            }
        }
    }

    public int getVboVertexHandle() {
        return vboVertexHandle;
    }

    public void setVboVertexHandle(int vboVertexHandle) {
        this.vboVertexHandle = vboVertexHandle;
    }

    public int getVertices() {
        return vertices;
    }

    public void setVertices(int vertices) {
        this.vertices = vertices;
    }

    public void setVboNormalHandle(int vboNormalHandle) {
        this.vboNormalHandle = vboNormalHandle;
    }

    public int getVboNormalHandle() {
        return vboNormalHandle;
    }

    public int getVboTexHandle() {
        return vboTexHandle;
    }

    public void setVboTexHandle(int vboTexHandle) {
        this.vboTexHandle = vboTexHandle;
    }

    public int getVboColorHandle() {
        return vboColorHandle;
    }

    public void setVboColorHandle(int vboColorHandle) {
        this.vboColorHandle = vboColorHandle;
    }

}
