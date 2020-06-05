package comp3170.ass3.sceneobjects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import comp3170.SceneObject;
import comp3170.Shader;
import org.joml.Matrix3f;
import org.joml.Vector4f;

import java.awt.*;

public class Water extends SceneObject {
    private float[] vertices;
    private float[] normalVertices;
    private int normalVertexBuffer;
    private float[] colour = { 0.2f, 0.2f, 1f, 0.5f};
    private int colourBuffer;
    private Vector4f lightDir;
    private Vector4f viewDir;
    private Matrix3f normalMatrix;
    private float specularity = 2;


    private int vertexBuffer;
    public Water(Shader shader, int width, int depth, float waterHeight) {
        super(shader);
        this.lightDir = new Vector4f();
        this.viewDir = new Vector4f();
        this.normalMatrix = new Matrix3f();

        vertices = new float[]{
                1, waterHeight, 1,
                -1, waterHeight, -1,
                -1, waterHeight, 1,

                1, waterHeight, 1,
                1, waterHeight, -1,
                -1, waterHeight, -1,

        };

        normalVertices = new float[]{
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,

                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
        };

        this.normalVertexBuffer = shader.createBuffer(normalVertices, GL4.GL_FLOAT_VEC3);
        this.vertexBuffer = shader.createBuffer(vertices, GL4.GL_FLOAT_VEC3);
        this.colourBuffer = shader.createBuffer(colour, GL4.GL_FLOAT_VEC4);
    }

    public void setLightDir(Vector4f lightDir) {
        this.lightDir.set(lightDir);
    }

    public void setViewDir(Vector4f viewDir) {
        this.viewDir.set(viewDir);
    }

    @Override
    protected void drawSelf(Shader shader) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        shader.setUniform("u_mvpMatrix", this.mvpMatrix);
        shader.setAttribute("a_position", this.vertexBuffer);

        // draw the triangle

        if (shader.hasAttribute("a_colour")) {
            shader.setAttribute("a_colour", this.colourBuffer);
        }

        if (shader.hasUniform("u_colour")) {
            shader.setUniform("u_colour", this.colour);
        }

        if (shader.hasAttribute("a_normal")) {
            shader.setAttribute("a_normal", normalVertexBuffer);
        }

        if (shader.hasUniform("u_normalMatrix")) {
            getWorldMatrix(this.worldMatrix);
            this.worldMatrix.normal(this.normalMatrix);
            shader.setUniform("u_normalMatrix", normalMatrix);
        }

        if (shader.hasUniform("u_worldMatrix")) {
            shader.setUniform("u_worldMatrix", worldMatrix);
        }

        if (shader.hasAttribute("a_normal")) {
            shader.setAttribute("a_normal", normalVertexBuffer);
        }

        if (shader.hasUniform("u_diffuseMaterial")) {
            shader.setUniform("u_diffuseMaterial", this.colour);
        }

        if (shader.hasUniform("u_specularMaterial")) {
            shader.setUniform("u_specularMaterial", this.colour);
        }

        if (shader.hasUniform("u_specularity")) {
            shader.setUniform("u_specularity", this.specularity);
        }

        if (shader.hasUniform("u_lightDir")) {
            shader.setUniform("u_lightDir", this.lightDir);
        }

        if (shader.hasUniform("u_viewDir")) {
            shader.setUniform("u_viewDir", this.viewDir);
        }

        gl.glDrawArrays(GL.GL_TRIANGLES, 0,  vertices.length / 3);

    }
}
