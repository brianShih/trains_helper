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
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

	Double[][] train_stations = {
			// latitude  , longitude
			{24.0253312, 120.5372467}, //0.花壇
			{24.082064,  120.53862},   //1.彰化
			{24.11454,   120.59014},   //2.成功
			{24.110192,  120.614451},  //3.新烏日			
			{24.109056,  120.622434},  //4.烏日
			{24.119084,  120.647947},  //5.大慶
			{24.137335,  120.68509},   //6.台中
			{24.167135,  120.700046}   //7.太原
    };


	public void create_notif_option()
	{
	  NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	  //設定當按下這個通知之後要執行的activity
	  Intent notifyIntent = new Intent(lifes_main.this,lifes_main.class);
	    notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
	  PendingIntent appIntent=PendingIntent.getActivity(lifes_main.this,0,
	    notifyIntent,0);
	  Notification notification = new Notification();
	  //設定出現在狀態列的圖示
	  //notification.icon=R.drawable.icon;
	  notification.icon=R.drawable.ic_launcher;
	  //顯示在狀態列的文字
	  notification.tickerText="notification on status bar.";
	  //會有通知預設的鈴聲、振動、light
	  notification.defaults=Notification.DEFAULT_ALL;
	  //設定通知的標題、內容
	  notification.setLatestEventInfo(lifes_main.this, "LifeRecorder", "TEST", appIntent);
	  //notification.setLatestEventInfo(lifes_main.this,"Title","content",appIntent);
	  //送出Notification
	  notificationManager.notify(0,notification);
	}

	public void the_Way(Location location)
	{
		TextView dir_txt = (TextView) findViewById(R.id.curr_dir);
		Double curr_lati = location.getLatitude();
		Double curr_longi = location.getLongitude();
		change_count++;
		if (change_count > 5)
		{
			change_count = 0;
			last_latitude = curr_lati;
			last_longitude = curr_longi;
		}
		if (change_count == 3)
		{
			Double lst_lati = last_latitude;
			Double lst_longi = last_longitude;
			if ((curr_lati > last_latitude && (curr_lati - lst_lati > 0.01)) ||
				(curr_longi > last_longitude && (curr_longi - lst_longi > 0.01))
			   )
			{
			  next_way = 1;
			}
			else if (curr_lati < last_latitude && (curr_lati - lst_lati > 0.01) ||
					(curr_longi < last_longitude && (curr_longi - lst_longi > 0.01))
			        )
			{
			  next_way = 0;
			}
		}

		if (next_way == 1)
		  dir_txt.setText(String.valueOf("北上"));
		else if (next_way == 0)
		  dir_txt.setText(String.valueOf("南下"));
	}
	
	public void searchCurr_station(Location loc)
	{
	  int i = 0;
	  for (i = 0; i < 8; i++)
	  {
		Double gap = getDistance(loc.getLatitude(), loc.getLongitude(), train_stations[i][0], train_stations[i][1]);
	    if (gap < 1000 && gap >= 0)
	      break;
	  }
	  setCurr_stations(i);
	  curr_point = i;
	}
	public void setPass_stations(int i)
	{
		TextView pass_sta_txt = (TextView) findViewById(R.id.pass_sta);
		switch (i)
		{
		case 0:
			pass_sta_txt.setText(String.valueOf("花壇火車站"));
		  break;
		case 1:
			pass_sta_txt.setText(String.valueOf("彰化火車站"));
			  break;
		case 2:
			pass_sta_txt.setText(String.valueOf("成功火車站"));
			  break;
		case 3:
			pass_sta_txt.setText(String.valueOf("新烏日火車站"));
			  break;
		case 4:
			pass_sta_txt.setText(String.valueOf("烏日火車站"));
			  break;
		case 5:
			pass_sta_txt.setText(String.valueOf("大慶火車站"));
			  break;
		case 6:
			pass_sta_txt.setText(String.valueOf("台中火車站"));
			  break;
		case 7:
			pass_sta_txt.setText(String.valueOf("太原火車站"));
			  break;
		}
		return;
	}

	public void setNext_stations(int i)
	{
		TextView next_sta_txt = (TextView) findViewById(R.id.next_sta);
		switch (i)
		{
		case 0:
			next_sta_txt.setText(String.valueOf("花壇火車站"));
		  break;
		case 1:
			next_sta_txt.setText(String.valueOf("彰化火車站"));
			  break;
		case 2:
			next_sta_txt.setText(String.valueOf("成功火車站"));
			  break;
		case 3:
			next_sta_txt.setText(String.valueOf("新烏日火車站"));
			  break;
		case 4:
			next_sta_txt.setText(String.valueOf("烏日火車站"));
			  break;
		case 5:
			next_sta_txt.setText(String.valueOf("大慶火車站"));
			  break;
		case 6:
			next_sta_txt.setText(String.valueOf("台中火車站"));
			  break;
		case 7:
			next_sta_txt.setText(String.valueOf("太原火車站"));
			  break;
		}
		return;
	}

	public void setCurr_stations(int i)
	{
		TextView curr_sta_txt = (TextView) findViewById(R.id.curr_sta);
		switch (i)
		{
		case 0:
		  curr_sta_txt.setText(String.valueOf("花壇火車站"));
		  break;
		case 1:
			  curr_sta_txt.setText(String.valueOf("彰化火車站"));
			  break;
		case 2:
			  curr_sta_txt.setText(String.valueOf("成功火車站"));
			  break;
		case 3:
			  curr_sta_txt.setText(String.valueOf("新烏日火車站"));
			  break;
		case 4:
			  curr_sta_txt.setText(String.valueOf("烏日火車站"));
			  break;
		case 5:
			  curr_sta_txt.setText(String.valueOf("大慶火車站"));
			  break;
		case 6:
			  curr_sta_txt.setText(String.valueOf("台中火車站"));
			  break;
		case 7:
			  curr_sta_txt.setText(String.valueOf("太原火車站"));
			  break;
		}
		return;
	}

	public double getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[3];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}
	
    public void target_distance(Location location)
    {
		TextView met_status_txt = (TextView) findViewById(R.id.met_status);
		TextView gap_status_txt = (TextView) findViewById(R.id.gap_status);
		Double gap = 0.0;
		Double longitude = location.getLongitude();
		Double latitude = location.getLatitude();
		Double tar_lati = target_latitude;
		Double tar_longi = target_longitude;
		if (once_init == 1)
		{
		  gap = getDistance(latitude, longitude, tar_lati, tar_longi);
		  if (next_way == 0 || next_way == 1)
		    gap_status_txt.setText(String.valueOf(gap));
		  if (gap <= 150 || gap == 0)
		  {
		    met_status_txt.setText(String.valueOf("抵達"));
		    if (curr_point < 8)
		      curr_point++;
	      }
		  else if (gap <= 1000 && gap > 800)
		  {
		    met_status_txt.setText(String.valueOf("接近"));
	      }
		  else if (gap < 500 && gap > 150)
		  {
	        met_status_txt.setText(String.valueOf("即將抵達"));
	      }
		}
    }
    private void processing(Location location, Double tar_lati, Double tar_longi)
    {
    	if (once_init == 1)
    	  target_distance(location);

		//search current station
		searchCurr_station(location);
		//search next station
		if (next_way == 1) // direction is north
		{
		  if (curr_point <= 8 && curr_point > 0)
			  setPass_stations(curr_point - 1);
		  setNext_stations(curr_point + 1);
		}
		else if (next_way == 0) // direction is south
		{
		  if (curr_point < 8 && curr_point >= 0)
		    setPass_stations(curr_point + 1);
		  setNext_stations(curr_point - 1);
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
			  Toast.makeText(this, "請開啓GPS/WIFI定位系統", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(this, "定位中..", Toast.LENGTH_LONG).show();
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
        //criteria.setPowerRequirement(Criteria.POWER_LOW);  
        // enable speed monitor
        criteria.setSpeedRequired(true);
		bestProvider = lms.getBestProvider(criteria, true);		
		Location location = lms.getLastKnownLocation(bestProvider);
		getLocation(location);
	}
	private void getLocation(Location location) {	
       if(location != null) {
			TextView longitude_txt = (TextView) findViewById(R.id.longitude);
			TextView latitude_txt = (TextView) findViewById(R.id.latitude);
 
			Double longitude = location.getLongitude();	//���蝬�漲
			Double latitude = location.getLatitude();	//���蝺臬漲
 
			longitude_txt.setText(String.valueOf(longitude));
			latitude_txt.setText(String.valueOf(latitude));
		}
		else {
			Toast.makeText(this, "", Toast.LENGTH_LONG).show();
		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(getService) {
			lms.requestLocationUpdates(bestProvider, 1000, 1, this);
		}

        isRunning.set(true);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		isRunning.set(true);
	}
 
    @Override
    protected void onStop() {
      super.onStop();
		if(getService) {
			//lms.removeUpdates(this);
			//create_notif_option();
        }
		isRunning.set(false);
    }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(getService) {
			//lms.removeUpdates(this);
			
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
	    processing(location, target_latitude, target_longitude);
		// find out the way of train's direction
		the_Way(location);
		// update current speed
		speed_txt.setText(String.valueOf(location.getSpeed()));
		if (next_way == 1 && once_init == 0)
		{
	      target_latitude = train_stations[7][0];
          target_longitude = train_stations[7][1];
          once_init = 1;
	    }
		else if (next_way == 0 && once_init == 0)
	    {
	      target_latitude = train_stations[0][0];
		  target_longitude = train_stations[0][1];
		  once_init = 1;
		}
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

