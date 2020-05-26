package comp3170.ass3.sceneobjects;

import org.joml.Vector3f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.SceneObject;
import comp3170.Shader;

public class Plane extends SceneObject {

	private Vector3f[] vertices;
	private int vertexBuffer;

	private float[] colour = { 0.5f, 0.5f, 0.5f, 1.0f }; // grey
	
	/**
	 * create a 2x2 square with the origin (0,0) at the centre
	 * and draw grid lines on it.
	 * 
	 * @param shader 	The shader to use to draw this
	 * @param nLines	The number of lines to draw
	 */
	
	public Plane(Shader shader, int nLines) {
		super(shader);
		
		vertices = new Vector3f[(nLines + 1) * 2 * 2];
		
		int j = 0;
		
		for (int i = 0; i <= nLines; i++) {
			
			float p = (float) 2 * i / nLines - 1; 
			
			// line parallel to the x axis
			vertices[j++] = new Vector3f(-1, 0, p);
			vertices[j++] = new Vector3f( 1, 0, p);

			// line parallel to the z axis
			vertices[j++] = new Vector3f(p, 0, -1);
			vertices[j++] = new Vector3f(p, 0,  1);
		}		
		
		this.vertexBuffer = shader.createBuffer(this.vertices);
	}

	@Override
	protected void drawSelf(Shader shader) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.setUniform("u_mvpMatrix", this.mvpMatrix);			
		shader.setAttribute("a_position", this.vertexBuffer);
		shader.setUniform("u_colour", this.colour);
		gl.glDrawArrays(GL.GL_LINES, 0, this.vertices.length);

	}

}
