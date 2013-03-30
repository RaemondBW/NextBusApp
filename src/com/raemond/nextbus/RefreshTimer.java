package com.raemond.nextbus;

import java.util.TimerTask;

public class RefreshTimer extends TimerTask {
	@Override
	public void run() {
		for (Bus_Stop stop: MainActivity.stops) {
			stop.refreshStop();
		}
	}

}
