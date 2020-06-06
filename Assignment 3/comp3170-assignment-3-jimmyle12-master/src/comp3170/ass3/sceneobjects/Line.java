package comp3170.ass3.sceneobjects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import comp3170.SceneObject;
import comp3170.Shader;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Line extends SceneObject {
	public float[] vertices;

	private int vertexBuffer;

	private float[] colour = {
			1,1,1,1};

	public Line(Shader shader, Vector4f dest) {
		super(shader);

		this.setLightDir(dest);


	}


	@Override
	protected void drawSelf(Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		this.vertexBuffer = shader.createBuffer(this.vertices, GL4.GL_FLOAT_VEC3);


		shader.setUniform("u_mvpMatrix", this.mvpMatrix);
		shader.setAttribute("a_position", this.vertexBuffer);
		shader.setUniform("u_colour", this.colour);

		gl.glDrawArrays(GL.GL_LINES, 0, this.vertices.length);

	}

	public void setLightDir(Vector4f dest) {
		vertices = new float[] {
				0, 0, 0,
				dest.x, dest.y, dest.z
		};
	}
}
