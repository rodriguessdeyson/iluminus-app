package com.rad.iluminus.repository.models;

import android.graphics.Color;

public class ColorFavorite
{
	/**
	 * Id of the register.
	 */
	private int Id;

	/**
	 * Color opacity.
	 */
	private int A;

	/**
	 * Color R reference.
	 */
	private int R;

	/**
	 * Color G reference.
	 */
	private int G;

	/**
	 * Color B reference.
	 */
	private int B;

	/**
	 * DateTime when the color was saved.
	 */
	private String SavedTime;

	//region Methods
	/**
	 * Sets the Id.
	 * @param id Id of the register.
	 */
	public void setId(int id)
	{
		Id = id;
	}

	public int getId()
	{
		return Id;
	}

	public void setA(int a)
	{
		A = a;
	}

	public void setR(int r)
	{
		R = r;
	}

	public void setG(int g)
	{
		G = g;
	}

	public void setB(int b)
	{
		B = b;
	}

	public void setSavedTime(String savedTime)
	{
		SavedTime = savedTime;
	}

	public int getA() { return A; }

	public int getR()
	{
		return R;
	}

	public int getG()
	{
		return G;
	}

	public int getB()
	{
		return B;
	}

	public int GetColor()
	{
		return Color.argb((int)A, (int)R, (int)G, (int)B);
	}

	/**
	 * Retrieves the saved DateTime
	 * @return The DateTime that the color was saved.
	 */
	public String getSavedTime()
	{
		return SavedTime;
	}

	//endregion
}
