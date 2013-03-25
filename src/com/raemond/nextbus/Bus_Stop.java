package com.raemond.nextbus;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.raemond.nextbus.MainActivity.RetrievePrediction;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Bus_Stop {
	public String agencyRoute = new String();
	public String stop = new String();
	public String prediction = new String();
	public String url = new String();
	public FrameLayout currentFrame;
	
	Bus_Stop() {
		//Does nothing
	}
	
	Bus_Stop(String m_agency,String formalAgency, String m_route, String formalStop, String m_stop, FrameLayout m_currentFrame) {
		agencyRoute = formalAgency + ": Route " + m_route;
		stop = "Stop: " + formalStop;
		url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=" + m_agency + "&stopId=" + m_stop + "&routeTag=" + m_route;
		currentFrame = m_currentFrame;
		
		TextView agencyRouteText = (TextView) currentFrame.findViewById(R.id.TransitAgency_Route);
		TextView stopText = (TextView) currentFrame.findViewById(R.id.bus_stop);
		agencyRouteText.setText(agencyRoute);
		stopText.setText(stop);
		new RetrievePrediction().execute(url);
	}
	
	public void refreshStop() {
		new RetrievePrediction().execute(url);
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
			TextView estimation = (TextView) currentFrame.findViewById(R.id.time);
			estimation.setText(result);
		}
	}
}
