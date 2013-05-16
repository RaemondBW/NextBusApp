package com.raemond.nextbus;

import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

public class Bus_Stop {
	public String agencyRoute = new String();
	public String prediction = new String();
	public String url = new String();
	public String formalAgency = new String();
	public String formalStop = new String();
	public String stoptext = new String();
	public String stop = new String();
	public String route = new String();
	public String agency = new String();
	public String formalRoute = new String();
	public FrameLayout currentFrame;
	Context context;
	
	Bus_Stop() {
		//Does nothing
	}
	
	Bus_Stop(String m_agency,String m_formalAgency, String m_route, String m_formalRoute, String m_stop, String m_formalStop, FrameLayout m_currentFrame, Context m_context) {
		agencyRoute = m_formalAgency + ": " + m_formalRoute;
		//stoptext = "Stop: " + m_formalStop;
		stoptext = m_formalStop;
		stop = m_stop;
		formalStop = m_formalStop;
		formalAgency = m_formalAgency;
		formalRoute = m_formalRoute;
		agency = m_agency;
		route = m_route;
		context = m_context;
		url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=" + m_agency + "&stopId=" + m_stop + "&routeTag=" + m_route;
		currentFrame = m_currentFrame;
		
		TextView minutesText = (TextView) currentFrame.findViewById(R.id.minutes);
		minutesText.setTypeface(MainActivity.robotoCond);
		//TextView arrivalText = (TextView) currentFrame.findViewById(R.id.bus_arrives_in);
		//arrivalText.setTypeface(MainActivity.robotoCond);
		
		
		TextView agencyRouteText = (TextView) currentFrame.findViewById(R.id.TransitAgency_Route);
		agencyRouteText.setTypeface(MainActivity.robotoCond);
		TextView stopText = (TextView) currentFrame.findViewById(R.id.bus_stop);
		stopText.setTypeface(MainActivity.robotoCond);
		agencyRouteText.setText(agencyRoute);
		agencyRouteText.setTypeface(MainActivity.robotoCond);
		stopText.setText(stoptext);
		stopText.setTypeface(MainActivity.robotoCond);
		new RetrievePrediction().execute(url);
		
		FrameLayout favorite = (FrameLayout) currentFrame.findViewById(R.id.favoriteIMG);
		favorite.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.removeCard(Bus_Stop.this);
			}
		});
		/*final Button button = (Button) currentFrame.findViewById(R.id.menuButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	PopupMenu popup = new PopupMenu(context, button);
                popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                    	switch (item.getItemId()) {
                    	case R.id.remove:
                    		MainActivity.removeCard(Bus_Stop.this);
                    		break;
                    	case R.id.navigate:
                    		String[] formatedStop = formalStop.replace("&", "and").split("\\(");
                    		String uri = String.format(Locale.ENGLISH, "geo:0,0?q=" + formatedStop[0]);
                    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    		context.startActivity(intent);
                    		break;
                    	}
                        return true;
                    }
                });

                popup.show();
            }
        });*/
	}
	
	public void reAddFrame(FrameLayout m_currentFrame) {
		currentFrame = m_currentFrame;
		
		TextView minutesText = (TextView) currentFrame.findViewById(R.id.minutes);
		minutesText.setTypeface(MainActivity.robotoCond);
		//TextView arrivalText = (TextView) currentFrame.findViewById(R.id.bus_arrives_in);
		//arrivalText.setTypeface(MainActivity.robotoCond);
		
		TextView agencyRouteText = (TextView) currentFrame.findViewById(R.id.TransitAgency_Route);
		agencyRouteText.setTypeface(MainActivity.robotoCond);
		TextView stopText = (TextView) currentFrame.findViewById(R.id.bus_stop);
		stopText.setTypeface(MainActivity.robotoCond);
		agencyRouteText.setText(agencyRoute);
		agencyRouteText.setTypeface(MainActivity.robotoCond);
		stopText.setText(stoptext);
		stopText.setTypeface(MainActivity.robotoCond);
		new RetrievePrediction().execute(url);
		
		FrameLayout favorite = (FrameLayout) currentFrame.findViewById(R.id.favoriteIMG);
		favorite.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v("Clicked", "favorite button");
				MainActivity.removeCard(Bus_Stop.this);
			}
		});
		/*final Button button = (Button) currentFrame.findViewById(R.id.menuButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	PopupMenu popup = new PopupMenu(context, button);
                popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                    	switch (item.getItemId()) {
                    	case R.id.remove:
                    		MainActivity.removeCard(Bus_Stop.this);
                    		break;
                    	case R.id.navigate:
                    		String[] formatedStop = formalStop.replace("&", "and").split("\\(");
                    		String uri = String.format(Locale.ENGLISH, "geo:0,0?q=" + formatedStop[0]);
                    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    		context.startActivity(intent);
                    		break;
                    	//case R.id.reminder:
                    		//Toast.makeText(context, "Set Reminder", Toast.LENGTH_SHORT).show();
                    		//break;
                    	}
                        return true;
                    }
                });

                popup.show();
            }
        });*/
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
			TextView estimation = (TextView) currentFrame.findViewById(R.id.time);
			estimation.setTypeface(MainActivity.robotoCond);
			estimation.setText(result);
			Log.v("bus estimation",result);
		}
	}
}
