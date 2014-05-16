/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import voxels.Noise.FastNoise;

/**
 *
 * @author otso
 */
public class Chunk {

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;
    public final int chunkX;
    public final int chunkZ;
    public final int xOff;
    public final int zOff;

    public Block[][][] blocks;

    public Chunk(int chunkX, int chunkZ) {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.xOff = CHUNK_WIDTH * chunkX;
        this.zOff = CHUNK_WIDTH * chunkZ;

        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    blocks[x][y][z] = new Block();
                }
            }
        }
        setAirBlocks();
        setInsideAirBlocks();
    }

    private void setAirBlocks() {
        int airBlockCount = 0;
        int noise;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                noise = getNoise(x + xOff, z + zOff);
                for(int y = 0; y< blocks[x].length; y++){
                    if(y>noise){
                        blocks[x][y][z].deactivate();
                        airBlockCount++;
                    }
                }
            }
        }

        System.out.println("Deactivated blocks outside: "+airBlockCount);
    }
    private void setInsideAirBlocks() {
        int airBlockCount = 0;
        for (int x = 1; x < blocks.length-1; x++) {
            for (int z = 1; z < blocks[x][0].length-1; z++) {
                for(int y = 1; y< blocks[x].length-1; y++){
                    // if block is surrounded by active blocks, mark it for deactivation
                    if(blocks[x+1][y][z].isActive() && blocks[x-1][y][z].isActive()&& blocks[x][y][z+1].isActive() && blocks[x][y][z-1].isActive() && blocks[x][y+1][z].isActive() && blocks[x][y-1][z].isActive())
                        blocks[x][y][z].markToDeactivate();
                }
            }
        }
        for (int x = 1; x < blocks.length-1; x++) {
            for (int z = 1; z < blocks[x][0].length-1; z++) {
                for(int y = 1; y< blocks[x].length-1; y++){
                    if(blocks[x][y][z].isMarked()){
                        blocks[x][y][z].deactivate();
                        airBlockCount++;
                    }
                }
            }
        }
        System.out.println("Deactivated blocks inside: "+airBlockCount);
    }

    private static int getNoise(float x, float z) {
        return (FastNoise.noise((x) / 200f, (z) / 200f, 7));
    }

}
