package nl.bransom.robsapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class RobsOpenGLESRenderer implements GLSurfaceView.Renderer {

	public static int SIZEOF_FLOAT = Float.SIZE / 8;
	public static int SIZEOF_SHORT = Short.SIZE / 8;

	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int W = 3;
	public static final int X0 = 0;
	public static final int Y0 = 1;
	public static final int Z0 = 2;
	public static final int W0 = 3;
	public static final int X1 = 4;
	public static final int Y1 = 5;
	public static final int Z1 = 6;
	public static final int W1 = 7;
	public static final int X2 = 8;
	public static final int Y2 = 9;
	public static final int Z2 = 10;
	public static final int W2 = 11;
	public static final int X3 = 12;
	public static final int Y3 = 13;
	public static final int Z3 = 14;
	public static final int W3 = 15;

	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;
	private FloatBuffer colorBuffer;
	private ShaderProgram shaderProgram;
	private int maPositionHandle;
	private int maColorHandle;

	private List<float[]> matrixStack;
	private float[] resultMatrix;
	private float[] workMatrix;

	private int muMVPMatrixHandle;
	private float[] mViewMatrix;
	private float[] mProjMatrix;

	public float mAngleX;
	public float mAngleY;
	public float mAngleZ;
	public float mAccX;
	public float mAccY;
	public float mAccZ;
	public float mMagX;
	public float mMagY;
	public float mMagZ;

	public RobsOpenGLESRenderer() {
		matrixStack = new ArrayList<float[]>();
		resultMatrix = new float[16];
		workMatrix = new float[16];
		mViewMatrix = new float[16];
		mProjMatrix = new float[16];
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		// Enable depth-buffering.
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// initialize the vertex array and shaders
		initShapes();
		initShaders();
	}

	private float vertices[] = {
			// X, Y, Z
			0.0f, 0.0f, -0.8f, // tail bottom
			2.0f, 0.0f, 0.0f, // nose
			-0.1f, -0.1f, -0.2f, // right wing base
			-0.1f, 0.1f, -0.2f, // left wing base
			-0.5f, -0.8f, 0.0f, // right wing tip
			-0.5f, 0.8f, 0.0f, // left wing tip
	};
	private float colors[] = {
			// R, G, B
			0.0f, 0.0f, 0.0f, // black
			1.0f, 1.0f, 1.0f, // white
			1.0f, 1.0f, 1.0f, // white
			1.0f, 1.0f, 1.0f, // white
			0.0f, 1.0f, 0.0f, // green
			1.0f, 0.0f, 0.0f, // red
	};
	private short indices[] = {
			// counter clockwise
			2, 1, 0, // right side
			3, 0, 1, // left side
			2, 4, 1, // right wing
			5, 1, 3, // left wing
	};

	private void initShapes() {
		// initialize vertex Buffer for triangle (# of coordinate values * 4 bytes per float)
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * SIZEOF_FLOAT);
		vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
		vertexBuffer = vbb.asFloatBuffer(); // create a floating point buffer from the ByteBuffer
		vertexBuffer.put(vertices); // add the coordinates to the FloatBuffer
		vertexBuffer.position(0); // set the buffer to read the first coordinate

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * SIZEOF_SHORT);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);

		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * SIZEOF_FLOAT);
		cbb.order(ByteOrder.nativeOrder());
		colorBuffer = cbb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);
	}

	private static final String VERTEX_SHADER_SRC = "" //
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			// the matrix must be included as a modifier of gl_Position
			+ "uniform mat4 uMVPMatrix;\n" //
			+ "attribute vec4 vPosition;\n" //
			+ "attribute vec4 vColor;\n" //
			+ "varying vec4 v_vColor;\n" //
			+ "void main() {\n" //
			+ "  gl_Position = uMVPMatrix * vPosition;\n" //
			+ "  v_vColor = vColor;\n" //
			+ "}";
	private static final String FRAGMENT_SHADER_SRC = "" //
			+ "precision mediump float;\n" //
			+ "varying vec4 v_vColor;\n" //
			+ "void main() {\n" //
			+ "  gl_FragColor = v_vColor;\n" //
			+ "}";

	private void initShaders() {
		shaderProgram = new ShaderProgram();
		shaderProgram.addShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_SRC);
		shaderProgram.addShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SRC);
		maPositionHandle = shaderProgram.getAttribLocation("vPosition");
		maColorHandle = shaderProgram.getAttribLocation("vColor");
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;
		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f, 8.0f);

		// reference the uMVPMatrix shader matrix variable
		muMVPMatrixHandle = shaderProgram.getUniformLocation("uMVPMatrix");
		// define a camera view matrix.
		Matrix.setLookAtM(mViewMatrix, 0, //
				0.0f, 0.0f, 3.0f, // eye
				0.0f, 0.0f, 0.0f, // center
				0.0f, 1.0f, 0.0f // WorldUp
		);
	}

	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// Add program to OpenGL environment
		GLES20.glUseProgram(shaderProgram.getProgramId());

		// Prepare the triangle data
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maColorHandle, 3, GLES20.GL_FLOAT, false, 0, colorBuffer);
		GLES20.glEnableVertexAttribArray(maColorHandle);

		// Apply all matrices in the correct order.
		matrixStack.clear();
		matrixStack.add(mProjMatrix);
		matrixStack.add(mViewMatrix);
		matrixStack.add(getSpaceMatrix(mMagX, mMagY, mMagZ, mAccX, mAccY, mAccZ));
		matrixStack.add(getTouchMatrix(mAngleX, mAngleY, mAngleZ));

		Matrix.setIdentityM(resultMatrix, 0);
		for (float[] stackedMatrix : matrixStack) {
			Matrix.multiplyMM(workMatrix, 0, resultMatrix, 0, stackedMatrix, 0);
			for (int i = 0; i < resultMatrix.length; i++) {
				resultMatrix[i] = workMatrix[i];
			}
		}
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, resultMatrix, 0);

		// Draw the triangles
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
	}

	private static float[] touchMatrixX = new float[16];
	private static float[] touchMatrixY = new float[16];
	private static float[] touchMatrix = new float[16];

	private static float[] getTouchMatrix(float angleX, float angleY, float angleZ) {
		Matrix.setRotateM(touchMatrixX, 0, angleX, 1.0f, 0.0f, 0.0f);
		Matrix.setRotateM(touchMatrixY, 0, angleY, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(touchMatrix, 0, touchMatrixX, 0, touchMatrixY, 0);
		return touchMatrix;
	}

	private static float[] spaceMatrix = new float[16];
	private static float[] vectorP = new float[3];
	private static float[] vectorN = new float[3];

	private static float[] getSpaceMatrix(float magX, float magY, float magZ, float accX, float accY, float accZ) {
		// Let vecotor M be the vector of the magneticfield (magX, magY, magZ).
		// Let vecotor A be the vector of acceleration (accX, accY, accZ).
		// Let vecotor P be perpendicular to A and M so that P = M x A.
		// Let vector N ("north") be perpendicular to both A and P.
		// The resulting matrix will rotate the coordinate system so that X => N, Y => P and Z => A.

		// Compute P = M x A
		// P = [ My*Az - Mz*Ay, Mz*Ax - Mx*Az, Mx*Ay - My*Ax ]
		vectorP[X] = magY * accZ - magZ * accY;
		vectorP[Y] = magZ * accX - magX * accZ;
		vectorP[Z] = magX * accY - magY * accX;

		// Compute N = P x A
		// N = [ Py*Az - Pz*Ay, Pz*Ax - Px*Az, Px*Ay - Py*Ax ]
		vectorN[X] = vectorP[Y] * accZ - vectorP[Z] * accY;
		vectorN[Y] = vectorP[Z] * accX - vectorP[X] * accZ;
		vectorN[Z] = vectorP[X] * accY - vectorP[Y] * accX;

		Matrix.setIdentityM(spaceMatrix, 0);

		// The top row of rotation matrix is equal to the projection of the original X-axis,
		// which is the normalized vector N.
		float length = Matrix.length(vectorN[X], vectorN[Y], vectorN[Z]);
		spaceMatrix[X0] = vectorN[X] / length;
		spaceMatrix[Y0] = vectorN[Y] / length;
		spaceMatrix[Z0] = vectorN[Z] / length;

		// The middle row is equal to the projection of the original Y-axis,
		// which is the normalized vector P.
		length = Matrix.length(vectorP[X], vectorP[Y], vectorP[Z]);
		spaceMatrix[X1] = vectorP[X] / length;
		spaceMatrix[Y1] = vectorP[Y] / length;
		spaceMatrix[Z1] = vectorP[Z] / length;

		// The bottom row is equal to the projection of the original Z-axis,
		// which is the normalized vector A.
		length = Matrix.length(accX, accY, accZ);
		spaceMatrix[X2] = accX / length;
		spaceMatrix[Y2] = accY / length;
		spaceMatrix[Z2] = accZ / length;

		return spaceMatrix;
	}
}