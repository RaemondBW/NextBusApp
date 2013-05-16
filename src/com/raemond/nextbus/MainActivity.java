package com.raemond.nextbus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.raemond.nextbus.R;
import com.raemond.nextbus.Bus_Stop;

import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static MixpanelAPI mMixpanel;
	DataBaseHelper myDbHelper;
	static SQLiteDatabase stopDatabase;
	Dialog dialog;
	RefreshTimer refreshTimer;
	static ArrayList <Bus_Stop> stops = new ArrayList<Bus_Stop>();
	static Typeface robotoCond;
	LinearLayout linearLayout;
	addNewStopPopUp stopadder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		robotoCond = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mMixpanel = MixpanelAPI.getInstance(this, "1b833c37d8c3a3e826bae69f207556ce");
		
		/*ActionBar actionBar = getActionBar();
		actionBar.show();*/
		linearLayout = (LinearLayout)findViewById(R.id.ListOfStops);
		
		//load the popup window but don't show
		stopadder = new addNewStopPopUp(this, linearLayout);
		
		//enable a 10 MiB cache for the http gets
		try {
			File httpCacheDir = new File(this.getCacheDir(), "http");
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			HttpResponseCache.install(httpCacheDir, httpCacheSize);
		}
		catch (IOException e) {
			Log.i("onCreate", "HTTP response cache installation failed:" + e);
		}

		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		if (firstrun){

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Welcome!");
			builder.setMessage("Congratulations on installing nextBus! To start using the application just hit the \"get started\" button and add your first bus stop!");
			builder.setPositiveButton("get started", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					stopadder.showDialog();
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();

			getSharedPreferences("PREFERENCE", MODE_PRIVATE)
			.edit()
			.putBoolean("firstrun", false)
			.commit();
		}
	}
	
	
	protected void onStart() {
	  	super.onStart();
	  	myDbHelper = new DataBaseHelper(this);
		stopDatabase = myDbHelper.getWritableDatabase();
		
	  	if (stops.isEmpty()) {
	  		restoreCards();
	  	} else {
	  		for (Bus_Stop elem : stops) {
	  			elem.currentFrame.setVisibility(View.GONE);
	  			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
				linearLayout.addView(temp,0);
	  			elem.reAddFrame(temp);
	  		}
	  	}
	  	for (Bus_Stop stop: stops) {
			stop.refreshStop();
		}
	  	final Button button = (Button) findViewById(R.id.add_busStop);
        button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopadder.showDialog();
			}
        	
        });
	  	
	  	refreshTimer = new RefreshTimer();
	  	Timer t = new Timer();
	  	t.schedule(refreshTimer, 30000, 30000);
	}
	
	
	protected void onPause() {
		super.onPause();
		refreshTimer.cancel();
		for (Bus_Stop stop : stops) {
			if (!checkDuplicateInDatabase(stop)) {
				ContentValues cv = new ContentValues();
			    cv.put("agency_tag", stop.agency);
			    cv.put("agency_formal", stop.formalAgency);
			    cv.put("route", stop.route);
			    cv.put("route_formal", stop.formalRoute);
			    cv.put("stop_id", stop.stop);
			    cv.put("stop_formal", stop.formalStop);
			    stopDatabase.insert("saved_stops", null, cv);
			}
		}
	}
	
	
	protected void onStop() {
		super.onStop();
		mMixpanel.flush();
		stopDatabase.close();
		myDbHelper.close();
		HttpResponseCache cache = HttpResponseCache.getInstalled();
		if (cache != null) {
			cache.flush();
		}
	}
	
	
	@Override
  	public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()) {
    	case R.id.item_refresh:
    		makeToast("Refreshing...");
    		for (Bus_Stop stop: stops) {
    			stop.refreshStop();
    		}
    		break;
    	case R.id.item_new:
    		stopadder.showDialog();
    		break;
    	}
  		return true;
  	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
       
        return true;
    }
	
	
	public boolean checkDuplicateInDatabase(Bus_Stop insertAttempt) {
		//Log.v("checkDuplicate", "SELECT _ID FROM saved_stops WHERE stop_id = " + insertAttempt.stop);
		return stopDatabase.rawQuery("SELECT _ID FROM saved_stops WHERE stop_id = " + insertAttempt.stop, null).moveToFirst();
		//return true;
	}
	
	
	public static void removeCard(Bus_Stop item) {
		item.currentFrame.setVisibility(View.GONE);
		int index = stops.indexOf(item);
		if (index != -1) {
			stopDatabase.delete("saved_stops", "stop_id" + "=" + item.stop, null);
			stops.remove(index);
		}
	}
	
	
	public void restoreCards() {
		String[] columns = {"_ID", "agency_tag", "agency_formal", "route", "route_formal", "stop_id", "stop_formal"};
		Cursor cursor = stopDatabase.query("saved_stops", columns, null, null, null, null, null);
	    int agencyTag = cursor.getColumnIndex("agency_tag");
	    int agencyFormal = cursor.getColumnIndex("agency_formal");
	    int route = cursor.getColumnIndex("route");
	    int routeFormal = cursor.getColumnIndex("route_formal");
	    int stopID = cursor.getColumnIndex("stop_id");
	    int stopFormal = cursor.getColumnIndex("stop_formal");

	    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout linearLayout = (LinearLayout)findViewById(R.id.ListOfStops);
			FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
			linearLayout.addView(temp,0);
			
			Bus_Stop new_stop = new Bus_Stop(cursor.getString(agencyTag), cursor.getString(agencyFormal), cursor.getString(route),
					cursor.getString(routeFormal), cursor.getString(stopID), cursor.getString(stopFormal), temp, this);
			
			stops.add(new_stop);
	    }
	}
	
    
    public void makeToast(String message) {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
