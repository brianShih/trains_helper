package com.lifes.liferecorder;

import android.location.Location;

public class taiwan_trainstation {
	private int train_index = 0;
	Double[][] train_stations = {
			// latitude  , longitude
			{24.0253312, 120.5372467}, //0.���
			{24.082064,  120.53862},   //1.����
			{24.11454,   120.59014},   //2.���\
			{24.110192,  120.614451},  //3.�s�Q��			
			{24.109056,  120.622434},  //4.�Q��
			{24.119084,  120.647947},  //5.�j�y
			{24.137335,  120.68509},   //6.�x��
			{24.167135,  120.700046}   //7.�ӭ�
    };
	String[] train_name = {
			"��¤�����"
			,"���Ƥ�����"
			,"���\������"
			,"�s�Q�������"
			,"�Q�������"
			,"�j�y������"
			,"�x��������"
			,"�ӭ������"
	};
	

	public int getTrain_index()
	{
	  return this.train_index;
	}
	
	public void setTrain_index(int set_index)
	{
	  this.train_index = set_index;
	}

	public double getDistance(double lat1, double lon1, double lat2, double lon2) {
		float[] results = new float[3];
		Location.distanceBetween(lat1, lon1, lat2, lon2, results);
		return results[0];
	}

	public void the_Way(Location location)
	{
/*
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
		  dir_txt.setText(String.valueOf("�_�W"));
		else if (next_way == 0)
		  dir_txt.setText(String.valueOf("�n�U"));
     */
	}

    public void target_distance(Location location)
    {
    	/*
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
		    met_status_txt.setText(String.valueOf("��F"));
		    if (curr_point < 8)
		      curr_point++;
	      }
		  else if (gap <= 1000 && gap > 800)
		  {
		    met_status_txt.setText(String.valueOf("����"));
	      }
		  else if (gap < 500 && gap > 150)
		  {
	        met_status_txt.setText(String.valueOf("�Y�N��F"));
	      }
		}
		*/
    }

	public int searchCurr_station(Location loc)
	{
	  int i = 0;
	  for (i = 0; i < 8; i++)
	  {
		Double gap = getDistance(loc.getLatitude(), loc.getLongitude(), train_stations[i][0], train_stations[i][1]);
	    if (gap < 1000 && gap >= 0)
	      break;
	  }

	  return i;
	}
}
