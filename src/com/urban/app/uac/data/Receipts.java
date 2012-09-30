package com.urban.app.uac.data;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;

import com.urban.app.uac.util.Base64;

public class Receipts
{
	private static final String	SHARED_PREFS		= "com.urban.app.uac";
	private static final String	SHARED_PREFS_LAST	= "com.urban.app.uac.last";

	private Activity			activity			= null;

	public Receipts(Activity activity)
	{
		this.activity = activity;
	}

	public void saveReceipt(String name, ArrayAdapter<Ingredient> receipt)
	{
		saveReceipt(SHARED_PREFS, name, receipt);
	}

	public ArrayList<Ingredient> loadReceipt(String name)
	{
		return loadReceipt(SHARED_PREFS, name);
	}

	public void saveCurrentReceipt(ArrayAdapter<Ingredient> receipt)
	{
		saveReceipt(SHARED_PREFS_LAST, SHARED_PREFS_LAST, receipt);
	}

	public ArrayList<Ingredient> loadLastReceipt()
	{
		return loadReceipt(SHARED_PREFS_LAST, SHARED_PREFS_LAST);
	}

	public boolean exists(String name)
	{
		SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFS, 0);
		return prefs.contains(name);
	}
	
	public void deleteReceipt(String name)
	{
		SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(name);
		editor.commit();
	}

	public String[] getSortedReceiptNames()
	{
		SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFS, 0);
		String[] receipt_names = prefs.getAll().keySet().toArray(new String[0]);
		Arrays.sort(receipt_names);
		return receipt_names;
	}

	private void saveReceipt(String pref, String name, ArrayAdapter<Ingredient> receipt)
	{
		String serialized = serializeReceipt(receipt);
		SharedPreferences prefs = activity.getSharedPreferences(pref, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(name, serialized);
		editor.commit();
	}

	private ArrayList<Ingredient> loadReceipt(String pref, String name)
	{
		SharedPreferences prefs = activity.getSharedPreferences(pref, 0);
		String serialized = prefs.getString(name, "");
		ArrayList<Ingredient> receipt = null;
		if (serialized.length() > 0)
		{
			receipt = deserializeReceipt(serialized);
		}
		return receipt;
	}

	private String serializeReceipt(ArrayAdapter<Ingredient> receipt)
	{
		ArrayList<Ingredient> list = new ArrayList<Ingredient>(receipt.getCount());
		for (int i = 0; i < receipt.getCount(); i++)
		{
			list.add(receipt.getItem(i));
		}
		String serialized = null;
		try
		{
			serialized = Base64.encodeObject(list, Base64.GZIP);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return serialized;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Ingredient> deserializeReceipt(String serialized)
	{
		ArrayList<Ingredient> receipt = null;
		try
		{
			receipt = (ArrayList<Ingredient>) Base64.decodeToObject(serialized, Base64.GZIP, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return receipt;
	}
}
