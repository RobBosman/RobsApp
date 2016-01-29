package nl.bransom.robsapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

public class RobsOpenGLESSensorEventListener implements SensorEventListener {

	private TextView glTextView;
	private GLSurfaceView glSurfaceView;
	private RobsOpenGLESRenderer mRenderer;

	public RobsOpenGLESSensorEventListener(TextView glTextView, RobsOpenGLESSurfaceView glSurfaceView) {
		this.glTextView = glTextView;
		this.glSurfaceView = glSurfaceView;
		this.mRenderer = glSurfaceView.getRenderer();
	}

	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
	}

	public final void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// mRenderer.mAngle += 10.0f;
			mRenderer.mAccX = event.values[0];
			mRenderer.mAccY = event.values[1];
			mRenderer.mAccZ = event.values[2];
			glSurfaceView.requestRender();
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mRenderer.mMagX = event.values[0];
			mRenderer.mMagY = event.values[1];
			mRenderer.mMagZ = event.values[2];
			glSurfaceView.requestRender();
		}

		glTextView.setText(String.format(
				"Gravity: [ %+.1f, %+.1f, %+.1f ]\nMagnet: [ %+03.0f, %+03.0f, %+03.0f ]", mRenderer.mAccX,
				mRenderer.mAccY, mRenderer.mAccZ, mRenderer.mMagX, mRenderer.mMagY, mRenderer.mMagZ));
	}
}