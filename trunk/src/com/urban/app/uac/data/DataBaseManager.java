package com.urban.app.uac.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.urban.app.uac.ApplicationContextProvider;
import com.urban.app.uac.R;

public class DataBaseManager extends SQLiteOpenHelper
{
	private static String			DB_PATH				= null;
	private static final String		DB_NAME				= "database";
	private static final int		DATABASE_VERSION	= 1;

	private static SQLiteDatabase	mDataBase			= null;
	private static DataBaseManager	sInstance			= null;

	/**
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 */
	private DataBaseManager()
	{
		super(ApplicationContextProvider.getContext(), DB_NAME, null, DATABASE_VERSION);

		// Android's default system path of the application database
		DB_PATH = "/data/data/" + ApplicationContextProvider.getContext().getPackageName() + "/databases/";

		try
		{
			createDataBase();
			openDataBase();
		}
		catch (IOException e)
		{
			try
			{
				Toast.makeText(ApplicationContextProvider.getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
			catch (Throwable ignore)
			{
			}
		}
	}

	/**
	 * Singleton for DataBase
	 * 
	 * @return singleton instance
	 */
	public static DataBaseManager instance()
	{
		if (sInstance == null)
		{
			sInstance = new DataBaseManager();
		}
		return sInstance;
	}

	/**
	 * Creates an empty database on the system and rewrites it with your own database.
	 * 
	 * @throws java.io.IOException
	 *             io exception
	 */
	private void createDataBase() throws IOException
	{
		boolean dbExist = checkDataBase();

		if (dbExist)
		{
			// do nothing - database already exist
		}
		else
		{
			// By calling this method an empty database will be created into
			// the default system path of the application so we are gonna be
			// able to overwrite that database with our database
			this.getReadableDatabase();

			try
			{
				copyDataBase();
			}
			catch (IOException e)
			{
				try
				{
					Toast.makeText(ApplicationContextProvider.getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
				catch (Throwable ignore)
				{
				}
			}
		}
	}

	/**
	 * Creates an empty database on the system and rewrites it with your own database.
	 * 
	 * @throws java.io.IOException
	 *             io exception
	 */
	public void restoreDataBase()
	{
		// First close the current database.
		close();

		// By calling this method an empty database will be created into
		// the default system path of the application so we are gonna be
		// able to overwrite that database with our database
		this.getReadableDatabase();

		try
		{
			copyDataBase();
			Toast.makeText(ApplicationContextProvider.getContext(), R.string.db_restored, Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			try
			{
				Toast.makeText(ApplicationContextProvider.getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
			catch (Throwable ignore)
			{
			}
		}

		// Close the just created database.
		close();
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase()
	{
		SQLiteDatabase checkDB = null;

		try
		{
			String path = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch (SQLiteException e)
		{
			// database doesn't exist yet.
		}

		if (checkDB != null)
		{
			checkDB.close();
		}

		return checkDB != null;
	}

	/**
	 * Copies the database from the local assets-folder to the just created empty database in the system folder, from
	 * where it can be accessed and handled. This is done by transfering bytestream.
	 * 
	 * @throws java.io.IOException
	 *             io exception
	 */
	public void copyDataBase() throws IOException
	{
		// Open your local db as the input stream
		InputStream input = ApplicationContextProvider.getContext().getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream output = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = input.read(buffer)) > 0)
		{
			output.write(buffer, 0, length);
		}

		// Close the streams
		output.flush();
		output.close();
		input.close();
	}

	private void openDataBase() throws SQLException
	{
		// Open the database
		String path = DB_PATH + DB_NAME;
		mDataBase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
	}

	/**
	 * Select method
	 * 
	 * @param query
	 *            select query
	 * @return - Cursor with the results
	 * @throws android.database.SQLException
	 *             sql exception
	 */
	public Cursor select(String query) throws SQLException
	{
		return mDataBase.rawQuery(query, null);
	}

	/**
	 * Insert method
	 * 
	 * @param table
	 *            - name of the table
	 * @param values
	 *            values to insert
	 * @throws android.database.SQLException
	 *             sql exception
	 */
	public void insert(String table, ContentValues values) throws SQLException
	{
		mDataBase.insert(table, null, values);
	}

	/**
	 * Delete method
	 * 
	 * @param table
	 *            - table name
	 * @param where
	 *            WHERE clause, if pass null, all the rows will be deleted
	 * @throws android.database.SQLException
	 *             sql exception
	 */
	public void delete(String table, String where) throws SQLException
	{
		mDataBase.delete(table, where, null);
	}

	/**
	 * Update method
	 * 
	 * @param table
	 *            - table name
	 * @param values
	 *            - values to update
	 * @param where
	 *            - WHERE clause, if pass null, all rows will be updated
	 */
	public void update(String table, ContentValues values, String where)
	{
		mDataBase.update(table, values, where, null);
	}

	/**
	 * Let you make a raw query
	 * 
	 * @param command
	 *            - the sql comand you want to run
	 */
	public void sqlCommand(String command)
	{
		mDataBase.execSQL(command);
	}

	@Override
	public synchronized void close()
	{
		sInstance = null;

		if (mDataBase != null)
		{
			mDataBase.close();
			mDataBase = null;
		}

		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
