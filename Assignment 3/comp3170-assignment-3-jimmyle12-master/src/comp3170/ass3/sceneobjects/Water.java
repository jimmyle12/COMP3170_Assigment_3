package comp3170.ass3.sceneobjects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import comp3170.SceneObject;
import comp3170.Shader;
import org.joml.Vector4f;

import java.awt.*;

public class Water extends SceneObject {
    private float[] vertices;
    private float[] normalVertices;
    private int normalVertexBuffer;
    private float[] colour = {0.0f, 0.0f, 0.9f};
    private Vector4f lightDir;
    private Vector4f viewDir;


    private int vertexBuffer;
    public Water(Shader shader, int width, int depth, float waterHeight) {
        super(shader);
        this.lightDir = new Vector4f();
        this.viewDir = new Vector4f();

        vertices = new float[]{
                1, waterHeight, 1,
                -1, waterHeight, -1,
                -1, waterHeight, 1,

                1, waterHeight, 1,
                1, waterHeight, -1,
                -1, waterHeight, -1,

        };

        normalVertices = new float[]{
                1, 0, 1,
                -1, 0, -1,
                -1, 0, 1,

                1, 0, 1,
                1, 0, -1,
                -1, 0, -1,
        };

        this.normalVertexBuffer = shader.createBuffer(normalVertices, GL4.GL_FLOAT_VEC3);
        this.vertexBuffer = shader.createBuffer(vertices, GL4.GL_FLOAT_VEC3);
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
        shader.setAttribute("a_position", this.normalVertexBuffer);

        // draw the triangle

        if (shader.hasUniform("a_colour")) {
            shader.setUniform("a_colour", this.colour);
        }

        if (shader.hasAttribute("a_normal")) {
            shader.setAttribute("a_normal", normalVertexBuffer);
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

        gl.glDrawArrays(GL.GL_TRIANGLES, 0,  vertices.length / 3);

    }
}
