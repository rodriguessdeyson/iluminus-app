package com.rad.iluminus.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBContext {

    //region Constants

    /**
     * The name of the database.
     */
    private final String databaseName = "Iluminus";

    //endregion

    //region Attributes

    /**
     * Local instance of sqliteDatabase to manipulate the .db file.
     */
    protected SQLiteDatabase sqliteDatabase;

    /**
     * Activity context references.
     */
    private final Context context;

    //endregion

    //region Constructor

    /**
     * Initialize an objective of type DBContext.
     * @param ctx Activity context.
     */
    public DBContext(Context ctx)
    {
        this.context = ctx;
    }

    //endregion

    //region Methods

    /**
     * Creates a new SQLite database.
     */
    public void Create()
    {
        // Create the database informed.
        sqliteDatabase = context
                .openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);

        // Create a table to persist all the colors history.
        sqliteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS COLORHISTORY(" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "A INTEGER," +
                        "R INTEGER," +
                        "G INTEGER," +
                        "B INTEGER," +
                        "DateTime TEXT)");

        // Create a table to persist favorite colors.
        sqliteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS COLORFAVORITE(" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "A INTEGER," +
                        "R INTEGER," +
                        "G INTEGER," +
                        "B INTEGER," +
                        "DateTime TEXT)");
    }

    /**
     * Opens an existing database by its name.
     */
    protected void Open()
    {
        sqliteDatabase = context
                .openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
    }

    /**
     * Closes an existing opened database.
     */
    protected void Close()
    {
        if (sqliteDatabase.isOpen())
            sqliteDatabase.close();
    }

    //endregion
}
