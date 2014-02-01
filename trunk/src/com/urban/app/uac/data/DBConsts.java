package com.urban.app.uac.data;

import java.util.Locale;

public class DBConsts
{
	public static final String	TABLE_CONTENT		= "content";
	public static final String	TABLE_NAME_PREFIX	= "name_";
	public static final String	TABLE_NAME;

	public static final String	COLUMN_KCAL			= "kcal";
	public static final String	COLUMN_URIC_ACID	= "uric_acid";
	public static final String	COLUMN_NAME			= "name";
	public static final String	COLUMN_UNIT			= "unit";
	public static final String	COLUMN_ID			= "_id";

	public static final String	SQL_SELECT_BY_ID;
	public static final String	SQL_DELETE_BY_ID;
	public static final String	SQL_ORDER_BY_NAME;
	public static final String	SQL_ORDER_BY_KCAL;
	public static final String	SQL_ORDER_BY_URIC_ACID;
	public static final String	SQL_AND_NAME_LIKE;
	public static final String	SQL_AND_ID_EQUALS;
	public static final String	SQL_WHERE_NAME_ID_EQUALS;
	public static final String	SQL_WHERE_CONTENT_ID_EQUALS;

	static
	{
		String lang = Locale.getDefault().getLanguage();
		TABLE_NAME = TABLE_NAME_PREFIX + lang;

		String name_id_col = TABLE_NAME + "." + COLUMN_ID;
		String name_name_col = TABLE_NAME + "." + COLUMN_NAME;
		String content_id_col = TABLE_CONTENT + "." + COLUMN_ID;

		SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + ", " + TABLE_CONTENT + " WHERE " + name_id_col + "=" + content_id_col;
		SQL_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME + ", " + TABLE_CONTENT + " WHERE " + name_id_col + "=" + content_id_col;
		SQL_AND_NAME_LIKE = " AND " + name_name_col + " LIKE ";
		SQL_AND_ID_EQUALS = " AND " + name_id_col + "=";
		SQL_WHERE_NAME_ID_EQUALS = "WHERE " + name_id_col + "=";
		SQL_WHERE_CONTENT_ID_EQUALS = "WHERE " + content_id_col + "=";
		SQL_ORDER_BY_NAME = " ORDER BY " + name_name_col;
		SQL_ORDER_BY_KCAL = " ORDER BY " + TABLE_CONTENT + "." + COLUMN_KCAL;
		SQL_ORDER_BY_URIC_ACID = " ORDER BY " + TABLE_CONTENT + "." + COLUMN_URIC_ACID;
	}
}
