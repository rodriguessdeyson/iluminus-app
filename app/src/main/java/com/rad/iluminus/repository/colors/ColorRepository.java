package com.rad.iluminus.repository.colors;

import static com.rad.iluminus.utils.ViewUtils.FormatDateTime;

import android.content.Context;
import android.database.Cursor;

import com.rad.iluminus.repository.DBContext;
import com.rad.iluminus.repository.models.ColorHistory;
import com.rad.iluminus.repository.models.ColorFavorite;

import java.util.ArrayList;

public class ColorRepository extends DBContext
{

    /**
     * Initialize an objective of type DBContext.
     *
     * @param ctx Activity context.
     */
    public ColorRepository(Context ctx) { super(ctx); }

    /**
     * Insert a color to color history in database.
     */
    public void Insert(ColorHistory colorHistory)
    {
        Open();
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
        Close();
    }

    /**
     * Delete all register from database.
     */
    public void DeleteColorHistory()
    {
        Open();
        sqliteDatabase.delete("COLORHISTORY", null, null);
        sqliteDatabase.execSQL("VACUUM");
        Close();
    }

    /**
     * Reads the whole color history.
     * @return A list with all colors saved.
     */
    public ArrayList<ColorHistory> ListColorHistory()
    {
        ArrayList<ColorHistory> colors = new ArrayList<>();

        String selectQuery = "SELECT * FROM COLORHISTORY;";

        Open();
        Cursor dbCursor = sqliteDatabase.rawQuery(selectQuery, null);

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
        Close();
        return colors;
    }

    /**
     * Insert a favorite color in database.
     */
    public void InsertFavorite(ColorFavorite colorFavorite)
    {
        Open();
        sqliteDatabase.execSQL(""            +
                "INSERT INTO COLORFAVORITE(" +
                "A,"                         +
                "R,"                         +
                "G,"                         +
                "B,"                         +
                "DateTime)"                  +
                "VALUES("                    +
                "'"+ colorFavorite.getA() +"',"+
                "'"+ colorFavorite.getR() +"',"+
                "'"+ colorFavorite.getG() +"',"+
                "'"+ colorFavorite.getB() +"',"+
                "'"+ FormatDateTime() +"')"+";");
        Close();
    }

    /**
     * Delete a favorite color from database.
     */
    public void DeleteFavorite(int id)
    {
        Open();
        sqliteDatabase.delete("COLORFAVORITE", "Id = ?", new String[]{String.valueOf(id)});
        Close();
    }

    /**
     * Reads the whole favorite colors.
     * @return A list with all favorites saved.
     */
    public ArrayList<ColorFavorite> ListFavorites()
    {
        ArrayList<ColorFavorite> colors = new ArrayList<>();

        String selectQuery = "SELECT * FROM COLORFAVORITE;";

        Open();
        Cursor dbCursor = sqliteDatabase.rawQuery(selectQuery, null);

        if (dbCursor.getCount() > 0)
        {
            while(dbCursor.moveToNext())
            {
                ColorFavorite color = new ColorFavorite();
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
        Close();
        return colors;
    }
}
