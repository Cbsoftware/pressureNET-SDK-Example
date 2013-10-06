package ca.cumulonimbus.pressurenetsdkexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import ca.cumulonimbus.pressurenetsdk.CbService;

public class MainActivity extends Activity {
	
	private Button buttonStartService;
	private Button buttonCheckSettings;
	private Button buttonChangeSetting;
	private Button buttonGetRecentReadings;
	private Button buttonStopAutoSubmit;

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
				Intent intent = new Intent(getApplicationContext(), CbService.class);
				startService(intent);
			}
		});
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
