package com.raemond.nextbus;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper{

	private static String DB_PATH = "/data/data/com.raemond.nextbus/databases/";
	private static String DB_NAME = "myStops.db";
	private static String DB_TABLE_NAME = "saved_stops";
	private static String DB_ID = "_ID";
	private static String DB_Agency_Tag = "agency_tag";
	private static String DB_Agency_Formal = "agency_formal";
	private static String DB_Route = "route";
	private static String DB_Route_Formal = "route_formal";
	private static String DB_Stop_ID = "stop_id";
	private static String DB_Stop_Formal = "stop_formal";
	private static final int DB_Version = 1;
	private SQLiteDatabase myDataBase; 

	
	private static final String DICTIONARY_TABLE_CREATE ="CREATE TABLE " + DB_TABLE_NAME + " (" +
		DB_ID + " INTEGER PRIMARY KEY, " +
		DB_Agency_Tag + " TEXT, " +
		DB_Agency_Formal + " TEXT, " +
		DB_Route + " TEXT, " +
		DB_Route_Formal + " TEXT, " +
		DB_Stop_ID + " TEXT, " +
		DB_Stop_Formal + " TEXT);";

	
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, DB_Version);
	}	

	
	public void openDataBase() throws SQLException{
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	}

	
	@Override
	public synchronized void close() {
		if(myDataBase != null)
			myDataBase.close();
		super.close();
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS employees");
			onCreate(db);
		}
	}
}
