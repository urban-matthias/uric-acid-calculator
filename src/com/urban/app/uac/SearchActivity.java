package com.urban.app.uac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.urban.app.uac.SearchActivity.SearchListCursorAdapter.ViewHolder;
import com.urban.app.uac.data.DBConsts;
import com.urban.app.uac.data.DataBaseManager;
import com.urban.app.uac.data.Ingredient;

public class SearchActivity extends Activity
{
	private DataBaseManager		dataBase		= null;
	private ListView			list_view		= null;

	public static final String	SEARCH_RESULT	= "search_result";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_view);

		dataBase = DataBaseManager.instance();

		Cursor cursor = dataBase.select(DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_ORDER_BY_NAME);
		startManagingCursor(cursor);

		ListAdapter adapter = new SearchListCursorAdapter(this, cursor);

		list_view = (ListView) findViewById(R.id.search_list);
		list_view.setAdapter(adapter);
		list_view.setTextFilterEnabled(true);

		list_view.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				ViewHolder holder = (ViewHolder) view.getTag();

				Cursor cursor = dataBase.select(DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_AND_ID_EQUALS + holder.id);
				cursor.moveToNext();

				Ingredient result = new Ingredient();
				result.name = cursor.getString(1);
				result.unit = cursor.getString(2);
				result.kcal = cursor.getInt(4);
				result.uric_acid = cursor.getInt(5);
				result.amount = 100; // defaults to 100 g

				cursor.close();

				getIntent().putExtra(SEARCH_RESULT, result);
				setResult(RESULT_OK, getIntent());
				finish();
			}
		});

		list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
			{
				ViewHolder holder = (ViewHolder) view.getTag();

				Intent intent = new Intent(getBaseContext(), EditActivity.class);
				intent.putExtra(EditActivity.DB_ENTRY_ID, holder.id);
				startActivity(intent);

				return true;
			}
		});

		EditText search_box = (EditText) findViewById(R.id.search_box);
		search_box.addTextChangedListener(new TextWatcher()
		{
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
			}

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
			{
			}

			public void afterTextChanged(Editable box)
			{
				String search_term = box.toString();
				String sql_stmt = null;
				if (search_term == null || search_term.length() == 0)
				{
					sql_stmt = DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_ORDER_BY_NAME;
				}
				else
				{
					sql_stmt = DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_AND_NAME_LIKE + DatabaseUtils.sqlEscapeString("%" + search_term + "%") + DBConsts.SQL_ORDER_BY_NAME;
				}

				Cursor cursor = dataBase.select(sql_stmt);
				startManagingCursor(cursor);

				ListAdapter adapter = new SearchListCursorAdapter(SearchActivity.this, cursor);
				list_view.setAdapter(adapter);
				list_view.invalidate();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.add_db_entry:
			Intent intent = new Intent(getBaseContext(), EditActivity.class);
			startActivity(intent);
			break;
		default:
			return false;
		}
		return true;
	}

	protected class SearchListCursorAdapter extends SimpleCursorAdapter
	{
		private LayoutInflater	inflater;

		class ViewHolder
		{
			TextView	title;
			TextView	subtitle;
			int			id;
		}

		public SearchListCursorAdapter(Context context, Cursor cursor)
		{
			super(context, R.layout.search_list_item, cursor, new String[0], new int[0]);
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			View view = inflater.inflate(R.layout.search_list_item, parent, false);

			// Save text view handles to avoid inefficient findViewById calls when
			// binding view to actual data
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.search_list_item_title);
			holder.subtitle = (TextView) view.findViewById(R.id.search_list_item_subtitle);
			view.setTag(holder);

			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			int id = cursor.getInt(0);
			String title = cursor.getString(1);
			String energy = cursor.getString(4);
			String uric_acid = cursor.getString(5);
			String subtitle = uric_acid + " " + getString(R.string.mg) + " (" + energy + " " + getString(R.string.kcal) + ")";

			// Get the ViewHolder back to get fast access to the TextViews
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.id = id;
			holder.title.setText(title);
			holder.subtitle.setText(subtitle);
		}
	}
}
