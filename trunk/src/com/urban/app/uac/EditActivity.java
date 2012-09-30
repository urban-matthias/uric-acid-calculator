package com.urban.app.uac;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.urban.app.uac.data.DBConsts;
import com.urban.app.uac.data.DataBaseManager;
import com.urban.app.uac.dialog.ConfirmDialog;

public class EditActivity extends Activity
{
	public static final String	DB_ENTRY_ID	= "db_entry_id";

	private DataBaseManager		dataBase	= null;
	private int					db_entry_id	= 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_view);

		dataBase = DataBaseManager.instance();

		final EditText name_edit = (EditText) findViewById(R.id.name_edit);
		final EditText unit_edit = (EditText) findViewById(R.id.unit_edit);
		final EditText kcal_edit = (EditText) findViewById(R.id.kcal_edit);
		final EditText uric_acid_edit = (EditText) findViewById(R.id.uric_acid_edit);

		// defaults
		unit_edit.setText("g");
		kcal_edit.setText("0");
		uric_acid_edit.setText("0");

		db_entry_id = getIntent().getIntExtra(DB_ENTRY_ID, -1);
		if (db_entry_id != -1)
		{
			// fill fields from database
			Cursor cursor = dataBase.select(DBConsts.SQL_SELECT_BY_ID + DBConsts.SQL_AND_ID_EQUALS + db_entry_id);
			if (cursor != null)
			{
				cursor.moveToNext();
				name_edit.setText(cursor.getString(1));
				unit_edit.setText(cursor.getString(2));
				kcal_edit.setText("" + cursor.getInt(4));
				uric_acid_edit.setText("" + cursor.getInt(5));
				cursor.close();
			}
		}

		Button button_save = (Button) findViewById(R.id.button_save);
		button_save.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				String name_str = name_edit.getText().toString();
				String unit_str = unit_edit.getText().toString();
				String kcal_str = kcal_edit.getText().toString();
				String uric_acid_str = uric_acid_edit.getText().toString();
				int kcal = kcal_str.length() > 0 ? Integer.parseInt(kcal_str) : 0;
				int uric_acid = uric_acid_str.length() > 0 ? Integer.parseInt(uric_acid_str) : 0;

				ContentValues content_values = new ContentValues();
				content_values.put(DBConsts.COLUMN_KCAL, kcal);
				content_values.put(DBConsts.COLUMN_URIC_ACID, uric_acid);
				
				ContentValues name_values = new ContentValues();
				name_values.put(DBConsts.COLUMN_NAME, name_str);
				name_values.put(DBConsts.COLUMN_UNIT, unit_str);

				if (db_entry_id == -1)
				{
					// new entry
					dataBase.insert(DBConsts.TABLE_CONTENT, content_values);
					dataBase.insert(DBConsts.TABLE_NAME, name_values);
				}
				else
				{
					// existing entry, update it
					dataBase.update(DBConsts.TABLE_CONTENT, content_values, DBConsts.COLUMN_ID + "=" + db_entry_id);
					dataBase.update(DBConsts.TABLE_NAME, name_values, DBConsts.COLUMN_ID + "=" + db_entry_id);
				}

				EditActivity.this.finish();
			}
		});

		Button button_cancel = (Button) findViewById(R.id.button_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				EditActivity.this.finish();
			}
		});

		Button button_delete = (Button) findViewById(R.id.button_delete);
		if (db_entry_id == -1)
		{
			// new database entry, can not be deleted so remove button
			button_delete.setVisibility(View.GONE);
		}
		else
		{
			button_delete.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					ConfirmDialog dialog = new ConfirmDialog(EditActivity.this, R.string.dialog_delete_db_entry)
					{
						@Override
						public boolean onOkClicked()
						{
							dataBase.delete(DBConsts.TABLE_NAME, DBConsts.COLUMN_ID + "=" + db_entry_id);
							dataBase.delete(DBConsts.TABLE_CONTENT, DBConsts.COLUMN_ID + "=" + db_entry_id);
							EditActivity.this.finish();
							return true;
						}
					};
					dialog.show();
				}
			});
		}
	}
}
