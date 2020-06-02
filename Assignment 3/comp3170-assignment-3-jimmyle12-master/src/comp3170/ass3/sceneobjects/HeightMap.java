package comp3170.ass3.sceneobjects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import comp3170.SceneObject;
import comp3170.Shader;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.json.JSONArray;

public class HeightMap extends SceneObject {


    private final float TAU = (float) (Math.PI * 2);
    private final int N_DIVISIONS = 20;

    private Vector3f[] vertices;
    private int vertexBuffer;

    private Vector2f[] uvs;
    private int uvBuffer;

    private Vector3f[] faceNormals;
    private int faceNormalBuffer;

    private Vector3f[] vertexNormals;
    private int vertexNormalBuffer;

    private Vector3f[] barycentric;
    private int barycentricBuffer;

    private float[] colour = {0.1f, 0.8f, 0.1f};
    private float specularity = 10;

    private int width;
    private int depth;

    private int texture;

    private Matrix3f normalMatrix;
    private Vector4f lightDir;
    private Vector4f viewDir;

    public HeightMap(Shader shader, int width, int depth, JSONArray heightArray, int texture) {
        super(shader);
        this.width = width;
        this.depth = depth;
        this.texture = texture;

        //
        // Load the array of heights from the JSONArray
        //


        float[][] height = new float[width][depth];

        int k = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < width; i++) {
                height[i][j] = heightArray.getFloat(k++) ;
            }
        }

        int nSquares = (width - 1) * (depth - 1);
        System.out.println("n squares = " + nSquares);

        vertices = new Vector3f[2 * 3 * nSquares];
        this.faceNormals = new Vector3f[3 * 2 * nSquares];
        this.vertexNormals = new Vector3f[3 * 2 * nSquares];
        uvs = new Vector2f[vertices.length];
//		colour = new float[4 *3 * vertices.length];


        int v = 0;
        int nfn = 0;
        int nvn = 0;
        int c = 0;
        for (k = 0; k < depth - 1; k++) {
            for (int l = 0; l < width - 1; l++) {
                float x = getX(k);
                float z = getZ(l);
                float y = height[k][l];
                System.out.println("Square " + k + " " + l + " has coords " + x +
                        " " + z + " " + y);
                float x1 = getX(k + 1);
                float z1 = getZ(l + 1);
                float ykl1 = height[k][l + 1];
                float yk1l = height[k + 1][l];
                {
                    //creating first triangle
                    Vector3f p0, p1, p2;
                    p0 = vertices[v++] = new Vector3f(x, y, z);
                    uvs[c++] = new Vector2f(1, 0);
                    p1 = vertices[v++] = new Vector3f(x, ykl1, z1);
                    uvs[c++] = new Vector2f(0,0);
                    p2 = vertices[v++] = new Vector3f(x1, yk1l, z);
                    uvs[c++] = new Vector2f(0,1);


                    // compute face normals using cross product
                    Vector3f v10 = new Vector3f();
                    Vector3f v20 = new Vector3f();

                    p1.sub(p0, v10);    // v10 = p1 - p0
                    p2.sub(p0, v20);    // v20 = p2 - p0
                    Vector3f fn = new Vector3f();
                    v10.cross(v20, fn);    // fn = v10 x v20;

                    faceNormals[nfn++] = fn;
                    faceNormals[nfn++] = fn;
                    faceNormals[nfn++] = fn;

                    // vertex normals point straight out

                    vertexNormals[nvn++] = new Vector3f(x, 0, z).normalize();
                    vertexNormals[nvn++] = new Vector3f(x, 0, z1).normalize();
                    vertexNormals[nvn++] = new Vector3f(x1, 0, z).normalize();

                }
                {
                    Vector3f p0, p1, p2;
                    //creating second triangle to make a square
                    float yk1l1 = height[k + 1][l + 1];
                    p0 = vertices[v++] = new Vector3f(x1, yk1l, z);
                    uvs[c++] = new Vector2f(0,1);
                    p1 = vertices[v++] = new Vector3f(x, ykl1, z1);
                    uvs[c++] = new Vector2f(1,0);
                    p2 = vertices[v++] = new Vector3f(x1, yk1l1, z1);
                    uvs[c++] = new Vector2f(1,1);


                    // compute face normals using cross product
                    Vector3f v10 = new Vector3f();
                    Vector3f v20 = new Vector3f();

                    p1.sub(p0, v10);    // v10 = p1 - p0
                    p2.sub(p0, v20);    // v20 = p2 - p0
                    Vector3f fn = new Vector3f();
                    v10.cross(v20, fn);    // fn = v10 x v20;

                    faceNormals[nfn++] = fn;
                    faceNormals[nfn++] = fn;
                    faceNormals[nfn++] = fn;

                    // vertex normals point straight out

                    vertexNormals[nvn++] = new Vector3f(x1, 0, z).normalize();
                    vertexNormals[nvn++] = new Vector3f(x, 0, z1).normalize();
                    vertexNormals[nvn++] = new Vector3f(x1, 0, z1).normalize();

                }
            }
        }

        this.lightDir = new Vector4f();
        this.viewDir = new Vector4f();
        this.normalMatrix = new Matrix3f();
        createBarycentric();
        this.faceNormalBuffer = shader.createBuffer(this.faceNormals);
        this.vertexNormalBuffer = shader.createBuffer(this.vertexNormals);
        this.vertexBuffer = shader.createBuffer(this.vertices);
        this.barycentricBuffer = shader.createBuffer(this.barycentric);
        this.uvBuffer = shader.createBuffer(this.uvs);
    }


    private void createBarycentric() {
        this.barycentric = new Vector3f[vertices.length];

        Vector3f b0 = new Vector3f(1, 0, 0);
        Vector3f b1 = new Vector3f(0, 1, 0);
        Vector3f b2 = new Vector3f(0, 0, 1);

        for (int i = 0; i < vertices.length; i += 3) {
            barycentric[i] = b0;
            barycentric[i + 1] = b1;
            barycentric[i + 2] = b2;
        }
    }

    private float getZ(int l) {
        return 1f - 1.0f / depth - 2.0f / depth * l;
    }

    private float getX(int k) {
        return -1f + 0.5f * (2.0f / width) + 2.0f / width * k;
    }

    public void setViewDir(Vector4f viewDir) {
        this.viewDir.set(viewDir);
    }

    @Override
    protected void drawSelf(Shader shader) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // TODO: Complete this

        shader.setUniform("u_mvpMatrix", this.mvpMatrix);
        shader.setAttribute("a_position", this.vertexBuffer);
        if (shader.hasAttribute("a_barycentric")) {
            shader.setAttribute("a_barycentric", barycentricBuffer);
        }

        if (shader.hasAttribute("a_normal")) {
//            shader.setAttribute("a_normal", faceNormalBuffer);
            shader.setAttribute("a_normal", vertexNormalBuffer);
        }

        if (shader.hasUniform("u_diffuseTexture")) {
            shader.setUniform("u_diffuseTexture", 0);
        }

        if (shader.hasUniform("u_texture")) {
            shader.setUniform("u_texture", 0);
        }

        if (shader.hasAttribute("a_texcoord")){
            shader.setAttribute("a_texcoord", this.uvBuffer);
        }

        if (shader.hasUniform("u_normalMatrix")) {
            getWorldMatrix(this.worldMatrix);
            this.worldMatrix.normal(this.normalMatrix);
            shader.setUniform("u_normalMatrix", normalMatrix);
        }


        if (shader.hasUniform("u_diffuseMaterial")) {
            shader.setUniform("u_diffuseMaterial", this.colour);
        }


        if (shader.hasUniform("u_lightDir")) {
            shader.setUniform("u_lightDir", this.lightDir);
        }

        if (shader.hasUniform("u_viewDir")) {
            shader.setUniform("u_viewDir", this.viewDir);
        }

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, this.texture);

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, this.vertices.length);
    }

    public void setLightDir(Vector4f lightDir) {
        this.lightDir.set(lightDir);
    }
}
