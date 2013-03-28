package com.raemond.nextbus;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import android.R;
import com.raemond.nextbus.R;
import com.raemond.nextbus.Bus_Stop;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	DataBaseHelper myDbHelper;
	SQLiteDatabase stopDatabase;
	Dialog dialog;
	
	Spinner agencyspinner;
	Spinner routespinner;
	Spinner directionspinner;
	Spinner stopspinner;
	ArrayList <Bus_Stop> stops = new ArrayList<Bus_Stop>();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Setup my stops.db so that we can query individual stops by different criterions.
	    /*myDbHelper = new DataBaseHelper(this);
	    try {
	    	myDbHelper.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			myDbHelper.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
		
		stopDatabase = myDbHelper.getReadableDatabase();*/
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		// gets the activity's default ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.show();
		
		//TextView transitAgency = (TextView) findViewById(R.id.TransitAgency_Route);
		
		String agency = "actransit";
		String route = "52";
		String direction = "52_53_0";
		String stop = "57955";
		
		/*transitAgency.setText(agency + ": Route " + route);
		stops.setText("Stop: Hearst Av & Le Roy Av");
		String url = new String();
		
		url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=" + agency + "&stopId=" + stop + "&routeTag=" + route;
		Log.v("url",url);
		new RetrievePrediction().execute(url);*/
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
		FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
		linearLayout.addView(temp);
		
		Bus_Stop new_stop = new Bus_Stop(agency,"AC Transit", route, stop, "Hearst Av & Le Roy Av", temp);
		stops.add(new_stop);
		
		/*TextView stops = (TextView) temp.findViewById(R.id.bus_stop);
		stops.setText("Stop: Hearst Av & Le Roy Av");*/
		//estimation.setText(prediction/*getPrediction(agency,route,stop)*/);
		
		
		/*setContentView(R.layout.activity_main);
		agencyspinner = (Spinner)findViewById(R.id.agencySpinner);
		ArrayAdapter<String> agencyArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,agencies);//getAgencies());
		agencyspinner.setAdapter(agencyArrayAdapter);
		agencyspinner.setOnItemSelectedListener(new agencylistener());*/
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
				//System.out.println("Prediction for " + agency + " " + route + " " + stop);
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						//Log.v("Bus Arrival Prediction", eElement.getAttribute("minutes") + " minutes");
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
	
	private ArrayList<String> cursorToArray(Cursor current) {
		ArrayList<String> returnArray = new ArrayList<String>();
		current.moveToFirst();
		do {
			returnArray.add(current.getString(0));
			Log.v("item",current.getString(0));
		} while (current.moveToNext());
		 
		current.close();
		return returnArray;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		//Cursor c = stopDatabase.rawQuery("SELECT DISTINCT agency_name FROM stop", null);
    		
    		//agencies = cursorToArray(c);
    		
    		//final Dialog dialog = new Dialog((Context) this);
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
					final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
					FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
					linearLayout.addView(temp);
					
					Bus_Stop new_stop = new Bus_Stop(agencymap.get(agency), agency, route, stopmap.get(stop), stop, temp);//Don't I need to pass the formal route name?
					stops.add(new_stop);
					dialog.dismiss();
				}
			});
 
			dialog.show();
			
			
			/*routespinner = (Spinner) dialog.findViewById(R.id.routeSpinner);
			ArrayAdapter<String> routeArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,routes);//getRoutes(agency));
			routespinner.setAdapter(routeArrayAdapter);
			routespinner.setOnItemSelectedListener(new routelistener());
    		
    		
			directionspinner = (Spinner) dialog.findViewById(R.id.directionSpinner);
			ArrayAdapter<String> directionArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,directions);//getRoutes(agency));
			directionspinner.setAdapter(directionArrayAdapter);
			directionspinner.setOnItemSelectedListener(new directionlistener());
			
			
			stopspinner = (Spinner) dialog.findViewById(R.id.stopSpinner);
			ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,stopList);
			stopspinner.setAdapter(stopArrayAdapter);
			stopspinner.setOnItemSelectedListener(new stoplistener());*/
			
			
			/*final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listOfStops);
			FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
			linearLayout.addView(temp);
			
			Bus_Stop new_stop = new Bus_Stop(agency,"AC Transit", route, "Hearst Av & Le Roy Av", stop, temp);
			stops.add(new_stop);*/
        	
    		break;
    		
    	}
    	
  		return true;
  	}
    
    public void makeToast(String message) {
    	// with jam obviously
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
	
	
    class RetrieveAgencies extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			//ArrayList<String> agencies = new ArrayList<String>();
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//agencies.clear();
			MainActivity.agencymap = new HashMap<String,String>();
			try {
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList");
				connection = url.openConnection();
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(connection.getInputStream());
				
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
			//return agencies;
			return null;//(String[]) agencies.toArray(new String[0]);
		}

		protected void onPostExecute(String[] result) {
			//Log.v("Bus Prediction",result);
			//TextView estimation = (TextView) findViewById(R.id.time);
			//estimation.setText(result);
		}

	}
	
	
    class RetrieveRoutes extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			//ArrayList<String> routes = new ArrayList<String>();
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//MainActivity.routes.clear();
			try {
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agencymap.get(agency));
				connection = url.openConnection();
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(connection.getInputStream());
				
				NodeList nList = doc.getElementsByTagName("route");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						Log.v("route", eElement.getAttribute("title"));
						MainActivity.routes.add(eElement.getAttribute("title"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//return routes;
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Log.v("Bus Prediction",result);
			//TextView estimation = (TextView) findViewById(R.id.time);
			//estimation.setText(result);
		}

	}
    
    
    class RetrieveDirections extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			//ArrayList<String> directions = new ArrayList<String>();
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			//boolean useForUI = false;
			//directions.clear();
			MainActivity.directionmap = new HashMap<String,String>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+route);
				connection = url.openConnection();
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(connection.getInputStream());
				
				NodeList nList = doc.getElementsByTagName("direction");
				
				for (int i=0; i<nList.getLength(); i++){
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (!"".equals(eElement.getAttribute("title")) && eElement.getAttribute("useForUI").equals("true")){
							//useForUI = true;
							Log.v("Direction", eElement.getAttribute("title"));
							MainActivity.directionmap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
							directions.add(eElement.getAttribute("title"));
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			//return directions;
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Log.v("Bus Prediction",result);
			//TextView estimation = (TextView) findViewById(R.id.time);
			//estimation.setText(result);
		}

	}
	
	/*public static ArrayList<String> getDirection(String agency, String route) {//changed type from bool to arraylist
		ArrayList<String> directions = new ArrayList<String>();
		URL url;
		URLConnection connection;
		DocumentBuilder dBuilder;
		//boolean useForUI = false;
		directionmap = new HashMap<String,String>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agency+"&r="+route);
			connection = url.openConnection();
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(connection.getInputStream());
			
			NodeList nList = doc.getElementsByTagName("direction");
			
			for (int i=0; i<nList.getLength(); i++){
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (!"".equals(eElement.getAttribute("title")) && eElement.getAttribute("useForUI").equals("true")){
						//useForUI = true;
						directionmap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
						directions.add(eElement.getAttribute("title"));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return useForUI;
		return directions;
	}*/
	
	
	class RetrieveStops extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			HashMap<String,String> m_stops = new HashMap<String,String>();//tag and stop Name
			HashMap<String,String> ids = new HashMap<String,String>();//tag and stop id
			//ArrayList<String> returnStops = new ArrayList<String>();
			URL url;
			URLConnection connection;
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			//Log.v("Direction",direction);
			MainActivity.stopmap = new HashMap<String, String>();
			//stopList.clear();
			try {
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+route);
				connection = url.openConnection();
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(connection.getInputStream());
				
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
			//return returnStops;
			return null;
		}

		protected void onPostExecute(String[] result) {
			//Log.v("Bus Prediction",result);
			//TextView estimation = (TextView) findViewById(R.id.time);
			//estimation.setText(result);
		}

	}
	
	public class agencylistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			//routespinner = (Spinner) findViewById(R.id.routeSpinner);
			/*routemap.clear();
			directionmap.clear();
			stopmap.clear();*/
    		routes.clear();
    		directions.clear();
    		stopList.clear();
    		route = "";
    		direction = "";
    		stop = "";
			agency = parent.getItemAtPosition(pos).toString();//agencymap.get(parent.getItemAtPosition(pos).toString());
			/*Cursor c = stopDatabase.rawQuery("SELECT DISTINCT route_name FROM stop WHERE agency_name = '"+agency+"'", null);
			routes = cursorToArray(c);*/
			
			AsyncTask<String, Void, String[]> task = new RetrieveRoutes().execute(" ");
			try {
				task.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			routespinner = (Spinner) dialog.findViewById(R.id.routeSpinner);
			ArrayAdapter<String> routeArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,routes);//getRoutes(agency));
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
			/*directionmap.clear();
			stopmap.clear();*/
    		directions.clear();
    		stopList.clear();
    		direction = "";
    		stop = "";
			route = parent.getItemAtPosition(pos).toString();
			AsyncTask<String, Void, String[]> task = new RetrieveDirections().execute(" ");
			try {
				task.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			directionspinner = (Spinner) dialog.findViewById(R.id.directionSpinner);
			ArrayAdapter<String> directionArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,directions);
			directionspinner.setAdapter(directionArrayAdapter);
			directionspinner.setOnItemSelectedListener(new directionlistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	public class directionlistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			//stopmap.clear();
    		stopList.clear();
    		stop = "";
			direction = parent.getItemAtPosition(pos).toString();//directionmap.get(parent.getItemAtPosition(pos).toString());
			AsyncTask<String, Void, String[]> task = new RetrieveStops().execute(" ");
			try {
				task.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stopspinner = (Spinner)dialog.findViewById(R.id.stopSpinner);
			ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,stopList);
			stopspinner.setAdapter(stopArrayAdapter);
			stopspinner.setOnItemSelectedListener(new stoplistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	}
	
	public class stoplistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			//stopspinner = (Spinner)findViewById(R.id.stopSpinner);
			//stop = directionmap.get(parent.getItemAtPosition(pos).toString());
			stop = parent.getItemAtPosition(pos).toString();
			
			/*ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,getStops(agency,route,direction));
			stopspinner.setAdapter(stopArrayAdapter);*/
			//stopspinner.setOnItemSelectedListener(new directionlistener());
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	}
}
