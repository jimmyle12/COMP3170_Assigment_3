package comp3170;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SceneObject {

	private Shader shader;
	
	public Matrix4f localMatrix;
	protected Matrix4f worldMatrix;
	protected Matrix4f mvpMatrix;
	
	private List<SceneObject> children;
	private SceneObject parent;


	/**
	 * Shader-less constructor for invisible pivot points
	 */
	public SceneObject() {
		// Shader is null. For use in empty objects only
		this(null);
	}
	
	protected SceneObject(Shader shader) {
		if (shader != null) {
			shader.enable();			
		}

		this.shader = shader;
		this.localMatrix = new Matrix4f();
		this.localMatrix.identity();

		this.worldMatrix = new Matrix4f();
		this.mvpMatrix = new Matrix4f();
			
		this.parent = null;
		this.children = new ArrayList<SceneObject>();
	}
	
	/**
	 * Set the parent of this object in the scene graph
	 * 
	 * @param parent
	 */
	
	public void setParent(SceneObject parent) {
		if (this.parent != null) {
			this.parent.children.remove(this);
		}
		
		this.parent = parent;
		
		if (this.parent != null) {
			this.parent.children.add(this);
		}
	}
	
	/**
	 * Get the global transformation matrix for this object.
	 * 
	 * Note: you must pre-allocate a matrix in which to store the result
	 * 
	 * @param matrix	The matrix in which to store the result
	 * @return
	 */
	public Matrix4f getWorldMatrix(Matrix4f matrix) {
		localMatrix.get(matrix);
		
		SceneObject obj = this.parent;
		
		while (obj != null) {
			matrix.mulLocal(obj.localMatrix);
			obj = obj.parent;
		}
		return matrix;
	}
	
	/**
	 * Write the position of this object into the position vector given.
	 * 
	 * Note: you must pre-allocate a Vector4f in which to store the result
	 * 
	 * @param position	The vector in which to store the result
	 * @return
	 */
	public Vector4f getPosition(Vector4f position) {
		getWorldMatrix(this.worldMatrix);
		position.set(0,0,0,1);
		position.mul(this.worldMatrix);		
		
		return position;		
	}
	
	/**
	 * Draw this object. 
	 * Override this to draw specific objects.
	 * 
	 * @param shader
	 */
	protected void drawSelf(Shader shader) {
		// do nothing
	}

	/**
	 * Recursively draw the object and all its children.
	 * 
	 * The Model-View-Projection matrix is the product of
	 * the world matrix, the view matrix and the projection matrix.
	 * 
	 * i.e.
	 * mvpMatrix = projectionMatrix * viewMatrix * worldMatrix
	 *
	 * @param shader		The shader with which to draw
	 * @param parentMatrix	The parent's Model-View-Projection matrix
	 */
	public void draw(Matrix4f parentMatrix) {
		
		// copy the parent matrix
		parentMatrix.get(this.mvpMatrix);
		
		// multiply it by the local matrix
		this.mvpMatrix.mul(this.localMatrix);
		
		// draw this object, if it has a shader
		if (this.shader != null) {
			this.shader.enable();
			drawSelf(this.shader);
		}
			
		// draw the children 
		for (SceneObject child : children) {
			child.draw(mvpMatrix);
		}
		
	}
}
