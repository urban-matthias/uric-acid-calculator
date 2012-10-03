package com.urban.app.uac.data;

import java.util.ArrayList;
import java.util.Arrays;

import android.widget.ArrayAdapter;

import com.urban.app.uac.util.Base64;
import com.urban.app.uac.util.SharedPrefs;

public class Receipts
{
	private static final String	SHARED_PREFS		= "com.urban.app.uac.receipts";
	private static final String	SHARED_PREFS_LAST	= "com.urban.app.uac.receipts.last";
	private static final String	LAST_RECEIPT		= "receipt";

	public static void saveReceipt(String name, ArrayAdapter<Ingredient> receipt)
	{
		saveReceipt(SHARED_PREFS, name, receipt);
	}

	public static ArrayList<Ingredient> loadReceipt(String name)
	{
		return loadReceipt(SHARED_PREFS, name);
	}

	public static void saveCurrentReceipt(ArrayAdapter<Ingredient> receipt)
	{
		saveReceipt(LAST_RECEIPT, SHARED_PREFS_LAST, receipt);
	}

	public static ArrayList<Ingredient> loadLastReceipt()
	{
		return loadReceipt(LAST_RECEIPT, SHARED_PREFS_LAST);
	}

	public static boolean exists(String name)
	{
		return SharedPrefs.exists(SHARED_PREFS, name);
	}

	public static void deleteReceipt(String name)
	{
		SharedPrefs.delete(SHARED_PREFS, name);
	}

	public static String[] getSortedReceiptNames()
	{
		String[] receipt_names = SharedPrefs.getAllSorted(SHARED_PREFS);
		Arrays.sort(receipt_names);
		return receipt_names;
	}

	private static void saveReceipt(String pref, String name, ArrayAdapter<Ingredient> receipt)
	{
		String serialized = serializeReceipt(receipt);
		SharedPrefs.save(pref, name, serialized);
	}

	private static ArrayList<Ingredient> loadReceipt(String pref, String name)
	{
		String serialized = SharedPrefs.load(pref, name, "");
		ArrayList<Ingredient> receipt = null;
		if (serialized.length() > 0)
		{
			receipt = deserializeReceipt(serialized);
		}
		return receipt;
	}

	private static String serializeReceipt(ArrayAdapter<Ingredient> receipt)
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
	private static ArrayList<Ingredient> deserializeReceipt(String serialized)
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
