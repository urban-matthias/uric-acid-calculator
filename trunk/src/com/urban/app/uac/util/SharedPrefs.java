package com.urban.app.uac.util;

import java.util.Arrays;

import android.content.SharedPreferences;

import com.urban.app.uac.ApplicationContextProvider;

public class SharedPrefs
{
	public static void save(String pref_name, String value_name, int value)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(value_name, value);
		editor.commit();
	}

	public static void save(String pref_name, String value_name, String value)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(value_name, value);
		editor.commit();
	}

	public static int load(String pref_name, String value_name, int default_value)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		return prefs.getInt(value_name, default_value);
	}

	public static String load(String pref_name, String value_name, String default_value)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		return prefs.getString(value_name, default_value);
	}

	public static boolean exists(String pref_name, String value_name)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		return prefs.contains(value_name);
	}

	public static void delete(String pref_name, String value_name)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(value_name);
		editor.commit();
	}

	public static String[] getAllSorted(String pref_name)
	{
		SharedPreferences prefs = ApplicationContextProvider.getContext().getSharedPreferences(pref_name, 0);
		String[] all = prefs.getAll().keySet().toArray(new String[0]);
		Arrays.sort(all);
		return all;
	}
}
