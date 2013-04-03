package com.raemond.nextbus;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.raemond.nextbus.R;
import com.raemond.nextbus.Bus_Stop;

import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	DataBaseHelper myDbHelper;
	static SQLiteDatabase stopDatabase;
	Dialog dialog;
	RefreshTimer refreshTimer;
	static ArrayList <Bus_Stop> stops = new ArrayList<Bus_Stop>();
	static Typeface robotoCond;
	LinearLayout linearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		robotoCond = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		ActionBar actionBar = getActionBar();
		actionBar.show();
		linearLayout = (LinearLayout)findViewById(R.id.listOfStops);

		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		if (firstrun){

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Welcome!");
			builder.setMessage("Congratulations on installing nextBus! To start using the application just hit the \"get started\" button and add your first bus stop!");
			builder.setPositiveButton("get started", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					new addNewStopPopUp(MainActivity.this,linearLayout);
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
				linearLayout.addView(temp);
	  			elem.reAddFrame(temp);
	  		}
	  	}
	  	for (Bus_Stop stop: stops) {
			stop.refreshStop();
		}
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
		stopDatabase.close();
		myDbHelper.close();
	}
	
	
	public boolean checkDuplicateInDatabase(Bus_Stop insertAttempt) {
		return stopDatabase.rawQuery("SELECT _ID FROM saved_stops WHERE stop_id = " + insertAttempt.stop, null).moveToFirst();
	}
	
	
	class RetrievePrediction extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls){
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				url = new URL(urls[0]);
				connection = url.openConnection();
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(connection.getInputStream());
				
				NodeList nList = doc.getElementsByTagName("prediction");
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						return eElement.getAttribute("minutes");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "None";
		}

		protected void onPostExecute(String result) {
			TextView estimation = (TextView) findViewById(R.id.time);
			estimation.setText(result);
		}
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
       
        return true;
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
			LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
			FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
			linearLayout.addView(temp);
			
			Bus_Stop new_stop = new Bus_Stop(cursor.getString(agencyTag), cursor.getString(agencyFormal), cursor.getString(route),
					cursor.getString(routeFormal), cursor.getString(stopID), cursor.getString(stopFormal), temp, this);
			stops.add(new_stop);
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
    		new addNewStopPopUp(this,linearLayout);
    		break;
    	}
  		return true;
  	}
    
    
    public void makeToast(String message) {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
