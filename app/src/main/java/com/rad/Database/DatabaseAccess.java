package com.rad.Database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rad.Database.models.BluetoothDevicesModel;
import com.rad.Database.models.ColorHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.rad.Utils.ViewUtils.FormatDateTime;

public class DatabaseAccess
{
	/**
	 * Local instance of SQLiteDatabase to manipulate the .db file.
	 */
	private SQLiteDatabase sqliteDatabase;

	/**
	 * Activity context references.
	 */
	private Context appContext;

	/**
	 * Name of the database to be opened.
	 */
	private String database;

	/**
	 * Initialize an objective of type DatabaseAccess.
	 * @param ctx Activity context.
	 * @param database Database name to use.
	 */
	public DatabaseAccess(Context ctx, String database)
	{
		this.appContext = ctx;
		this.database = database;
	}

	/**
	 * Creates a new SQLite database.
	 */
	public void CreateDatabase()
	{
		// Create the database informed.
		sqliteDatabase = appContext
			.openOrCreateDatabase(database, Context.MODE_PRIVATE, null);

		// Create a table to persist all the colors history.
		sqliteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS COLORHISTORY(" +
						"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"A INTEGER," +
						"R INTEGER," +
						"G INTEGER," +
						"B INTEGER," +
						"DateTime TEXT)");

		// Create a table to configure the devices to connect.
		sqliteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS BLUETOOTHCONFIGURATION(" +
						"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"DeviceName TEXT," +
						"DeviceAddress TEXT," +
						"ServiceUUID TEXT);");

		// Create a table to manipulate add show.
		sqliteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS ADMONITORING(" +
						"Id             INTEGER PRIMARY KEY AUTOINCREMENT," +
						"ShowedTime     TEXT NOT NULL)");
	}

	/**
	 * Opens an existing database by its name.
	 */
	private void OpenDatabase()
	{
		sqliteDatabase = appContext
			.openOrCreateDatabase(database, Context.MODE_PRIVATE, null);
	}

	/**
	 * Closes an existing opened database.
	 */
	private void CloseDatabase()
	{
		if (sqliteDatabase.isOpen())
			sqliteDatabase.close();
	}

	/**
	 * Inserts into opened database a new configuration.
	 * @param configuration A configuration of the device.
	 */
	public void Insert(BluetoothDevicesModel configuration)
	{
		OpenDatabase();
		sqliteDatabase.execSQL(
				"INSERT INTO BLUETOOTHCONFIGURATION(" +
						"DeviceName,"        +
						"DeviceAddress,"     +
						"ServiceUUID)"       +
						"VALUES("            +
						"'"+ configuration.getDeviceName()    +"'," +
						"'"+ configuration.getDeviceAddress() +"'," +
						"'"+ configuration.getServiceUUID()   +"')" + ";");
		CloseDatabase();
	}

	/**
	 * Insert a color to color history in database.
	 */
	public void Insert(ColorHistory colorHistory)
	{
		OpenDatabase();
		sqliteDatabase.execSQL(""            +
				"INSERT INTO COLORHISTORY("  +
				"A,"                         +
				"R,"                         +
				"G,"                         +
				"B,"                         +
				"DateTime)"                  +
				"VALUES("                    +
				"'"+ colorHistory.getA() +"',"+
				"'"+ colorHistory.getR() +"',"+
				"'"+ colorHistory.getG() +"',"+
				"'"+ colorHistory.getB() +"',"+
				"'"+ FormatDateTime() +"')"+";");
		CloseDatabase();
	}

	/**
	 * Inserts the dateTime that an ad was showed.
	 * @param dateTime Time in text of showing.
	 */
	public void Insert(String dateTime)
	{
		OpenDatabase();
		sqliteDatabase.execSQL(
				"INSERT INTO ADMONITORING(" +
						"ShowedTime)"    +
						"VALUES("           +
						"'"+ dateTime +"')" + ";");
		CloseDatabase();
	}

	/**
	 * Delete all register from database.
	 */
	public void DeleteColorHistory()
	{
		OpenDatabase();
		sqliteDatabase.delete("COLORHISTORY", null, null);
		sqliteDatabase.execSQL("VACUUM");
		CloseDatabase();
	}

	/**
	 * Reads the whole color history.
	 * @return A list with all colors saved.
	 */
	public ArrayList<ColorHistory> ReadColorHistory()
	{
		// Create an configuration list with all devices configured.
		ArrayList<ColorHistory> colors = new ArrayList<>();

		// Creates the query to retrieve all devices.
		String selectQuery = "SELECT * FROM COLORHISTORY;";

		// Get all devices configured.
		OpenDatabase();
		Cursor dbCursor = sqliteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			while(dbCursor.moveToNext())
			{
				ColorHistory color = new ColorHistory();
				color.setId(dbCursor.getInt(0));
				color.setA(dbCursor.getInt(1));
				color.setR(dbCursor.getInt(2));
				color.setG(dbCursor.getInt(3));
				color.setB(dbCursor.getInt(4));
				color.setSavedTime(dbCursor.getString(5));
				colors.add(color);
			}
		}
		dbCursor.close();
		CloseDatabase();
		return colors;
	}

	/**
	 * Reades the configurations already in database.
	 * @return A list of configurations.
	 */
	public ArrayList<BluetoothDevicesModel> ReadBluetoothConfiguration()
	{
		// Create an configuration list with all devices configured.
		ArrayList<BluetoothDevicesModel> configurations = new ArrayList<>();

		// Creates the query to retrieve all devices.
		String selectQuery = "SELECT * FROM BLUETOOTHCONFIGURATION;";

		// Get all devices configured.
		OpenDatabase();
		Cursor dbCursor = sqliteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			while(dbCursor.moveToNext())
			{
				BluetoothDevicesModel configuration = new BluetoothDevicesModel();
				configuration.setId(dbCursor.getInt(0));
				configuration.setDeviceName(dbCursor.getString(1));
				configuration.setDeviceAddress(dbCursor.getString(2));
				configuration.setServiceUUID(UUID.fromString(dbCursor.getString(3)));
				configurations.add(configuration);
			}
		}
		dbCursor.close();
		return configurations;
	}

	/**
	 * Updates the dateTime that an ad was showed.
	 */
	public void UpdateLastAdShown()
	{
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		OpenDatabase();
		sqliteDatabase.execSQL(
				"UPDATE ADMONITORING SET " +
						"ShowedTime =" + "'" + dateFormat.format(date) + "'" +
						"WHERE Id =" + "'" + 1 + "'" + ";");
		CloseDatabase();
	}

	/**
	 * Gets the last time an ad was shown
	 * @return THe DateTime of shown;
	 */
	public Date GetTimeLastAdShowed()
	{
		Date lastShowed = null;
		String selectQuery = "SELECT * FROM ADMONITORING;";

		OpenDatabase();
		Cursor dbCursor = sqliteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			@SuppressLint("SimpleDateFormat")
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			try
			{
				while (dbCursor.moveToNext())
				{
					lastShowed = format.parse(dbCursor.getString(1));
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		dbCursor.close();
		CloseDatabase();
		return lastShowed;
	}
}
