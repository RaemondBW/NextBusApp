package com.raemond.nextbus;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import android.R;
import com.raemond.nextbus.R;
import com.raemond.nextbus.Bus_Stop;


import android.net.http.AndroidHttpClient;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	DataBaseHelper myDbHelper;
	static SQLiteDatabase stopDatabase;
	Dialog dialog;
	RefreshTimer refreshTimer;
	
	Spinner agencyspinner;
	Spinner routespinner;
	Spinner directionspinner;
	Spinner stopspinner;
	static ArrayList <Bus_Stop> stops = new ArrayList<Bus_Stop>();
	String agency;
	String route;
	String direction;
	String stop;
	static HashMap<String,String> agencymap;
	static HashMap<String,String> directionmap;
	static HashMap<String,String> routemap;
	static HashMap<String,String> stopmap;
	static ArrayList <String> agencies = new ArrayList<String>();
	static ArrayList <String> routes = new ArrayList<String>();
	static ArrayList <String> directions = new ArrayList<String>();
	static ArrayList <String> stopList = new ArrayList<String>();
	static Typeface robotoCond;
	Document doc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		robotoCond = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		// gets the activity's default ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.show();

		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		if (firstrun){

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setTitle("Welcome!");
			builder.setMessage("Congratulations on installing nextBus! To start using the application just hit the \"get started\" button and add your first bus stop!");
			builder.setPositiveButton("get started", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					createNewStopPopup();

				}
			});
			// create alert dialog
			AlertDialog alertDialog = builder.create();
			// show it
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
	  		Log.v("stops","empty");
	  		restoreCards();
	  	} else {
	  		Log.v("else", "called");
	  		for (Bus_Stop elem : stops) {
	  			elem.currentFrame.setVisibility(View.GONE);
	  			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
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
			    Log.v("INSERTED INTO", "database");
			}
		}
	}
	
	protected void onStop() {
		super.onStop();
		stopDatabase.close();
		myDbHelper.close();
	}
	
	public boolean checkDuplicateInDatabase(Bus_Stop insertAttempt) {
		//if (insertAttempt.stop)
		return stopDatabase.rawQuery("SELECT _ID FROM saved_stops WHERE stop_id = " + insertAttempt.stop, null).moveToFirst();
	}
		
	class RetrievePrediction extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls){
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			Log.v("function","called");
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
			Log.v("Bus Prediction",result);
			TextView estimation = (TextView) findViewById(R.id.time);
			estimation.setText(result);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// use an inflater to populate the ActionBar with items
		Log.v("onCreate","OptionsMenu");
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
	
	
	public void createNewStopPopup() {
		agencies.clear();
		routes.clear();
		directions.clear();
		stopList.clear();
		agency = "";
		route = "";
		direction = "";
		stop = "";
		//create the list of agencies
		AsyncTask<String, Void, String[]> task = new RetrieveAgencies().execute(" ");
		try {
			task.get(1000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dialog = new Dialog((Context) this);
		dialog.setContentView(R.layout.activity_main);
		dialog.setTitle("pick your stop:");

		// set the custom dialog components - text, image and button
		agencyspinner = (Spinner) dialog.findViewById(R.id.agencySpinner);
		ArrayAdapter<String> agencyArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,agencies);
		agencyspinner.setAdapter(agencyArrayAdapter);
		agencyspinner.setOnItemSelectedListener(new agencylistener());

		Button dialogButton = (Button) dialog.findViewById(R.id.addBusStop);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!agency.isEmpty() && !route.isEmpty() && !stop.isEmpty()) {
					final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
					FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
					linearLayout.addView(temp);
					
					Bus_Stop new_stop = new Bus_Stop(agencymap.get(agency), agency, routemap.get(route), route, stopmap.get(stop), stop, temp, MainActivity.this);
					stops.add(new_stop);
					dialog.dismiss();
				}
			}
		});

		dialog.show();
	}
    
    @Override
  	public boolean onOptionsItemSelected(MenuItem item){
    	// same as using a normal menu
    	switch(item.getItemId()) {
    	case R.id.item_refresh:
    		makeToast("Refreshing...");
    		for (Bus_Stop stop: stops) {
    			stop.refreshStop();
    		}
    		break;
    	case R.id.item_new:
    		createNewStopPopup();
    		break;
    	}
  		return true;
  	}
    
    public void makeToast(String message) {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
	
	
    class RetrieveAgencies extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			
			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			MainActivity.agencymap = new HashMap<String,String>();
			try {
				url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList");
				HttpUriRequest request = url;
				request.addHeader("Accept-Encoding", "gzip, deflate");
				DocumentBuilder builder = factory.newDocumentBuilder();
				HttpResponse connection = client.execute(url);//url.openConnection();
				InputStream instream = connection.getEntity().getContent();
				Header contentEncoding = connection.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					Log.v("Retrieve Agencies","gzipped");
				    instream = new GZIPInputStream(instream);
				}
				Document doc = builder.parse(instream);

				
				NodeList nList = doc.getElementsByTagName("agency");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						//Log.v(eElement.getAttribute("title"),eElement.getAttribute("tag"));
						agencies.add(eElement.getAttribute("title"));
						MainActivity.agencymap.put(eElement.getAttribute("title"),eElement.getAttribute("tag"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			client.close();
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Do nothing
		}

	}
	
	
    class RetrieveRoutes extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			
			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			MainActivity.routemap = new HashMap<String,String>();
			try {
				Log.v("agency tag",agencymap.get(agency));
				url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agencymap.get(agency));

				HttpUriRequest request = url;
				request.addHeader("Accept-Encoding", "gzip, deflate");
				DocumentBuilder builder = factory.newDocumentBuilder();
				HttpResponse connection = client.execute(url);//url.openConnection();
				InputStream instream = connection.getEntity().getContent();
				Header contentEncoding = connection.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				    instream = new GZIPInputStream(instream);
				}
				Document doc = builder.parse(instream);
				
				NodeList nList = doc.getElementsByTagName("route");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						//Log.v(eElement.getAttribute("title"),eElement.getAttribute("tag"));
						MainActivity.routes.add(eElement.getAttribute("title"));
						MainActivity.routemap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//return routes;
			client.close();
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Do nothing
		}

	}
    
    
    class RetrieveDirections extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){

			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			MainActivity.directionmap = new HashMap<String,String>();
			Log.v("RetreiveDirections","called");

			try {
				//Log.v("url","http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+route);
				//url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+route);
				url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+routemap.get(route));
				HttpUriRequest request = url;
				request.addHeader("Accept-Encoding", "gzip, deflate");
				DocumentBuilder builder = factory.newDocumentBuilder();
				HttpResponse connection = client.execute(url);//url.openConnection();
				InputStream instream = connection.getEntity().getContent();
				Header contentEncoding = connection.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				    instream = new GZIPInputStream(instream);
				}

				doc = builder.parse(instream);
				
				NodeList nList = doc.getElementsByTagName("direction");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (!"".equals(eElement.getAttribute("title")) && eElement.getAttribute("useForUI").equals("true")){
							Log.v("Direction", eElement.getAttribute("title"));
							MainActivity.directionmap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
							directions.add(eElement.getAttribute("title"));
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			client.close();
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Do nothing
		}

	}
	
	
	class RetrieveStops extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			HashMap<String,String> m_stops = new HashMap<String,String>();//tag and stop Name
			HashMap<String,String> ids = new HashMap<String,String>();//tag and stop id
			MainActivity.stopmap = new HashMap<String, String>();

			try {
				
				NodeList nList = doc.getElementsByTagName("stop");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (!"".equals(eElement.getAttribute("title"))){
							m_stops.put(eElement.getAttribute("tag"), eElement.getAttribute("title"));
							ids.put(eElement.getAttribute("tag"), eElement.getAttribute("stopId"));
						}
					}
					
				}
				
				nList = doc.getElementsByTagName("direction");
				for (int i=0; i<nList.getLength(); i++) {
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (eElement.getAttribute("tag").equals(directionmap.get(direction))) {
							NodeList stop = nNode.getChildNodes();
							Log.v("Length",Integer.toString(stop.getLength()));
							for (int n=0; n<stop.getLength(); n++) {
								Node stopData = stop.item(n);
								if (stopData.getNodeType() == Node.ELEMENT_NODE) {
									Element individualStop = (Element) stopData;
									Log.v(m_stops.get(individualStop.getAttribute("tag")),ids.get(individualStop.getAttribute("tag")));
									MainActivity.stopList.add(m_stops.get(individualStop.getAttribute("tag")));
									MainActivity.stopmap.put(m_stops.get(individualStop.getAttribute("tag")), ids.get(individualStop.getAttribute("tag")));
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String[] result) {
			//Do nothing
		}

	}
	
	public class agencylistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
    		routes.clear();
    		directions.clear();
    		stopList.clear();
    		route = "";
    		direction = "";
    		stop = "";
			agency = parent.getItemAtPosition(pos).toString();
			
			AsyncTask<String, Void, String[]> task = new RetrieveRoutes().execute(" ");
			try {
				task.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			routespinner = (Spinner) dialog.findViewById(R.id.routeSpinner);
			ArrayAdapter<String> routeArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_dropdown_item_1line,routes);
			routespinner.setAdapter(routeArrayAdapter);
			routespinner.setOnItemSelectedListener(new routelistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	public class routelistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
    		directions.clear();
    		stopList.clear();
    		direction = "";
    		stop = "";
			//route = routemap.get(parent.getItemAtPosition(pos).toString());//added the routemap get
    		route = parent.getItemAtPosition(pos).toString();
    		Log.v("route",route);
    		//route = parent.getItemAtPosition(pos).toString().Case();//Hacked together change to make charm-city work. What I really should do is separate the route formal and route tag.
			AsyncTask<String, Void, String[]> task = new RetrieveDirections().execute(" ");
			try {
				task.get(2000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			directionspinner = (Spinner) dialog.findViewById(R.id.directionSpinner);
			ArrayAdapter<String> directionArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					//android.R.layout.simple_expandable_list_item_1,directions);
					android.R.layout.simple_dropdown_item_1line,directions);
			directionspinner.setAdapter(directionArrayAdapter);
			directionspinner.setOnItemSelectedListener(new directionlistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//Do nothing
		}
	}
	
	public class directionlistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
    		stopList.clear();
    		stop = "";
			direction = parent.getItemAtPosition(pos).toString();
			AsyncTask<String, Void, String[]> task = new RetrieveStops().execute(" ");
			try {
				task.get(2000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stopspinner = (Spinner)dialog.findViewById(R.id.stopSpinner);
			ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_dropdown_item_1line,stopList);
			stopspinner.setAdapter(stopArrayAdapter);
			stopspinner.setOnItemSelectedListener(new stoplistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//Do nothing

		}
	}
	
	public class stoplistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			stop = parent.getItemAtPosition(pos).toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//Do nothing

		}
	}
}
