/***************************************************************
* file: Checkpoint_2.java
* author: Joshua Pao, Luke Kimes
* class: CS 4450 - Computer Graphics
*
* assignment: Checkpoint 1
* date last modified: 11/07/2023
*
* purpose: This program adds to the previous checkpoint. It creates a chunk
* that contains 30x30 cubes with 6 random textures and applies noise to
* determine terrain.
****************************************************************/

package checkpoint_2;

import noise.SimplexNoise;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;
import org.lwjgl.util.glu.GLU;

// new imports for CP2:
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.*;

// TM
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class Checkpoint_2 {

    public static class Block {
        private boolean IsActive;
        private BlockType Type;
        private float x,y,z;
        
        public enum BlockType {
            BlockType_Grass(0),
            BlockType_Sand(1),
            BlockType_Water(2),
            BlockType_Dirt(3),
            BlockType_Stone(4),
            BlockType_Bedrock(5);
            
            private int BlockID;
            
            // method: BlockType
            // purpose: sets block ID for BlockType variables
            BlockType(int i) {
                BlockID = i;
            }
            
            // method: GetID
            // purpose: returns ID
            public int GetID(){
                return BlockID;
            }
            
            // method: SetID
            // purpose: set ID
            public void SetID(int i){
                BlockID = i;
            }
            
        }
        
        // method: Block
        // purpose: creates block, sets type    
        public Block(BlockType type){
            // BT variable (enum-ed earlier)
            Type = type;
        }
        
        // method: setCoords
        // purpose: sets the coords
        public void setCoords(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        // method: IsActive
        // purpose: returns IsActive    
        public boolean IsActive() {
            return IsActive;
        }
        
        // method: SetActive
        // purpose: sets IsActive 
        public void SetActive(boolean active){
            IsActive = active;
        }
        
        // method: GetID
        // purpose: returns ID
        public int GetID(){
            return Type.GetID();
        }
    }
    
    public static class Chunk {
        
        static final int CHUNK_SIZE = 30;   // 30x30 blocks per chunk
        static final int CUBE_LENGTH = 2;   // cubes are 2x2
        private Block[][][] Blocks;         // 3D array of block objects
        private int VBOVertexHandle;        
        private int VBOColorHandle;
        private int StartX, StartY, StartZ;
        private Random r;
        
        private int VBOTextureHandle;
        private Texture texture;
        
        // method: render
        // purpose: pushes and pop a matrix,
        // calls a number of gl methods to handle the matrix
        public void render(){
            glPushMatrix();
            glBindBuffer(GL_ARRAY_BUFFER,
            VBOVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER,
            VBOColorHandle);
            glColorPointer(3,GL_FLOAT, 0, 0L);
            
            glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
            glBindTexture(GL_TEXTURE_2D, 1);
            glTexCoordPointer(2,GL_FLOAT,0,0L);
            
            glDrawArrays(GL_QUADS, 0,
            CHUNK_SIZE *CHUNK_SIZE*
            CHUNK_SIZE * 24);
            glPopMatrix();
        }
        
        // method: rebuildMesh
        // purpose: applies noise to the chunk matrix to determine what to render,
        // creating vertical variations
    public void rebuildMesh(float startX, float startY, float startZ) {
        
        SimplexNoise noise = new SimplexNoise(30,0.7,420); // NG
            
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers(); // placed among the other VBOs // TM
        
        FloatBuffer VertexPositionData =
        BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = 
            BufferUtils.createFloatBuffer((CHUNK_SIZE* CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        
        //the following among our other Float Buffers before our for loops // TM
        FloatBuffer VertexTextureData =
            BufferUtils.createFloatBuffer((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE)* 6 * 12);
        
        for (float x = 0; x < CHUNK_SIZE; x += 1) {
            for (float z = 0; z < CHUNK_SIZE; z += 1) {
                double noise_result = noise.getNoise((int)x, (int)3, (int)z);
                System.out.println(noise_result);
                for(float y = 0; y < noise_result + 25; y++) {
                    
                    //double noise_result = noise.getNoise((int)x, (int)y, (int)z);
                    
                    VertexPositionData.put(createCube((float) (startX + x * CUBE_LENGTH), 
                                            (float)(y*CUBE_LENGTH+(int)(CHUNK_SIZE*.8)),
                                            (float) (startZ + z * CUBE_LENGTH)));
                    
                    VertexTextureData.put(createTexCube((float) 0, (float)
                        0, Blocks[(int)(x)][(int) (y)][(int) (z)])); // TM

                    VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[(int) x][(int) y][(int) z])));
                }
            }
        }
            VertexTextureData.flip(); // TM
            VertexColorData.flip();
            VertexPositionData.flip();
            
            glBindBuffer(GL_ARRAY_BUFFER,
            VBOVertexHandle);
            glBufferData(GL_ARRAY_BUFFER,
            VertexPositionData,
            GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ARRAY_BUFFER,
            VBOColorHandle);
            glBufferData(GL_ARRAY_BUFFER,
            VertexColorData,
            GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            
            glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
            glBufferData(GL_ARRAY_BUFFER, VertexTextureData,
            GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            
        }
    
        // method: createTexCube
        // purpose: creates a texture for a block by locating the texture on texture.png
    public static float[] createTexCube(float x, float y, Block block) {
        
        float offset = (1024f/16)/1024f;
        switch (block.GetID()) {
            // grass = 0, sand = 1, water = 2, dirt = 3, stone = 4, bedrock = 5
            case 0: // GRASS
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*10,
                    x + offset*2, y + offset*10,
                    x + offset*2, y + offset*9,
                    x + offset*3, y + offset*9,
                    // TOP!
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // FRONT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    // BACK QUAD
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    // LEFT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    // RIGHT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1 };
            case 1: // SAND
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // TOP!
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // FRONT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // BACK QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // LEFT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // RIGHT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1 };
            case 2: // WATER
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12,
                    // TOP!
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12,
                    // FRONT QUAD
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12,
                    // BACK QUAD
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12,
                    // LEFT QUAD
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12,
                    // RIGHT QUAD
                    x + offset*14, y + offset*13,
                    x + offset*13, y + offset*13,
                    x + offset*13, y + offset*12,
                    x + offset*14, y + offset*12 };
            case 3: // DIRT
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // TOP!
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // FRONT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // BACK QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // LEFT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // RIGHT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0 };
            case 4: // STONE
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // TOP!
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // FRONT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // BACK QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // LEFT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // RIGHT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0 };
            case 5: // BEDROCK            
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // TOP!
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // FRONT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // BACK QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // LEFT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // RIGHT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1 };
            default:    // just more dirt
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            
        }
    }
        
        // method: createCubeVertexCol
        // purpose: returns an array of cube colors
        private float[] createCubeVertexCol(float[] CubeColorArray) {
            float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
            for (int i = 0; i < cubeColors.length; i++) {
                cubeColors[i] = CubeColorArray[i %
                CubeColorArray.length];
            }
            return cubeColors;
        }
        
        // method: createCube
        // purpose: creates a cube array of size x y x
        public static float[] createCube(float x, float y, float z) {
            int offset = CUBE_LENGTH / 2;
            return new float[] {
                // TOP QUAD
                x + offset, y + offset, z,
                x - offset, y + offset, z,
                x - offset, y + offset, z - CUBE_LENGTH,
                x + offset, y + offset, z - CUBE_LENGTH,
                // BOTTOM QUAD
                x + offset, y - offset, z - CUBE_LENGTH,
                x - offset, y - offset, z - CUBE_LENGTH,
                x - offset, y - offset, z,
                x + offset, y - offset, z,
                // FRONT QUAD
                x + offset, y + offset, z - CUBE_LENGTH,
                x - offset, y + offset, z - CUBE_LENGTH,
                x - offset, y - offset, z - CUBE_LENGTH,
                x + offset, y - offset, z - CUBE_LENGTH,
                // BACK QUAD
                x + offset, y - offset, z,
                x - offset, y - offset, z,
                x - offset, y + offset, z,
                x + offset, y + offset, z,
                // LEFT QUAD
                x - offset, y + offset, z - CUBE_LENGTH,
                x - offset, y + offset, z,
                x - offset, y - offset, z,
                x - offset, y - offset, z - CUBE_LENGTH,
                // RIGHT QUAD
                x + offset, y + offset, z,
                x + offset, y + offset, z - CUBE_LENGTH,
                x + offset, y - offset, z - CUBE_LENGTH,
                x + offset, y - offset, z };
        }
        
        // method: getCubeColor
        // purpose: returns a float array that makes a cube "lit up"
        private float[] getCubeColor(Block block) {
            
            return new float[] { 1, 1, 1 };
            
        }
        
        // method: Chunk
        // purpose: defines a new chunk, randomly assigning block types
        // calls rebuildMesh
        public Chunk(int startX, int startY, int startZ) {
            // TM:
            try{texture = TextureLoader.getTexture("PNG",
                ResourceLoader.getResourceAsStream("/Users/lukekimes/cs4450/terrain.png"));
            }
            catch(Exception e)
            {
                System.out.println(e);
            }

            r = new Random();
            r.setSeed(123456789);
            
            Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    for (int z = 0; z < CHUNK_SIZE; z++) {
                        float randomNum = r.nextFloat();
                        //System.out.println(randomNum);
                        if(randomNum > 0.7f){
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Grass);
                            //System.out.println("grass!");
                        } else if(randomNum > 0.6f){
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand);
                            //System.out.println("sand!");
                        } else if(randomNum > 0.5f){
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Water);
                            //System.out.println("water!");
                        } else if(randomNum > 0.4f) {
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Dirt);
                            //System.out.println("dirt!");
                        } else if(randomNum > 0.1f) {
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                            //System.out.println("stone!");                            
                        } else {
                           Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Bedrock);
                           //System.out.println("bedrock!");
                        }
                    }
                }
            }
            
            VBOColorHandle = glGenBuffers();
            VBOVertexHandle = glGenBuffers();
            VBOTextureHandle = glGenBuffers(); //along with our other VBOs // TM
            StartX = startX;
            StartY = startY;
            StartZ = startZ;
            rebuildMesh(startX, startY, startZ);
        }
    }    
    
    // class: creates and contains functions for the camera
    // functions within: create camera, movements, renders cubes
    public static class FPCameraController {
        //3d vector to store the camera's position in
        private Vector3f position = null;
        private Vector3f lPosition = null;
        //the rotation around the Y axis of the camera
        private float yaw = 0.0f;
        //the rotation around the X axis of the camera
        private float pitch = 0.0f;
        //private Vector3Float me;
    
        // method: FPCameraController
        // purpose: this method creates a new camera
        public FPCameraController(float x, float y, float z) {
            //instantiate position Vector3f to the x y z params.
            position = new Vector3f(x, y, z);
            lPosition = new Vector3f(x,y,z);
            lPosition.x = 0f;
            lPosition.y = 15f;
            lPosition.z = 0f;
        }

        // method: yaw
        //increment the camera's current yaw rotation
        public void yaw(float amount)
        {
            //increment the yaw by the amount param
            yaw += amount;
        }

        // method: pitch
        // increment the camera's current pitch rotation
        public void pitch(float amount)
        {
            //increment the pitch by the amount param
            pitch -= amount;
        }

        // method: walkForward 
        // moves the camera forward relative to its current rotation (yaw)
        public void walkForward(float distance)
        {
            float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
            float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
            position.x -= xOffset;
            position.z += zOffset;
        }

        // method: walkBackwards
        // moves the camera backward relative to its current rotation (yaw)
        public void walkBackwards(float distance)
        {
            float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
            float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
            position.x += xOffset;
            position.z -= zOffset;
        }
        
        // method: strafeLeft
        // strafes the camera left relative to its current rotation (yaw)
        public void strafeLeft(float distance)
        {
            float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
            float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
            position.x -= xOffset;
            position.z += zOffset;
        }

        // method: strafeRight
        // strafes the camera right relative to its current rotation (yaw)
        public void strafeRight(float distance)
        {
            float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
            float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
            position.x -= xOffset;
            position.z += zOffset;
        }

        // method: moveUp
        // moves the camera up relative to its current rotation (yaw)
        public void moveUp(float distance)
        {
            position.y -= distance;
        }

        // method: moveDown
        // moves the camera down
        public void moveDown(float distance)
        {
            position.y += distance;
        }

        // method: lookThrough
        // translates and rotate the matrix so that it looks through the camera
        // this does basically what gluLookAt() does
        public void lookThrough()
        {
            //roatate the pitch around the X axis
            glRotatef(pitch, 1.0f, 0.0f, 0.0f);
            //roatate the yaw around the Y axis
            glRotatef(yaw, 0.0f, 1.0f, 0.0f);
            //translate to the position vector's location
            glTranslatef(position.x, position.y, position.z);
        }    
        
        // method: gameLoop
        // creates the camera, grabs mouse movements and listens for keyboard
        // presses, which will be sent to functions to update the camera
        // and then calls to render the new scene
        public void gameLoop()
        {
            FPCameraController camera = new FPCameraController(-30, -85, -30);
            Chunk chunky = new Chunk(0,0,0);
            float dx = 0.0f;
            float dy = 0.0f;
            float dt = 0.0f; //length of frame
            float lastTime = 0.0f; // when the last frame was
            long time = 0;
            float mouseSensitivity = 0.09f;
            float movementSpeed = .35f;
            //hide the mouse
            Mouse.setGrabbed(true);

            // keep looping till the display window is closed the ESC key is down
            while (!Display.isCloseRequested() &&
            !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            {
            time = Sys.getTime();
            lastTime = time;
            //distance in mouse movement
            //from the last getDX() call.
            dx = Mouse.getDX();
            //distance in mouse movement
            //from the last getDY() call.
            dy = Mouse.getDY();
            //controll camera yaw from x movement fromt the mouse
            camera.yaw(dx * mouseSensitivity);
            //controll camera pitch from y movement fromt the mouse
            camera.pitch(dy * mouseSensitivity);
                    //when passing in the distance to move
            //we times the movementSpeed with dt this is a time scale
            //so if its a slow frame u move more then a fast frame
            //so on a slow computer you move just as fast as on a fast computer
            if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
            {
                camera.walkForward(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
            {
                camera.walkBackwards(movementSpeed);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left 
            {
                camera.strafeLeft(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right 
            {
                camera.strafeRight(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))//move up 
            {
                camera.moveUp(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                camera.moveDown(movementSpeed);
            }

            //set the modelview matrix back to the identity
            glLoadIdentity();
            //look through the camera before you draw anything
            camera.lookThrough();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            //you would draw your scene here.

            // FROM CP1: (camera render method)
            // render();
            // CP2: use chunk render
            chunky.render();
            
            //draw the buffer to the screen
            Display.update();
            Display.sync(60);
            }
            Display.destroy();
        }
    
        // method: render
        // renders a cube with different colored sides, and draws an outline for it
        private void render() {
        try{

        // no longer used from CP1
            glBegin(GL_QUADS);
            //Top
            glColor3f(1.0f,0.0f,0.0f);
            glVertex3f( 1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f( 1.0f, 1.0f, 1.0f);
            //Bottom
            glColor3f(0.0f,0.0f,1.0f);
            glVertex3f( 1.0f,-1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f( 1.0f,-1.0f,-1.0f);
            //Front
            glColor3f(0.0f,1.0f,0.0f);
            glVertex3f( 1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f, 1.0f);
            //Back
            glColor3f(0.5f,0.0f,0.5f);
            glVertex3f( 1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f( 1.0f, 1.0f,-1.0f);
            //Left
            glColor3f(1.0f,0.5f,0.5f);
            glVertex3f(-1.0f, 1.0f,1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            //Right
            glColor3f(0.2f,0.5f,0.5f);
            glVertex3f( 1.0f, 1.0f,-1.0f);
            glVertex3f( 1.0f, 1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();
            
            glBegin(GL_LINE_LOOP);
            //Top
            glColor3f(0.0f,0.0f,0.0f);
            glVertex3f( 1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f( 1.0f, 1.0f, 1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
            //Bottom
            glVertex3f( 1.0f,-1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
            //Front
            glVertex3f( 1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f, 1.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            //Back
            glVertex3f( 1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f( 1.0f, 1.0f,-1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
            //Left
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, 1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f,-1.0f);
            glVertex3f(-1.0f,-1.0f, 1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
            //Right
            glVertex3f( 1.0f, 1.0f,-1.0f);
            glVertex3f( 1.0f, 1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f, 1.0f);
            glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();
            
        } catch(Exception e){}
    }
    }
    
    // class: Basic3D
    // mostly handles creating the window/space
    public static class Basic3D {
        //private FPCameraController fp = new FPCameraController(0f,0f,0f);
        private DisplayMode displayMode;
        
        public void start(FPCameraController fp) {
            try {
                createWindow();
                initGL();
                fp.gameLoop(); //render();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // method: createWindow
        // purpose: this method creates the window.
        private void createWindow() throws Exception{
            Display.setFullscreen(false);
            DisplayMode d[] =
            Display.getAvailableDisplayModes();
            for (int i = 0; i < d.length; i++) {
                if (d[i].getWidth() == 640 && d[i].getHeight() == 480 
                                           && d[i].getBitsPerPixel() == 32) {
                    displayMode = d[i];
                    break;
                }
            }
            Display.setDisplayMode(displayMode);
            Display.setTitle("Generic Block Engine");
            Display.create();
        }
    
        // method: initGL
        // purpose: this method initializes the window. In this program, 
        // the origin is set to the center.
        private void initGL() {
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float)
            displayMode.getHeight(), 0.1f, 300.0f);
            glMatrixMode(GL_MODELVIEW);
            glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
            
            // NEW FROM CP2 
            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_COLOR_ARRAY);
            glEnable(GL_DEPTH_TEST);
            
            glEnable(GL_TEXTURE_2D);
            glEnableClientState (GL_TEXTURE_COORD_ARRAY);
        }

        public class Vector3Float {
            public float x, y, z;
            public Vector3Float(int x, int y, int z){
            this.x=x;
            this.y=y;
            this.z=z;
            }
        }
        
    }

    // method: main
    // creates a new Basic3D object and starts the program
    public static void main(String[] args) {
        FPCameraController fp = new FPCameraController(0f,0f,0f);
        Basic3D basic = new Basic3D();
        basic.start(fp);
        
        }
    }
