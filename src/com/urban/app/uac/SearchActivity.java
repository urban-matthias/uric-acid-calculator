package com.urban.app.uac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
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
import com.urban.app.uac.dialog.SelectionDialog;
import com.urban.app.uac.util.SharedPrefs;

public class SearchActivity extends Activity
{
	private static final String	SHARED_PREFS_FAVORITES		= "com.urban.app.uac.search.favorites";
	private static final String	SHARED_PREFS_SORT_CRITERION	= "com.urban.app.uac.search.sort.criterion";
	private static final String	VALUE_ORDER_BY				= "OrderBy";

	private DataBaseManager		database					= null;
	private ListView			search_list					= null;
	private EditText			search_box					= null;
	private String				order_by					= null;

	public static final String	SEARCH_RESULT				= "search_result";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_view);

		database = DataBaseManager.instance();

		order_by = SharedPrefs.load(SHARED_PREFS_SORT_CRITERION, VALUE_ORDER_BY, DBConsts.SQL_ORDER_BY_NAME);

		Cursor cursor = database.select(DBConsts.SQL_SELECT_BY_ID + order_by);
		startManagingCursor(cursor);

		ListAdapter adapter = new SearchListCursorAdapter(this, cursor);

		search_list = (ListView) findViewById(R.id.search_list);
		search_list.setAdapter(adapter);
		search_list.setTextFilterEnabled(true);

		// Select entry on short click
		search_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				ViewHolder holder = (ViewHolder) view.getTag();

				Cursor cursor = database.select(DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_AND_ID_EQUALS + holder.id);
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

		// Edit entry on long click
		search_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
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

		search_box = (EditText) findViewById(R.id.search_box);

		// Favorites context menu for search box
		registerForContextMenu(search_box);

		// Filter list view while typing
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
				updateSearchList(box.toString());
			}
		});
	}

	private void updateSearchList(String search_term)
	{
		String sql_stmt = null;
		if (search_term == null || search_term.length() == 0)
		{
			sql_stmt = DBConsts.SQL_SELECT_BY_ID + order_by;
		}
		else
		{
			sql_stmt = DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_AND_NAME_LIKE + DatabaseUtils.sqlEscapeString("%" + search_term + "%") + order_by;
		}

		Cursor cursor = database.select(sql_stmt);
		startManagingCursor(cursor);

		ListAdapter adapter = new SearchListCursorAdapter(SearchActivity.this, cursor);
		search_list.setAdapter(adapter);
		search_list.invalidate();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
	{
		if (view.getId() == R.id.search_box)
		{
			menu.clear();
			menu.setHeaderTitle(R.string.menu_title_fav);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.favorites_menu, menu);
		}
		else
		{
			super.onCreateContextMenu(menu, view, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.select_fav:
			selectFavorite();
			break;
		case R.id.add_fav:
			addFavorite();
			break;
		case R.id.remove_fav:
			removeFavorite();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void removeFavorite()
	{
		String[] favorites = SharedPrefs.getAllSorted(SHARED_PREFS_FAVORITES);
		SelectionDialog dialog = new SelectionDialog(this, R.string.menu_remove_fav, favorites, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				String favorite = selections[which];
				SharedPrefs.delete(SHARED_PREFS_FAVORITES, favorite);
				return true;
			}
		};
		dialog.show();
	}

	private void addFavorite()
	{
		String favorite = search_box.getText().toString();
		SharedPrefs.save(SHARED_PREFS_FAVORITES, favorite, "");
	}

	private void selectFavorite()
	{
		String[] favorites = SharedPrefs.getAllSorted(SHARED_PREFS_FAVORITES);
		SelectionDialog dialog = new SelectionDialog(this, R.string.menu_select_fav, favorites, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				String favorite = selections[which];
				search_box.setText(favorite);
				return true;
			}
		};
		dialog.show();
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
		case R.id.sort_db_entries:
			changeSortCriterion();
			break;
		default:
			return false;
		}
		return true;
	}

	private void changeSortCriterion()
	{
		final String[] sortCriteria = new String[] { getString(R.string.sort_criterion_name), getString(R.string.sort_criterion_uric_acid), getString(R.string.sort_criterion_kcal) };
		final String[] orderBys = new String[] { DBConsts.SQL_ORDER_BY_NAME, DBConsts.SQL_ORDER_BY_URIC_ACID, DBConsts.SQL_ORDER_BY_KCAL };
		SelectionDialog dialog = new SelectionDialog(this, R.string.dialog_sort, sortCriteria, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				order_by = orderBys[which];
				SharedPrefs.save(SHARED_PREFS_SORT_CRITERION, VALUE_ORDER_BY, order_by);
				updateSearchList(search_box.getText().toString());
				return true;
			}
		};
		dialog.show();
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
