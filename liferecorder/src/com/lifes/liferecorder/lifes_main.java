package com.lifes.liferecorder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class lifes_main extends Activity implements LocationListener {
	static int once_init = 0;
	static int change_count = 0;
	private boolean getService = false;
	AtomicBoolean isRunning=new AtomicBoolean(false);
	private TextView gps_text;
	private LocationManager lms;
	private String bestProvider = LocationManager.GPS_PROVIDER;
	int next_way = 3; // 1 : north, 0 : south
	Double last_latitude = 0.0;
	Double last_longitude = 0.0;
	Double target_longitude = 0.0;
	Double target_latitude = 0.0;
	Double next_longitude = 0.0;
	Double next_latitude = 0.0;
	Double last_gap = 0.0;
	int stations_count = 8;
	int curr_point = 0;
	private static int train_temp_index = 0;
	taiwan_trainstation tts = new taiwan_trainstation();

	public void create_notif_option()
	{
	  NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	  //設定當按下這個通知之後要執行的activity
	  Intent notifyIntent = new Intent(lifes_main.this,lifes_main.class);
	    notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
	  PendingIntent appIntent=PendingIntent.getActivity(lifes_main.this,0,
	    notifyIntent,0);
	  Notification notification = new Notification();

	  RemoteViews remoteView = new RemoteViews(this.getPackageName(),R.layout.notification);  
      remoteView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
      remoteView.setTextViewText(R.id.train_info , "Current Train's locate ..");  
      notification.contentView = remoteView; 
	  //設定出現在狀態列的圖示
	  //notification.icon=R.drawable.icon;
	  notification.icon=R.drawable.ic_launcher;
	  //顯示在狀態列的文字
	  notification.tickerText="notification on status bar.";
	  //會有通知預設的鈴聲、振動、light
	  notification.defaults=Notification.DEFAULT_LIGHTS;
	  //設定通知的標題、內容
	  notification.setLatestEventInfo(lifes_main.this, "Current Station", "Locating...",appIntent); 
	  //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	  //notification.setLatestEventInfo(lifes_main.this, "Current Station", "TEST", appIntent);
	  //送出Notification
	  notificationManager.notify(0,notification);
	}

	public void setPass_stations(int idx)
	{
		TextView pass_sta_txt = (TextView) findViewById(R.id.pass_sta);
		pass_sta_txt.setText(String.valueOf(tts.train_name[idx]));
		return;
	}

	public void setNext_stations(int idx)
	{
		TextView next_sta_txt = (TextView) findViewById(R.id.next_sta);
		next_sta_txt.setText(String.valueOf(tts.train_name[idx]));

		return;
	}

	public void setCurr_stations(int idx)
	{
		TextView curr_sta_txt = (TextView) findViewById(R.id.curr_sta);
		curr_sta_txt.setText(String.valueOf(tts.train_name[idx]));

		return;
	}

	private void getLocation(Location location) {	
	  if(location != null) {
		TextView longitude_txt = (TextView) findViewById(R.id.longitude);
		TextView latitude_txt = (TextView) findViewById(R.id.latitude);

		Double longitude = location.getLongitude();
		Double latitude = location.getLatitude();
		longitude_txt.setText(String.valueOf(longitude));
		latitude_txt.setText(String.valueOf(latitude));
      }
	  else {
		Toast.makeText(this, "", Toast.LENGTH_LONG).show();
      }
	}
	
    private void processLocation(Location location, Double tar_lati, Double tar_longi)
    {
    	TextView dir_txt = (TextView) findViewById(R.id.curr_dir);
 
		//search current station
    	int tmp_index = tts.searchCurr_station(location);
    	if (once_init == 0)
    	{
    	  lifes_main.train_temp_index = tmp_index;
    	  once_init = 1;
    	}
    	else
    	{
    	  // indicate current train station
    	  setCurr_stations(tmp_index);
          // initialize the current direction
    	  if (tmp_index > lifes_main.train_temp_index)
    	    next_way = 1; // north
    	  else if (tmp_index < lifes_main.train_temp_index)
    		next_way = 0; // south

  	      if (next_way == 1)
  	      {
  		    dir_txt.setText(String.valueOf("北上"));
  		    setNext_stations(tmp_index + 1);
  		    if (tmp_index > 0)
  		      setPass_stations(tmp_index - 1);
  	      }
  		  else if (next_way == 0)
  		  {
  		    dir_txt.setText(String.valueOf("南下"));
  		    setNext_stations(tmp_index-1);
  		    if (tmp_index < tts.train_stations.length )
    		  setPass_stations(tmp_index - 1);
  		  }
    	}
    }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// initialize the gps text content
		String format = s.format(new Date());
		gps_text = (TextView) findViewById(R.id.gps_buf);
		gps_text.setText( format+"" );


		LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		  getService = true;
		  locationServiceInitial();
		} else {
		  Toast.makeText(this, "", Toast.LENGTH_LONG).show();
		  startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}

	}

	private void locationServiceInitial() {
		lms = (LocationManager) getSystemService(LOCATION_SERVICE);	// initialize the gps service
		if (!lms.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			  Toast.makeText(this, "Please Turn on the GPS/WiFi locate function..", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(this, "Locating.", Toast.LENGTH_LONG).show();
			lms.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, lifes_main.this);
			//GPS provider
			lms.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, lifes_main.this);
		}
    	// update gps data
    	Criteria criteria = new Criteria();    	
    	criteria.setAccuracy(Criteria.ACCURACY_FINE);   
        // enable Altitude status
        criteria.setAltitudeRequired(true);  
        // enable Bearing status 
        criteria.setBearingRequired(true);  
        // enable CostAllow
        criteria.setCostAllowed(true);      
        // enable power save 
        criteria.setPowerRequirement(Criteria.POWER_LOW);  
        // enable speed monitor
        criteria.setSpeedRequired(true);
		bestProvider = lms.getBestProvider(criteria, true);		
		Location location = lms.getLastKnownLocation(bestProvider);
		getLocation(location);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(getService) {
			lms.requestLocationUpdates(bestProvider, 1000, 1, this);
		}

        //isRunning.set(true);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//isRunning.set(true);
	}
 
    @Override
    protected void onStop() {
      super.onStop();
		if(getService) {
		  lms.removeUpdates(this);
			//create_notif_option();
        }
		isRunning.set(false);
    }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(getService) {
			lms.removeUpdates(this);
        }
		isRunning.set(false);
		create_notif_option();
	}
	@Override
	public void onLocationChanged(Location location) {	// GPS location change..
		// TODO Auto-generated method stub
		TextView speed_txt = (TextView) findViewById(R.id.curr_spd);

		getLocation(location);

		// process the trains status and location 
		processLocation(location, target_latitude, target_longitude);

		// update current speed
		speed_txt.setText(String.valueOf(location.getSpeed()));
	}
 
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
 
	}
 
	@Override
	public void onProviderEnabled(String arg0) {
// TODO Auto-generated method stub
 
	}
 
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
	}
}

