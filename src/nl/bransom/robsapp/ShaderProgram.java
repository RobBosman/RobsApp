package nl.bransom.robsapp;

import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderProgram {

	private int programId;
	private List<Integer> shaderIds;

	public ShaderProgram() {
		programId = -1;
		shaderIds = new ArrayList<Integer>();
	}
	
	public int getProgramId() {
		createProgram();
		return programId;
	}

	public int addShader(int type, String shaderSrc) {
		if (programId != -1) {
			Log.e(getClass().getName(), "Don't add shaders after compiling the shader program.");
			return 0;
		}

		// Create the shader object
		int shader = GLES20.glCreateShader(type);
		if (shader == 0) {
			Log.e(getClass().getName(), "Error creating shader '" + type + "'.");
			return 0;
		}

		// Load the shader source and compile the shader
		GLES20.glShaderSource(shader, shaderSrc);
		GLES20.glCompileShader(shader);

		// Check the compile status
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(getClass().getName(), "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			return 0;
		}

		shaderIds.add(shader);
		return shader;
	}

	private void createProgram() {
		if (programId == -1) {
			programId = GLES20.glCreateProgram(); // create empty OpenGL Program
			for (int shaderId : shaderIds) {
				GLES20.glAttachShader(programId, shaderId); // add the vertex shader to program
			}
			GLES20.glLinkProgram(programId); // creates OpenGL program executables
			// Free up no longer needed shader resources
			for (int shaderId : shaderIds) {
				GLES20.glDeleteShader(shaderId); // add the vertex shader to program
			}
			// Check the link status
			int[] linked = new int[1];
			GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linked, 0);
			if (linked[0] == 0) {
				Log.e(getClass().getName(), "Error linking shader program: " + GLES20.glGetProgramInfoLog(programId));
				GLES20.glDeleteProgram(programId);
				programId = -1;
			}
		}
	}

	public int getAttribLocation(String locationName) {
		createProgram();
		int locationId = GLES20.glGetAttribLocation(programId, locationName);
		if (locationId == -1) {
			Log.e(getClass().getName(), "Cannot get GLES20.attribLocation of '" + locationName + "'.");
		}
		return locationId;
	}

	public int getUniformLocation(String locationName) {
		createProgram();
		int locationId = GLES20.glGetUniformLocation(programId, locationName);
		if (locationId == -1) {
			Log.e(getClass().getName(), "Cannot get GLES20.uniformLocation of '" + locationName + "'.");
		}
		return locationId;
	}
}