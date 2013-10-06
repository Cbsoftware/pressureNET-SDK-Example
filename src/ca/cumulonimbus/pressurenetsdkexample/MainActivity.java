package ca.cumulonimbus.pressurenetsdkexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import ca.cumulonimbus.pressurenetsdk.CbService;
import ca.cumulonimbus.pressurenetsdk.CbSettingsHandler;

public class MainActivity extends Activity {

	private Button buttonStartService;
	private Button buttonCheckSettings;
	private Button buttonChangeSetting;
	private Button buttonGetRecentReadings;
	private Button buttonStopAutoSubmit;

	// pressureNET 4.0
	// SDK communication
	boolean mBound;
	private Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mService = null;
	CbSettingsHandler settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setClickListeners();
	}

	private void setClickListeners() {
		buttonStartService = (Button) findViewById(R.id.buttonStartService);
		buttonCheckSettings = (Button) findViewById(R.id.buttonCheckSettings);
		buttonChangeSetting = (Button) findViewById(R.id.buttonChangeSetting);
		buttonGetRecentReadings = (Button) findViewById(R.id.buttonGetRecentReadings);
		buttonStopAutoSubmit = (Button) findViewById(R.id.buttonStopAutoSubmit);

		buttonStartService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("starting service");
				Intent intent = new Intent(getApplicationContext(),
						CbService.class);
				startService(intent);
				bindCbService();
			}
		});

		buttonCheckSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				askForSettings();
			}
		});

		buttonChangeSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		buttonGetRecentReadings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		buttonStopAutoSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

	}

	public void unBindCbService() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	public void bindCbService() {
		System.out.println("Binding to CbService");
		if(!mBound) {
			bindService(new Intent(getApplicationContext(), CbService.class),
					mConnection, Context.BIND_AUTO_CREATE);
			
		}
	}
	
	/**
	 * Communicate with CbService
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			System.out.println("Service connected");
			mService = new Messenger(service);
			mBound = true;
			Message msg = Message.obtain(null, CbService.MSG_OKAY);
			
		}
		
		
		public void onServiceDisconnected(ComponentName className) {
			System.out.println("Service disconnected");
			mMessenger = null;
			mBound = false;
		}
	};
	
	/**
	 * Handle incoming communication from CbService. Listen for messages
	 * and act when they're received, sometimes responding with answers.
	 *
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CbService.MSG_SETTINGS:
				settings = (CbSettingsHandler) msg.obj;
				if (settings != null) {
					System.out.println("Settings:\n" + settings);
					Toast.makeText(getApplicationContext(), "Settings:\n" + settings, Toast.LENGTH_LONG).show();
				} else {
					System.out.println("Error: Settings null");
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	/**
	 * Get the settings from the Cb database
	 */
	private void askForSettings() {
		if (mBound) {
			System.out.println("asking for settings");

			Message msg = Message
					.obtain(null, CbService.MSG_GET_SETTINGS, 0, 0);
			try {
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				System.out.println("Remote exception: " + e.getMessage());
			}
		} else {
			System.out.println("Error: not bound to service");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
