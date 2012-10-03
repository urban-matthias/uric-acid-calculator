package com.urban.app.uac;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.urban.app.uac.data.DataBaseManager;
import com.urban.app.uac.data.Ingredient;
import com.urban.app.uac.data.Receipts;
import com.urban.app.uac.dialog.ConfirmDialog;
import com.urban.app.uac.dialog.InputDialog;
import com.urban.app.uac.dialog.SelectionDialog;
import com.urban.app.uac.util.SharedPrefs;

public class MainActivity extends Activity
{
	public static final int		SEARCH_REQUEST		= 1;
	private static final String	SHARED_PREFS_LIMIT	= "com.urban.app.uac.limit";
	private static final String	PREF_LIMIT			= "limit";

	private TextView			sumTextField		= null;
	private ListView			receiptView			= null;
	private String				labelMg				= null;
	private String				labelKcal			= null;
	private ReceiptListAdapter	receipt				= null;
	private int					limit				= 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		labelMg = getString(R.string.mg);
		labelKcal = getString(R.string.kcal);

		sumTextField = (TextView) findViewById(R.id.sum);

		limit = SharedPrefs.load(SHARED_PREFS_LIMIT, PREF_LIMIT, 500); // defaults to 500 mg per day

		ArrayList<Ingredient> last_receipt = Receipts.loadLastReceipt();
		if (last_receipt == null)
		{
			last_receipt = new ArrayList<Ingredient>();
		}

		receipt = new ReceiptListAdapter(this, R.layout.receipt_list_item, last_receipt);
		receiptView = (ListView) findViewById(R.id.recipe);
		receiptView.setAdapter(receipt);

		receiptView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, final int pos, long id)
			{
				Ingredient ingredient = receipt.getItem(pos);
				InputDialog dialog = new InputDialog(MainActivity.this, R.string.dialog_amount, InputDialog.SIGNED_FLOAT_NUMBER, "" + (int) ingredient.amount)
				{
					@Override
					public boolean onOkClicked(String input)
					{
						receipt.getItem(pos).amount = Float.parseFloat(input);
						receipt.notifyDataSetChanged();
						calcAndUpdateSum();
						return true;
					}
				};
				dialog.show();
			}
		});

		receiptView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
			{
				final Ingredient ingredient = receipt.getItem(pos);
				ConfirmDialog dialog = new ConfirmDialog(MainActivity.this, R.string.dialog_discard_ingredient)
				{
					@Override
					public boolean onOkClicked()
					{
						receipt.remove(ingredient);
						receipt.notifyDataSetChanged();
						calcAndUpdateSum();
						return true;
					}
				};
				dialog.show();
				return true;
			}
		});

		Button add = (Button) findViewById(R.id.buttonAdd);
		add.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(getBaseContext(), SearchActivity.class);
				startActivityForResult(intent, SEARCH_REQUEST);
			}
		});

		Button clear = (Button) findViewById(R.id.buttonClear);
		clear.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				receipt = new ReceiptListAdapter(MainActivity.this, R.layout.receipt_list_item, new ArrayList<Ingredient>());
				receiptView.setAdapter(receipt);
				calcAndUpdateSum();
			}
		});

		calcAndUpdateSum();
	}

	/**
	 * Calculate the sum of uric acid and energy in the receipt and update the text field.
	 */
	private void calcAndUpdateSum()
	{
		float sumUricAcid = 0;
		float sumKcal = 0;
		for (int i = 0; i < receipt.getCount(); i++)
		{
			Ingredient ingredient = receipt.getItem(i);
			sumUricAcid += ingredient.uric_acid * (ingredient.amount / 100);
			sumKcal += ingredient.kcal * (ingredient.amount / 100);
		}
		sumTextField.setText((int) sumUricAcid + " " + labelMg + " (" + (int) sumKcal + " " + labelKcal + ")");

		if (sumUricAcid > limit)
		{
			sumTextField.setTextColor(Color.RED);
			String msg = getString(R.string.limit_exceeded).replace("%1", limit + " " + labelMg);
			try
			{
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
			catch (Throwable ignore)
			{
			}
		}
		else
		{
			sumTextField.setTextColor(Color.BLACK);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy()
	{
		DataBaseManager.instance().close();
		Receipts.saveCurrentReceipt(receipt);
		SharedPrefs.save(SHARED_PREFS_LIMIT, PREF_LIMIT, limit);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.load_receipt:
			loadReceipt();
			break;
		case R.id.save_receipt:
			saveReceipt();
			break;
		case R.id.delete_receipt:
			deleteReceipt();
			break;
		case R.id.set_limit:
			setLimit();
			break;
		case R.id.restore_db:
			restoreOriginalDatabase();
			break;
		default:
			return false;
		}
		return true;
	}

	private void loadReceipt()
	{
		String[] receipt_names = Receipts.getSortedReceiptNames();
		SelectionDialog dialog = new SelectionDialog(this, R.string.menu_load_receipt, receipt_names, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				ArrayList<Ingredient> ingredients = Receipts.loadReceipt(selections[which]);
				receipt = new ReceiptListAdapter(MainActivity.this, R.layout.receipt_list_item, ingredients);
				receiptView.setAdapter(receipt);
				calcAndUpdateSum();
				return true;
			}
		};
		dialog.show();
	}

	private void saveReceipt()
	{
		InputDialog dialog = new InputDialog(this, R.string.menu_save_receipt, InputDialog.TEXT, getString(R.string.dialog_save))
		{
			@Override
			public boolean onOkClicked(final String name)
			{
				Receipts.saveReceipt(name, receipt);
				return true;
			}
		};
		dialog.show();
	}

	private void deleteReceipt()
	{
		String[] receipt_names = Receipts.getSortedReceiptNames();
		SelectionDialog dialog = new SelectionDialog(this, R.string.menu_delete_receipt, receipt_names, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				Receipts.deleteReceipt(selections[which]);
				return true;
			}
		};
		dialog.show();
	}

	private void setLimit()
	{
		InputDialog dialog = new InputDialog(this, R.string.menu_set_limit, InputDialog.UNSIGNED_NUMBER, "" + limit)
		{
			@Override
			public boolean onOkClicked(final String limit_str)
			{
				try
				{
					limit = Integer.valueOf(limit_str);
					calcAndUpdateSum();
				}
				catch (Exception e)
				{
				}
				return true;
			}
		};
		dialog.show();
	}

	private void restoreOriginalDatabase()
	{
		ConfirmDialog dialog = new ConfirmDialog(this, R.string.dialog_restore_db)
		{
			@Override
			public boolean onOkClicked()
			{
				DataBaseManager.instance().restoreDataBase();
				return true;
			}
		};
		dialog.show();
	}

	@Override
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data)
	{
		if (resultCode == Activity.RESULT_OK && requestCode == SEARCH_REQUEST)
		{
			Ingredient result = (Ingredient) data.getSerializableExtra(SearchActivity.SEARCH_RESULT);
			receipt.add(result);
			receipt.notifyDataSetChanged();
			calcAndUpdateSum();
		}
	}

	private class ReceiptListAdapter extends ArrayAdapter<Ingredient>
	{
		class ViewHolder
		{
			TextView	title;
			TextView	subtitle;
			TextView	amount;
		}

		public ReceiptListAdapter(Context context, int textViewResourceId, List<Ingredient> objects)
		{
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Ingredient ingredient = getItem(position);

			View view = convertView;
			if (view == null)
			{
				view = getLayoutInflater().inflate(R.layout.receipt_list_item, parent, false);

				ViewHolder holder = new ViewHolder();
				holder.title = (TextView) view.findViewById(R.id.receipt_list_item_title);
				holder.subtitle = (TextView) view.findViewById(R.id.receipt_list_item_subtitle);
				holder.amount = (TextView) view.findViewById(R.id.receipt_list_item_amount);
				view.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			holder.title.setText(ingredient.name);
			holder.subtitle.setText(ingredient.uric_acid + " " + getString(R.string.mg) + " (" + ingredient.kcal + " " + getString(R.string.kcal) + ")");
			holder.amount.setText("x " + (int) ingredient.amount + " " + ingredient.unit);

			return view;
		}
	}
}