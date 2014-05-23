/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static voxels.Chunk.CHUNK_HEIGHT;
import static voxels.Chunk.CHUNK_WIDTH;
import static voxels.Voxels.WaterOffs;
import static voxels.Voxels.getCurrentChunkX;
import static voxels.Voxels.getCurrentChunkZ;

/**
 *
 * @author otso
 */
public class ChunkThread extends Thread {

    Chunk chunk;
    private int chunkX;
    private int chunkZ;
    boolean running = false;

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private boolean[][][] top = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] bottom = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] left = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] right = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] front = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] back = new boolean[Chunk.CHUNK_WIDTH][][];

    private FloatBuffer vertexData;
    private FloatBuffer normalData;
    private FloatBuffer texData;
    private FloatBuffer colorData;

    private float[] vertexArray;
    private float[] normalArray;
    private float[] texArray;
    private float[] colorArray;

    public ChunkThread(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        initBooleanArrays();
    }

    public void run() {
        running = true;
        chunk = new Chunk(chunkX, chunkZ);
    }

    public int calculateGroundVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int zMax = Chunk.CHUNK_WIDTH - 1;
        int xMax = Chunk.CHUNK_WIDTH - 1;
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        boolean render = false;
        int returnVertices = 0;
        int difference = 0;
        int[][] maxHeights = chunk.maxHeights;

        // front face
        if (z == zMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz + 1];
        }
        if (render || chunk.blocks[xx][yy][zz + 1].isType(Block.AIR) || chunk.blocks[xx][yy][zz + 1].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            front[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            front[xx][yy][zz] = false;

        // left face
        render = false;
        if (x == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx - 1][zz];
        }
        if (render || chunk.blocks[xx - 1][yy][zz].isType(Block.AIR) || chunk.blocks[xx - 1][yy][zz].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            left[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            left[xx][yy][zz] = false;

        // back face
        render = false;
        if (z == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz - 1];
        }
        if (render || chunk.blocks[xx][yy][zz - 1].isType(Block.AIR) || chunk.blocks[xx][yy][zz - 1].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            back[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            back[xx][yy][zz] = false;

        // right face
        render = false;
        if (x == xMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx + 1][zz];
        }
        if (render || chunk.blocks[xx + 1][yy][zz].isType(Block.AIR) || chunk.blocks[xx + 1][yy][zz].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            right[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            right[xx][yy][zz] = false;

        // top face
        render = false;
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].isType(Block.AIR) || chunk.blocks[xx][yy + 1][zz].isType(Block.WATER) && maxHeights[xx][zz] == yy) {
            top[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            top[xx][yy][zz] = false;

        // bottom face
//        render = false;
//        if (y == 0)
//            render = true;
//        if (render || chunk.blocks[xx][yy - 1][zz].isType(Block.AIR) || chunk.blocks[xx][yy - 1][zz].isType(Block.WATER) && maxHeights[xx][zz] + 1 == yy) {
//            bottom[xx][yy][zz] = true;
//            returnVertices += 6;
//        }
//        else
        bottom[xx][yy][zz] = false;
        return returnVertices;
    }

    public int calculateWaterVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        int returnVertices = 0;
        boolean render = false;

        // top face
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].isType(Block.AIR) && y == Chunk.WATER_HEIGHT) {
            top[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            top[xx][yy][zz] = false;

        return returnVertices;

    }

    private void drawChunkVBO(Chunk chunk, int xOff, int zOff) {
        long startTime = System.nanoTime();
        int drawnBlocks = 0;
        int vertices = 0;
        int size = 1;
        zOff += getCurrentChunkZ() * chunk.blocks.length;
        xOff += getCurrentChunkX() * chunk.blocks.length;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].isType(Block.GROUND)) {
                        vertices += calculateGroundVertices(chunk, x, y, z, getCurrentChunkX() * chunk.blocks.length + xOff, 0, getCurrentChunkZ() * chunk.blocks.length + zOff, 1);
                        drawnBlocks++;
                    }
                    else if (chunk.blocks[x][y][z].isType(Block.WATER)) {
                        vertices += calculateWaterVertices(chunk, x, y, z, getCurrentChunkX() * chunk.blocks.length + xOff, 0, getCurrentChunkZ() * chunk.blocks.length + zOff, 1);
                        drawnBlocks++;
                    }
                }
            }
        }
        //System.out.println("");
        chunk.setVertices(vertices);
        //System.out.println("Vertices: " + vertices);
        float[] vertexArray = new float[vertices * vertexSize];
        float[] normalArray = new float[vertices * normalSize];
        float[] texArray = new float[vertices * texSize];
        float[] colorArray = new float[vertices * colorSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;
        int cArrayPos = 0;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer texData = BufferUtils.createFloatBuffer(vertices * texSize);
        FloatBuffer colorData = BufferUtils.createFloatBuffer(vertices * colorSize);

        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].isType(Block.GROUND)) {
                        if (front[x][y][z]) {
                            // 1st
                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // lower right + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // lower right + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                        }
                        if (back[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            //2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // 1st
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            // lower left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // upper right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                        }
                        if (right[x][y][z]) {

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // 1st
                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // upper left + -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                        }
                        if (left[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // 1st
                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // upper left + +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;
                        }
                        if (top[x][y][z]) {

                            // 1st
                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
                            tArrayPos++;

                            // lower left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;

                            // 2nd
                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;

                            // upper right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                        }
                        if (bottom[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // 1st
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                        }
                    }
                    if (chunk.blocks[x][y][z].isType(Block.WATER)) {
                        if (top[x][y][z]) {

                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
                            tArrayPos++;

                            // lower left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
                            tArrayPos++;

                            // 2nd
                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
                            tArrayPos++;

                            // upper right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;
                            colorArray[cArrayPos] = 1;
                            cArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
                            tArrayPos++;
                        }

                    }
                }
            }
        }

        vertexData.put(vertexArray);
        vertexData.flip();

        normalData.put(normalArray);
        normalData.flip();

        texData.put(texArray);
        texData.flip();

        colorData.put(colorArray);
        colorData.flip();

        System.out.println("Time taken: " + (System.nanoTime() - startTime) / 1000000 + " ms.");

        running = false;

    }

    private void initBooleanArrays() {
        for (int x = 0; x < top.length; x++) {
            top[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < top[x].length; y++) {
                top[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < bottom.length; x++) {
            bottom[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < bottom[x].length; y++) {
                bottom[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < right.length; x++) {
            right[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < right[x].length; y++) {
                right[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < left.length; x++) {
            left[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < left[x].length; y++) {
                left[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < front.length; x++) {
            front[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < front[x].length; y++) {
                front[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < back.length; x++) {
            back[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < back[x].length; y++) {
                back[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
    }

    public static int getNormalSize() {
        return normalSize;
    }

    public FloatBuffer getVertexData() {
        return vertexData;
    }

    public FloatBuffer getNormalData() {
        return normalData;
    }

    public FloatBuffer getTexData() {
        return texData;
    }

    public FloatBuffer getColorData() {
        return colorData;
    }

    public float[] getVertexArray() {
        return vertexArray;
    }

    public float[] getTexArray() {
        return texArray;
    }

    public float[] getColorArray() {
        return colorArray;
    }

}