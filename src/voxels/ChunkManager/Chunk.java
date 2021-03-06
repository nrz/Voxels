/* 
 * Copyright (C) 2016 Otso Nuortimo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package voxels.ChunkManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import voxels.Noise.RandomNumber;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_SIZE = 32;
    public static final int VERTICAL_CHUNKS = 8;
    public static final int WORLD_HEIGHT = CHUNK_SIZE * VERTICAL_CHUNKS;
    public static final int WATER_HEIGHT = 85;
    public static final int SHORE_HEIGHT = WATER_HEIGHT + 2;
    public static final int FORCED_AIR_LAYERS = 10;
    public static final float GROUND_SHARE = 0.9f;
    public static final int DIRT_LAYERS = 5;

    //3d noise min and max values
    public static final float noiseOneMin = 0.40f;
    public static final float noiseOneMax = 0.55f;
    public static final float noiseTwoMin = 0.40f;
    public static final float noiseTwoMax = 0.55f;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int yCoordinate;
    public final int xId;
    public final int zId;
    public final int yId;

    //public Block[][][] blocks;
    public short[][] maxHeights;
    public byte[][] biomes;

    public byte[][][] blocks;
    private boolean updateActive = false;
    private boolean updatePacked = false;
    private boolean empty = true;
    
    private Random r;

    public Chunk(int xId, int yId, int zId) {
        r = new Random(xId*1000+yId*100+zId*10);
        blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.xId = xId;
        this.zId = zId;
        this.yId = yId;
        xCoordinate = xId * CHUNK_SIZE;
        zCoordinate = zId * CHUNK_SIZE;
        yCoordinate = yId * CHUNK_SIZE;
        initMaxHeights();
        //initNoiseArray();
        //if(!empty || (Chunk.CHUNK_SIZE * yId+Chunk.CHUNK_SIZE) > WORLD_HEIGHT * GROUND_SHARE)
        setBlocks();
    }

    private void initMaxHeights() {
        maxHeights = new short[CHUNK_SIZE][CHUNK_SIZE];
        biomes = new byte[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                maxHeights[x][z] = (short) Voxels.getNoise(x + xCoordinate, z + zCoordinate);
                biomes[x][z] = Voxels.getBiomeNoise(x + xCoordinate, z + zCoordinate);
                if (yCoordinate + CHUNK_SIZE >= maxHeights[x][z]) {
                    empty = false;
                }
            }
        }
//        if(empty)
//            System.out.println("empty");
    }

    private void setBlocks() {
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {

                    // Make the terrain using 2d noise
                    if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z] && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                        if (y == 0 && yId == 1) {
                            blocks[x][y][z] = Type.UNBREAKABLE;
                        } else if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z] - DIRT_LAYERS) {
                            blocks[x][y][z] = Type.STONE;
                        } else {
                            blocks[x][y][z] = biomes[x][z];
                        }
                    }
                    if (y == 0 && yId == 1) {
                        blocks[x][y][z] = Type.UNBREAKABLE;
                    }

                    // add 3d noise if enabled
                    if (Voxels.USE_3D_NOISE) {

                        //only add 3d noise to the upper part of the world (clouds)
                        if (y + Chunk.CHUNK_SIZE * yId > WORLD_HEIGHT * GROUND_SHARE) {
                            float noise1 = Voxels.get3DNoise((x + xCoordinate), (y + yCoordinate) * 3, (z + zCoordinate)) / (float) (CHUNK_SIZE * VERTICAL_CHUNKS);
                            if (noise1 > 0.93f && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                                blocks[x][y][z] = Type.CLOUD;
                            }
                            // modify the ground portion of the world (caves)
                        } else if (!empty) {
                            if (yId != 1 || y != 0) {
                                if (Voxels.getCaveNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate)) {
                                    if(Voxels.MAKE_TREES_AND_CAVES)
                                        blocks[x][y][z] = Type.AIR;
                                }

                            }
                            if (y + yCoordinate >= WATER_HEIGHT && y + yCoordinate <= SHORE_HEIGHT || y + yCoordinate < WATER_HEIGHT && y + yCoordinate == maxHeights[x][z]) {
                                if (blocks[x][y][z] != Type.AIR) {
                                    blocks[x][y][z] = Type.SHORE;
                                }
                            }

                            if (y + yCoordinate <= WATER_HEIGHT) {
                                if (blocks[x][y][z] == Type.AIR) {
                                    blocks[x][y][z] = Type.WATER;
                                }
                            }

                            // add trees
                            if(Voxels.MAKE_TREES_AND_CAVES){
                                if (y + Chunk.CHUNK_SIZE * yId == maxHeights[x][z] + 1 && y + yCoordinate - 1 > SHORE_HEIGHT) {
                                    if (Voxels.getTreeNoise(x + CHUNK_SIZE * xId, y + yCoordinate - 1, z + CHUNK_SIZE * zId)) {
                                        if (biomes[x][z] == Type.DIRT) {
                                            createTree(x + CHUNK_SIZE * xId, y + yCoordinate, z + CHUNK_SIZE * zId);
                                        } else if (biomes[x][z] == Type.SAND) {
                                            createCactus(x + CHUNK_SIZE * xId, y + yCoordinate, z + CHUNK_SIZE * zId);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        //();
    }

    private void createTree(int x, int y, int z) {
        int width = (int) (4 + r.nextDouble() * 5);
        int height = (int) (5 + r.nextDouble() * 12);
        boolean bigTree = false;
        if (r.nextDouble() > 0.995f) {
            width *= 3;
            height *= 5;
            bigTree = true;

        }
        if (y + height >= WORLD_HEIGHT) {
            height = WORLD_HEIGHT - 1 - y;
        }
        // trunk
        for (int i = 0; i < height; i++) {
            Voxels.putToBuffer(Type.WOOD, x, y + i, z);
            if (bigTree) {
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z);
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z - 1);
                Voxels.putToBuffer(Type.WOOD, x, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x, y + i, z - 1);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z - 1);
            }
        }
        int startingWidth = width;
        for (int yy = y + 1 + height / 3; yy < y + 1 + height / 3 + height; yy++) {
            for (int zz = -width / 2 + z; zz <= width / 2 + z; zz++) {
                for (int xx = -width / 2 + x; xx <= width / 2 + x; xx++) {
                    if (r.nextDouble() < 0.65f) {
                        Voxels.putToBuffer(Type.LEAVES, xx, yy, zz);
                    }

                }
            }
            width = startingWidth - (int) (startingWidth * (((yy - height / 2f - y) / (float) (height - 2))));
        }

    }

    private void createCactus(int x, int y, int z) {
        int height = (int) (5 + r.nextDouble() * 4);

        if (y + height >= WORLD_HEIGHT) {
            height = WORLD_HEIGHT - 1 - y;
        }
        // trunk
        for (int i = 0; i < height; i++) {
            Voxels.putToBuffer(Type.CACTUS, x, y + i, z);
        }
        boolean leftBranch = r.nextDouble() > 0.75f;
        boolean rightBranch = r.nextDouble() > 0.75f;
        boolean frontBranch = r.nextDouble() > 0.75f;
        boolean backBranch = r.nextDouble() > 0.75f;

        if (leftBranch) {
            Voxels.putToBuffer(Type.CACTUS, x - 1, y + height / 2, z);
            Voxels.putToBuffer(Type.CACTUS, x - 2, y + height / 2, z);
            Voxels.putToBuffer(Type.CACTUS, x - 2, y + height / 2 + 1, z);
        }
        if (rightBranch) {
            Voxels.putToBuffer(Type.CACTUS, x + 1, y + height / 2, z);
            Voxels.putToBuffer(Type.CACTUS, x + 2, y + height / 2, z);
            Voxels.putToBuffer(Type.CACTUS, x + 2, y + height / 2 + 1, z);
        }
        if (frontBranch) {
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2, z + 1);
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2, z + 2);
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2 + 1, z + 2);
        }
        if (backBranch) {
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2, z - 1);
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2, z - 2);
            Voxels.putToBuffer(Type.CACTUS, x, y + height / 2 + 1, z - 2);
        }
    }

    public boolean checkBuffer() {
        boolean updated = false;
        if (Voxels.getBlockBuffer().containsKey(new Triple(xId, yId, zId).hashCode())) {
            BlockingQueue queue = Voxels.getBlockBuffer().get(new Triple(xId, yId, zId).hashCode());
            Iterator i = queue.iterator();
            while (i.hasNext()) {
                updated = true;
                BlockCoord bc = (BlockCoord) i.next();
                i.remove();
                setUpdateActive(true);
                setUpdatePacked(true);

                blocks[bc.x][bc.y][bc.z] = bc.Type;
            }
        }
        return updated;
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

    public boolean isUpdateActive() {
        return updateActive;
    }

    public boolean isUpdatePacked() {
        return updatePacked;
    }

    public void setUpdatePacked(boolean updatePacked) {
        this.updatePacked = updatePacked;
    }

    public void setUpdateActive(boolean updateActive) {
        this.updateActive = updateActive;
    }

    public void setBlock(int x, int y, int z, byte type) {
        blocks[x][y][z] = type;
    }
}
