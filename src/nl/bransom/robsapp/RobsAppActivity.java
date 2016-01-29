package nl.bransom.robsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class RobsAppActivity extends Activity {

	private static final int DIALOG_EXIT_ID = 0;

	private Intent robsOpenGLESIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		robsOpenGLESIntent = new Intent(this, RobsOpenGLESActivity.class);
		setContentView(R.layout.main);
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_EXIT_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Wil je echt stoppen?");
			builder.setCancelable(false);
			builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					RobsAppActivity.this.selfDestruct(null);
				}
			});
			builder.setNegativeButton("Nee", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	public void onBackPressed() {
		showDialog(DIALOG_EXIT_ID);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void selfDestruct(View view) {
		Toast.makeText(getApplicationContext(), "Tot kijk!", Toast.LENGTH_SHORT).show();

		// Delayed exit.
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {
					// ignore
				}
				System.exit(0);
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_menu, menu);
		return true;
	}

	public void onGroupItemClick(MenuItem item) {
		// One of the group items (using the onClick attribute) was clicked
		// The item parameter passed here indicates which item it is
		// All other menu item clicks are handled by onOptionsItemSelected()
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opengles_test:
			startActivity(robsOpenGLESIntent);
			return true;
		case R.id.exit:
			selfDestruct(null);
			return true;
		default:
			return false;
		}
	}
}