package nl.bransom.robsapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class RobsOpenGLESActivity extends Activity {

	private SensorManager mSensorManager;
	private Sensor accelerometerSensor;
	private Sensor magneticFieldSensor;
	private SensorEventListener sensorEventListener;
	private TextView glTextView;
	private RobsOpenGLESSurfaceView glSurfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.plane);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		glTextView = (TextView) findViewById(R.id.textView1);
		glTextView.setText("Gravity: \nMagnetic: ");

		glSurfaceView = (RobsOpenGLESSurfaceView) this.findViewById(R.id.robsOpenGLESSurfaceView);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorEventListener = new RobsOpenGLESSensorEventListener(glTextView, glSurfaceView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(sensorEventListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		glSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(sensorEventListener, accelerometerSensor);
		mSensorManager.unregisterListener(sensorEventListener, magneticFieldSensor);
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		glSurfaceView.onPause();
	}

	@Override
	public void onBackPressed() {
		System.exit(0);
	}
}