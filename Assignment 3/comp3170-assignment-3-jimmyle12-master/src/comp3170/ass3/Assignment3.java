package comp3170.ass3;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import comp3170.ass3.sceneobjects.*;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import comp3170.GLException;
import comp3170.InputManager;
import comp3170.SceneObject;
import comp3170.Shader;

public class Assignment3 extends JFrame implements GLEventListener {

	// TAU = 2 * PI radians = 360 degrees
	final private float TAU = (float) (Math.PI * 2);

	private JSONObject level;
	private GLCanvas canvas;

	final private File TEXTURE_DIRECTORY = new File("src/comp3170/ass3/textures");
	final private String GRASS_TEXTURE = "grass.jpg";
	private int grassTexture;

	// shaders
	
	final private File SHADER_DIRECTORY = new File("src/comp3170/ass3/shaders");
	
	private Shader simpleShader;
	final private String SIMPLE_VERTEX_SHADER = "simpleVertex.glsl";
	final private String SIMPLE_FRAGMENT_SHADER = "simpleFragment.glsl";

	private Shader colourShader;
	final private String COLOUR_VERTEX_SHADER = "coloursVertex.glsl";
	final private String COLOUR_FRAGMENT_SHADER = "coloursFragment.glsl";

	// normal colouring
	private Shader normalShader;
	final private String NORMAL_VERTEX_SHADER = "normalVertex.glsl";
	final private String NORMAL_FRAGMENT_SHADER = "normalFragment.glsl";

	// diffuse lighting in fragment shader
	private Shader diffuseFragmentLightingShader;
	final private String DIFFUSE_FRAGMENT_LIGHTING_VERTEX_SHADER = "diffuseFragmentLightingVertex.glsl";
	final private String DIFFUSE_FRAGMENT_LIGHTING_FRAGMENT_SHADER = "diffuseFragmentLightingFragment.glsl";

	// specular lighting in fragement shader
	private Shader specularFragmentLightingShader;
	final private String SPECULAR_FRAGMENT_LIGHTING_VERTEX_SHADER = "specularFragmentLightingVertex.glsl";
	final private String SPECULAR_FRAGMENT_LIGHTING_FRAGMENT_SHADER = "specularFragmentLightingFragment.glsl";

	// texture shader
	private Shader textureShader;
	final private String TEXTURE_VERTEX_SHADER = "textureVertex.glsl";
	final private String TEXTURE_FRAGMENT_SHADER = "textureFragment.glsl";

	// phong shader
	private Shader phongShader;
	final private String PHONG_VERTEX_SHADER = "phongVertex.glsl";
	final private String PHONG_FRAGMENT_SHADER = "phongFragment.glsl";

	// matrices
	
	private Matrix4f mvpMatrix;
	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;
	private Matrix4f lightMatrix;

	// window size in pixels
	
	private int screenWidth = 1000;
	private int screenHeight = 1000;

	// Input Manager
	private InputManager input;
	private Animator animator;
	private long oldTime;

	// Scene objects
	private SceneObject root;
	private SceneObject camera;
	private SceneObject cameraPivot;
	private SceneObject lightPivot;
	private Light light;
	private Light light2;
	private HeightMap map;
	private Water water;

	public Assignment3(JSONObject level) {
		super(level.getString("name"));
		this.level = level;

		// Set up GL Canvas

		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setSampleBuffers(true);
		capabilities.setNumSamples(4);

		this.canvas = new GLCanvas(capabilities);
		this.canvas.addGLEventListener(this);
		this.add(canvas);

		// set up Animator
		this.animator = new Animator(canvas);
		this.animator.start();
		this.oldTime = System.currentTimeMillis();

		// Set up Input manager

		this.input = new InputManager();
		input.addListener(this);
		input.addListener(this.canvas);

		// Set up the JFrame

		this.setSize(screenWidth, screenHeight);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}

	@Override
	/**
	 * Initialise the GLCanvas
	 */
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Enable flags

		// enable depth testing and backface culling

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		// Load shaders


		this.simpleShader = loadShader(SIMPLE_VERTEX_SHADER, SIMPLE_FRAGMENT_SHADER);
		this.colourShader = loadShader(COLOUR_VERTEX_SHADER, COLOUR_FRAGMENT_SHADER);
		this.normalShader = loadShader(NORMAL_VERTEX_SHADER, NORMAL_FRAGMENT_SHADER);
		this.diffuseFragmentLightingShader = loadShader(DIFFUSE_FRAGMENT_LIGHTING_VERTEX_SHADER, DIFFUSE_FRAGMENT_LIGHTING_FRAGMENT_SHADER);
		this.specularFragmentLightingShader = loadShader(SPECULAR_FRAGMENT_LIGHTING_VERTEX_SHADER, SPECULAR_FRAGMENT_LIGHTING_FRAGMENT_SHADER);
		this.textureShader = loadShader(TEXTURE_VERTEX_SHADER, TEXTURE_FRAGMENT_SHADER);
		this.phongShader = loadShader(PHONG_VERTEX_SHADER, PHONG_FRAGMENT_SHADER);


		// Allocate matrices

		this.mvpMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
		this.lightMatrix = new Matrix4f();

		// Construct the scene-graph

		this.root = new SceneObject();

		// Example objects (should not be included in your final submission)

		Plane plane = new Plane(this.simpleShader, 10);
		plane.setParent(this.root);
		plane.localMatrix.scale(5,5,5);

		Axes axes = new Axes(this.colourShader);
		axes.setParent(this.root);
		axes.localMatrix.translate(0,0.5f,0);

		// Height map (incomplete)

		JSONObject jsonMap = this.level.getJSONObject("map");
		int width = jsonMap.getInt("width");
		int depth = jsonMap.getInt("depth");
		JSONArray heights = jsonMap.getJSONArray("height");

		grassTexture = loadTexture(GRASS_TEXTURE);
		map = new HeightMap(textureShader, width, depth, heights, grassTexture);
		map.setParent(this.root);
		map.localMatrix.scale(5,1,5);

		// Create water
		float waterHeight = this.level.getFloat("waterHeight");
		water = new Water(phongShader, width, depth, waterHeight);
		water.setParent(this.root);
		water.localMatrix.scale(5,1,5);



		this.cameraPivot = new SceneObject();
		this.cameraPivot.setParent(this.root);

		this.camera = new SceneObject();
		this.camera.setParent(this.cameraPivot);
		this.camera.localMatrix.translate(0, cameraHeight, cameraDistance);

		this.lightPivot = new SceneObject();
		this.lightPivot.setParent(this.root);

		this.light = new Light(simpleShader, new float[]{1.0f, 1.0f, 0.0f, 1.0f});
		this.light.setParent(this.lightPivot);
		this.light.localMatrix.translate(0, 0, lightDistance);
		this.light.localMatrix.scale(0.2f,0.2f,0.2f);

		this.light2 = new Light(simpleShader, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
		this.light2.setParent(this.lightPivot);
		this.light2.localMatrix.scale(0.1f,0.1f,0.1f);
	}

	/**
	 * Load and compile a vertex shader and fragment shader
	 *
	 * @param vs	The name of the vertex shader
	 * @param fs	The name of the fragment shader
	 * @return
	 */
	private Shader loadShader(String vs, String fs) {
		try {
			File vertexShader = new File(SHADER_DIRECTORY, vs);
			File fragmentShader = new File(SHADER_DIRECTORY, fs);
			return new Shader(vertexShader, fragmentShader);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Unreachable
		return null;
	}

	private int loadTexture(String textureFile) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		int textureID = 0;
		try {
			Texture tex = TextureIO.newTexture(new File(TEXTURE_DIRECTORY, textureFile), true);
			textureID = tex.getTextureObject();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);

		return textureID;
	}


	private final float CAMERA_TURN = TAU/8;
	private final float CAMERA_ZOOM = 1;
	private float cameraYaw = 0;
	private float cameraPitch = 0;
	private float cameraDistance = 10;
	private float cameraHeight = 1;
	private float moveForward = 0;

	//	private float cameraFOV = 10;
	private float cameraAspect = (float)screenWidth / screenHeight;
	private float cameraNear = 0.1f;
	private float cameraFar = 20.0f;

	float cameraFOV = TAU / 8;

	private Vector4f viewDir = new Vector4f();

	private float lightDistance = 5;
	private float lightYaw = -TAU/4;
	private float lightPitch = -TAU/8;
	private Vector4f lightDir = new Vector4f();


	public void update(float dt) {

		// rotate the camera

		if (this.input.isKeyDown(KeyEvent.VK_UP)) {
			this.cameraPitch -= CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_DOWN)) {
			this.cameraPitch += CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_LEFT)) {
			this.cameraYaw -= CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_RIGHT)) {
			this.cameraYaw += CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_I)) {
			this.cameraFOV -= 	CAMERA_ZOOM * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_O)) {
			this.cameraFOV += CAMERA_ZOOM * dt;
		}


		this.cameraPivot.localMatrix.identity();

		if (this.input.isKeyDown(KeyEvent.VK_SPACE) || input.isKeyDown(KeyEvent.VK_PLUS)) {
			moveForward += 0.1f;
		}
		if (this.input.isKeyDown(KeyEvent.VK_MINUS)) {
			moveForward -= 0.1f;
		}
		if (this.input.isKeyDown(KeyEvent.VK_ESCAPE)) {
			moveForward = 0f;
			cameraFOV = TAU / 8;
		}
		Vector4f moveDir = viewDir.normalize(new Vector4f());
		moveDir.mul(moveForward);
		this.cameraPivot.localMatrix.translate(moveDir.x, moveDir.y, moveDir.z);
		this.cameraPivot.localMatrix.rotateY(cameraYaw);
		this.cameraPivot.localMatrix.rotateX(cameraPitch);

		// rotate the light

		this.viewDir.set(0,0,-1,0);
		this.camera.getWorldMatrix(this.viewMatrix);
		this.viewDir.mul(this.viewMatrix);

		if (this.input.isKeyDown(KeyEvent.VK_W)) {
			this.lightPitch -= CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_S)) {
			this.lightPitch += CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_A)) {
			this.lightYaw -= CAMERA_TURN * dt;
		}

		if (this.input.isKeyDown(KeyEvent.VK_D)) {
			this.lightYaw += CAMERA_TURN * dt;
		}

		this.lightPivot.localMatrix.identity();
		this.lightPivot.localMatrix.rotateY(lightYaw);
		this.lightPivot.localMatrix.rotateX(lightPitch);
//
//		// calculate the light direction
//
		this.lightDir.set(0,0,1,0);
		this.light.getWorldMatrix(this.lightMatrix);
		this.lightDir.mul(this.lightMatrix);

		map.setLightDir(lightDir);
		map.setViewDir(viewDir);

		water.setLightDir(lightDir);
		water.setViewDir(viewDir);

		input.clear();
	}


	@Override
	/**
	 * Called when the canvas is redrawn
	 */
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// compute time since last frame
		long time = System.currentTimeMillis();
		float dt = (time - oldTime) / 1000.0f;
		oldTime = time;
		update(dt);

		gl.glViewport(0, 0, screenWidth, screenHeight);

		// set the background colour to black
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		// clear the depth buffer
		gl.glClearDepth(1f);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

		// set the view matrix
		this.camera.getWorldMatrix(this.viewMatrix);
		this.viewMatrix.invert();

		// set the projection matrix

//		float width = cameraAspect * cameraFOV;
		this.projectionMatrix.setPerspective(cameraFOV, cameraAspect, cameraNear, cameraFar);

		// multiply the matrices together
		this.mvpMatrix.identity();
		this.mvpMatrix.mul(this.projectionMatrix);
		this.mvpMatrix.mul(this.viewMatrix);

		// draw the objects in the scene graph recursively
		this.root.draw(mvpMatrix);
	}

	@Override
	/**
	 * Called when the canvas is resized
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
	}

	@Override
	/**
	 * Called when we dispose of the canvas
	 */
	public void dispose(GLAutoDrawable drawable) {
	}

	/**
	 * Main method expects a JSON level filename to be give as an argument.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws GLException
	 */
	public static void main(String[] args) throws IOException, GLException {
		File levelFile = new File(args[0]);
		BufferedReader in = new BufferedReader(new FileReader(levelFile));
		JSONTokener tokener = new JSONTokener(in);
		JSONObject level = new JSONObject(tokener);

		new Assignment3(level);
	}

}
