package com.raemond.nextbus;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {

	Spinner agencyspinner;
	Spinner routespinner;
	Spinner directionspinner;
	Spinner stopspinner;
	String agency;
	String route;
	String direction;
	String stop;
	static HashMap<String,String> agencymap;
	static HashMap<String,String> directionmap;
	static HashMap<String,String> routemap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		agencyspinner = (Spinner)findViewById(R.id.agencySpinner);
		ArrayAdapter<String> agencyArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,getAgencies());
		agencyspinner.setAdapter(agencyArrayAdapter);
		agencyspinner.setOnItemSelectedListener(new agencylistener());
	}
	
	private static ArrayList<String> getAgencies() {
		ArrayList<String> agencies = new ArrayList<String>();
		URL url;
		URLConnection connection;
		DocumentBuilder dBuilder;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		agencymap = new HashMap<String,String>();
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
					agencies.add(eElement.getAttribute("title"));
					agencymap.put(eElement.getAttribute("title"),eElement.getAttribute("tag"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return agencies;
	}
	
	public static ArrayList<String> getRoutes(String agency) {
		ArrayList<String> routes = new ArrayList<String>();
		URL url;
		URLConnection connection;
		DocumentBuilder dBuilder;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agency);
			connection = url.openConnection();
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(connection.getInputStream());
			
			NodeList nList = doc.getElementsByTagName("route");
			
			for (int i=0; i<nList.getLength(); i++){
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					//System.out.println("Route Name: " + eElement.getAttribute("title"));
					//System.out.println("Route Tag: " + eElement.getAttribute("tag"));
					//System.out.println("--------------------------");
					routes.add(eElement.getAttribute("title"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return routes;
	}
	
	public static ArrayList<String> getDirection(String agency, String route) {//changed type from bool to arraylist
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
						/*System.out.println("Direction Name: " + eElement.getAttribute("title"));
						System.out.println("Direction Tag: " + eElement.getAttribute("tag"));
						System.out.println("--------------------------");*/
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static ArrayList<String> getStops(String agency, String route, String direction) {
		HashMap<String,String> stops = new HashMap<String,String>();//tag and stop Name
		HashMap<String,String> ids = new HashMap<String,String>();//tag and stop id
		ArrayList<String> returnStops = new ArrayList<String>();
		URL url;
		URLConnection connection;
		DocumentBuilder dBuilder;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Log.v("Direction",direction);
		try {
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agency+"&r="+route);
			connection = url.openConnection();
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(connection.getInputStream());
			
			NodeList nList = doc.getElementsByTagName("stop");
			
			for (int i=0; i<nList.getLength(); i++){
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (!"".equals(eElement.getAttribute("title"))){
						stops.put(eElement.getAttribute("tag"), eElement.getAttribute("title"));
						ids.put(eElement.getAttribute("tag"), eElement.getAttribute("stopId"));
					}
				}
				
			}
			
			nList = doc.getElementsByTagName("direction");
			for (int i=0; i<nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("tag").equals(direction)) {
						NodeList stop = nNode.getChildNodes();
						Log.v("Length",Integer.toString(stop.getLength()));
						for (int n=0; n<stop.getLength(); n++) {
							Node stopData = stop.item(n);
							if (stopData.getNodeType() == Node.ELEMENT_NODE) {
								Element individualStop = (Element) stopData;
								/*System.out.println("Stop Name: " + stops.get(individualStop.getAttribute("tag")));
								System.out.println("Stop Tag: " + individualStop.getAttribute("tag"));
								System.out.println("Stop ID: " + ids.get(individualStop.getAttribute("tag")));
								System.out.println("--------------------------");*/
								Log.v(stops.get(individualStop.getAttribute("tag")),ids.get(individualStop.getAttribute("tag")));
								returnStops.add(stops.get(individualStop.getAttribute("tag")));
								routemap.put(stops.get(individualStop.getAttribute("tag")), ids.get(individualStop.getAttribute("tag")));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnStops;
	}
	
	public class agencylistener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			routespinner = (Spinner)findViewById(R.id.routeSpinner);
			agency = agencymap.get(parent.getItemAtPosition(pos).toString());
			ArrayAdapter<String> routeArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,getRoutes(agency));
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
			directionspinner = (Spinner)findViewById(R.id.directionSpinner);
			route = parent.getItemAtPosition(pos).toString();
			ArrayAdapter<String> directionArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,getDirection(agency,route));
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
			stopspinner = (Spinner)findViewById(R.id.stopSpinner);
			direction = directionmap.get(parent.getItemAtPosition(pos).toString());
			ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_expandable_list_item_1,getStops(agency,route,direction));
			stopspinner.setAdapter(stopArrayAdapter);
			//stopspinner.setOnItemSelectedListener(new directionlistener());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	}
}
