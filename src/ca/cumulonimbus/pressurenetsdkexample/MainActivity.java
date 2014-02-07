package ca.cumulonimbus.pressurenetsdkexample;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
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
import ca.cumulonimbus.pressurenetsdk.CbApiCall;
import ca.cumulonimbus.pressurenetsdk.CbObservation;
import ca.cumulonimbus.pressurenetsdk.CbService;
import ca.cumulonimbus.pressurenetsdk.CbSettingsHandler;

public class MainActivity extends Activity {

	private Button buttonStartService;
	private Button buttonCheckSettings;
	private Button buttonChangeSetting;
	private Button buttonGetRecentReadings;
	private Button buttonStartStream;
	private Button buttonStopStream;
	private Button buttonStopAutoSubmit;
	private Button buttonStopService;
	
		// SDK communication
	boolean mBound;
	private Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mService = null;
	CbSettingsHandler settings;
	Intent serviceIntent;

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
		buttonStartStream = (Button) findViewById(R.id.buttonStartStream);
		buttonStopStream = (Button) findViewById(R.id.buttonStopStream);
		buttonStopService = (Button) findViewById(R.id.buttonStopService);

		buttonStartService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("starting service");
				serviceIntent = new Intent(getApplicationContext(),
						CbService.class);
				startService(serviceIntent);
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
				if(settings!=null) {
					settings.setDataCollectionFrequency(1000*60*1);
					setSettings(settings);
					String message = "Changed to auto-submit 1 minute interval";
					System.out.println(message);
					Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Please load settings first", Toast.LENGTH_LONG).show();
				}
			}
		});

		buttonGetRecentReadings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CbApiCall api = new CbApiCall();
				api.setMinLat(-90);
				api.setMaxLat(90);
				api.setMinLon(-180);
				api.setMaxLon(180);
				api.setStartTime(0);
				api.setEndTime(System.currentTimeMillis());
				askForRecentReadings(api);
			}
		});
		
		buttonStartStream.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startStream(Sensor.TYPE_PRESSURE);
			}
		});

		buttonStopStream.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopStream(Sensor.TYPE_PRESSURE);
			}
		});
		
		buttonStopAutoSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopAutoSubmit();
			}
		});
		
		buttonStopService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("Unbinding, stopping service");
				unBindCbService();
				stopService(serviceIntent);
			}
		});
	}
	
	

	@Override
	protected void onStop() {
		unBindCbService();
		super.onStop();
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
			case CbService.MSG_LOCAL_RECENTS:
				ArrayList<CbObservation> obsList = (ArrayList<CbObservation>) msg.obj;
				String message = "Received " + obsList.size() + " readings from local database";
				System.out.println(message);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				break;
			case CbService.MSG_DATA_STREAM:
				CbObservation obs = (CbObservation) msg.obj;
				System.out.println("Received streaming value: " + obs.getObservationValue());
				break; 
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	/**
	 * Stop all the alarms from firing
	 */
	private void stopAutoSubmit() {
		if (mBound) {
			System.out.println("Stop auto-submit");

			Message msg = Message
					.obtain(null, CbService.MSG_STOP, 0, 0);
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
	
	/**
	 * Start streaming sensor data
	 */
	private void startStream(int sensor) {
		if (mBound) {
			System.out.println("Starting sensor stream");

			Message msg = Message
					.obtain(null, CbService.MSG_START_STREAM, sensor, 0);
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
	
	/**
	 * Stop streaming sensor data
	 */
	private void stopStream(int sensor) {
		if (mBound) {
			System.out.println("Stopping sensor stream");

			Message msg = Message
					.obtain(null, CbService.MSG_STOP_STREAM, sensor, 0);
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
	
	/**
	 * Get the observations from the Cb database
	 */
	private void askForRecentReadings(CbApiCall apiCall) {
		if (mBound) {
			System.out.println("Asking for observations");

			Message msg = Message
					.obtain(null, CbService.MSG_GET_LOCAL_RECENTS, apiCall );
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
	
	
	/**
	 * Save settings from the Cb database
	 */
	private void setSettings(CbSettingsHandler newSettings) {
		if (mBound) {
			System.out.println("Saving settings: " + newSettings);

			Message msg = Message
					.obtain(null, CbService.MSG_SET_SETTINGS, newSettings);
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
	
	/**
	 * Get the settings from the Cb database
	 */
	private void askForSettings() {
		if (mBound) {
			System.out.println("Asking for settings");

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

}
