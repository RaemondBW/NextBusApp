package com.raemond.nextbus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

import android.app.Dialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class addNewStopPopUp {
	Dialog dialog;
	
	Spinner agencyspinner;
	Spinner routespinner;
	Spinner directionspinner;
	Spinner stopspinner;
	LinearLayout linearLayout;
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
	Document doc;
	Context context;
	
	public addNewStopPopUp(final Context m_context, LinearLayout m_linearLayout) {
		context = m_context;
		linearLayout = m_linearLayout;
		
		dialog = new Dialog((Context) context);
		dialog.setContentView(R.layout.add_stop_popup);
		dialog.setTitle("pick your stop:");
		
		//create the list of agencies
		new RetrieveAgencies().execute(" ");

		Button dialogButton = (Button) dialog.findViewById(R.id.addBusStop);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!agency.isEmpty() && !route.isEmpty() && !stop.isEmpty()) {
					final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					FrameLayout temp = (FrameLayout) inflater.inflate(R.layout.bus_info_fragment,null);
					linearLayout.addView(temp);
					
					Bus_Stop new_stop = new Bus_Stop(agencymap.get(agency), agency, routemap.get(route), route, stopmap.get(stop), stop, temp, context);
					MainActivity.stops.add(new_stop);
					dialog.dismiss();
				}
			}
		});
		//dialog.show();
	}
	
	public void showDialog() {
		dialog.show();
	}
	
	class RetrieveAgencies extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			
			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			agencymap = new HashMap<String,String>();
			try {
				url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList");
				HttpUriRequest request = url;
				request.addHeader("Accept-Encoding", "gzip, deflate");
				DocumentBuilder builder = factory.newDocumentBuilder();
				HttpResponse connection = client.execute(url);
				InputStream instream = connection.getEntity().getContent();
				Header contentEncoding = connection.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				    instream = new GZIPInputStream(instream);
				}
				Document doc = builder.parse(instream);
				
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
			client.close();
			return null;
		}

		protected void onPostExecute(String[] result) {
			agencyspinner = (Spinner) dialog.findViewById(R.id.agencySpinner);
			ArrayAdapter<String> agencyArrayAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,agencies);
			agencyspinner.setAdapter(agencyArrayAdapter);
			agencyspinner.setOnItemSelectedListener(new agencylistener());
		}
	}
	
	
    class RetrieveRoutes extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			
			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			routemap = new HashMap<String,String>();
			try {
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
						routes.add(eElement.getAttribute("title"));
						routemap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			client.close();
			return null;
		}

		protected void onPostExecute(String[] result) {
			routespinner = (Spinner) dialog.findViewById(R.id.routeSpinner);
			ArrayAdapter<String> routeArrayAdapter = new ArrayAdapter<String>(context,
					android.R.layout.simple_dropdown_item_1line,routes);
			routespinner.setAdapter(routeArrayAdapter);
			routespinner.setOnItemSelectedListener(new routelistener());
		}

	}
    
    
    class RetrieveDirections extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){

			HttpGet url;    
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			directionmap = new HashMap<String,String>();

			try {
				url = new HttpGet("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencymap.get(agency)+"&r="+routemap.get(route));
				HttpUriRequest request = url;
				request.addHeader("Accept-Encoding", "gzip, deflate");
				DocumentBuilder builder = factory.newDocumentBuilder();
				HttpResponse connection = client.execute(url);
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
							directionmap.put(eElement.getAttribute("title"), eElement.getAttribute("tag"));
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
			directionspinner = (Spinner) dialog.findViewById(R.id.directionSpinner);
			ArrayAdapter<String> directionArrayAdapter = new ArrayAdapter<String>(context,
					android.R.layout.simple_dropdown_item_1line,directions);
			directionspinner.setAdapter(directionArrayAdapter);
			directionspinner.setOnItemSelectedListener(new directionlistener());
		}

	}
	
	
	class RetrieveStops extends AsyncTask<String, Void, String[]> {
		protected String[] doInBackground(String... urls){
			HashMap<String,String> m_stops = new HashMap<String,String>();
			HashMap<String,String> ids = new HashMap<String,String>();
			stopmap = new HashMap<String, String>();

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
							for (int n=0; n<stop.getLength(); n++) {
								Node stopData = stop.item(n);
								if (stopData.getNodeType() == Node.ELEMENT_NODE) {
									Element individualStop = (Element) stopData;
									stopList.add(m_stops.get(individualStop.getAttribute("tag")));
									stopmap.put(m_stops.get(individualStop.getAttribute("tag")), ids.get(individualStop.getAttribute("tag")));
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
			stopspinner = (Spinner)dialog.findViewById(R.id.stopSpinner);
			ArrayAdapter<String> stopArrayAdapter = new ArrayAdapter<String>(context,
					android.R.layout.simple_dropdown_item_1line,stopList);
			stopspinner.setAdapter(stopArrayAdapter);
			stopspinner.setOnItemSelectedListener(new stoplistener());
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
			
			new RetrieveRoutes().execute(" ");
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//Do nothing
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

    		route = parent.getItemAtPosition(pos).toString();
			new RetrieveDirections().execute(" ");
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
			new RetrieveStops().execute(" ");
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
