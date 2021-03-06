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

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 *
 * @author otso
 */
public class BlockRenders {

    private Handle dirt;
    private Handle stone;
    private Handle leaves;
    private Handle cloud;
    private Handle wood;
    private Handle water;
    private Handle sand;
    private Handle cactus;
    private Handle rocksand;
    private Handle shore;

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private FloatBuffer vertexData;
    private FloatBuffer normalData;
    private FloatBuffer texData;

    public BlockRenders() {
        init();
    }

    private void init() {
        dirt = createVBO(Type.DIRT);
        stone = createVBO(Type.STONE);
        leaves = createVBO(Type.LEAVES);
        cloud = createVBO(Type.CLOUD);
        wood = createVBO(Type.WOOD);
        water = createVBO(Type.WATER);
        sand = createVBO(Type.SAND);
        cactus = createVBO(Type.CACTUS);
        rocksand = createVBO(Type.ROCKSAND);
        shore = createVBO(Type.SHORE);
    }

    private Handle createVBO(byte type) {

        int vertices = 24;

        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        texData = BufferUtils.createFloatBuffer(vertices * texSize);
        float frontXOff = AtlasManager.getFrontXOff(type);
        float frontYOff = AtlasManager.getFrontYOff(type);

        float backXOff = AtlasManager.getBackXOff(type);
        float backYOff = AtlasManager.getBackYOff(type);

        float rightXOff = AtlasManager.getRightXOff(type);
        float rightYOff = AtlasManager.getRightYOff(type);

        float leftXOff = AtlasManager.getLeftXOff(type);
        float leftYOff = AtlasManager.getLeftYOff(type);

        float topXOff;
        float topYOff;

        if (type != Type.DIRT) {
            topXOff = AtlasManager.getTopXOff(type);
            topYOff = AtlasManager.getTopYOff(type);
        } else {
            topXOff = AtlasManager.getTopXOff(Type.GRASS);
            topYOff = AtlasManager.getTopYOff(Type.GRASS);
        }

        float bottomXOff = AtlasManager.getBottomXOff(type);
        float bottomYOff = AtlasManager.getBottomYOff(type);

        float t = 1f / 16f;
 
       vertexData.put(new float[]{-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, // top
            -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, // bottom
            -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, // left
            0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, // right
            -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, // front
            -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f // back
    });
        vertexData.flip();

        normalData.put(new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, // top
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // bottom
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // left
            1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, // right
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1 // back
    });
        normalData.flip();

        texData.put(new float[]{topXOff, topYOff, topXOff, topYOff+t, topXOff+1, topYOff+t, topXOff+1, topYOff, // top
            bottomXOff, bottomYOff, bottomXOff+1, bottomYOff, bottomXOff+1, bottomYOff+t, bottomXOff, bottomYOff+t, // bottom
            leftXOff, leftYOff, leftXOff, leftYOff+t, leftXOff+1, leftYOff+t, leftXOff+1, leftYOff, // left
            rightXOff, rightYOff, rightXOff, rightYOff+t, rightXOff+1, rightYOff+t, rightXOff+1, rightYOff, // right
            frontXOff, frontYOff, frontXOff, frontYOff+t, frontXOff+1, frontYOff+t, frontXOff+1, frontYOff, // front
            backXOff, backYOff+t, backXOff+1, backYOff+t, backXOff+1, backYOff, backXOff, backYOff // back
    });
        texData.flip();
        

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        int vboTexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        return new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, vertices, null);
    }
    
    public Handle getHandle(byte type){
        if(type == Type.DIRT)
            return dirt;
        if(type == Type.STONE)
            return stone;
        if(type == Type.LEAVES)
            return leaves;
        if(type == Type.CLOUD)
            return cloud;
        if(type == Type.WOOD)
            return wood;
        if(type == Type.WATER)
            return water;
        if(type == Type.SAND)
            return sand;
        if(type == Type.CACTUS)
            return cactus;
        if(type == Type.ROCKSAND)
            return rocksand;
        if(type == Type.SHORE)
            return shore;
        else{
            System.out.println("No handle for block: "+type);
            return null;
        }
    }

    public Handle getDirt() {
        return dirt;
    }

    public Handle getStone() {
        return stone;
    }

    public Handle getLeaves() {
        return leaves;
    }

    public Handle getCloud() {
        return cloud;
    }

    public Handle getWood() {
        return wood;
    }
    
    
}
