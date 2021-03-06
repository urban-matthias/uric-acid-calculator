package com.urban.app.uac.dialog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;

import com.urban.app.uac.R;

public class SelectionDialog extends Builder implements OnClickListener, OnMultiChoiceClickListener
{
	protected String[]		selections	= null;
	private Set<Integer>	selected	= null;

	public SelectionDialog(Context context, int title, String[] selections, boolean multiSelection)
	{
		super(context);
		init(context, title, selections, multiSelection);
		if (multiSelection)
		{
			selected = new HashSet<Integer>();
			setMultiChoiceItems(selections, null, this);
		}
		else
		{
			setItems(selections, this);
		}
	}

	private void init(Context context, int title, String[] selections, boolean multiSelection)
	{
		this.selections = selections;
		setTitle(title);

		if (multiSelection)
		{
			setPositiveButton(R.string.btn_ok, this);
			setNegativeButton(R.string.btn_cancel, this);
		}
	}

	public void onClick(DialogInterface dialog, int which)
	{
		switch (which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			if (onSelection(selected))
			{
				dialog.dismiss();
			}
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		default:
			if (onSelection(which))
			{
				dialog.dismiss();
			}
			break;
		}
	}

	public void onClick(DialogInterface dialog, int which, boolean is_selected)
	{
		if (is_selected)
		{
			selected.add(which);
		}
		else
		{
			selected.remove(which);
		}
	}

	public boolean onSelection(Set<Integer> selected)
	{
		Iterator<Integer> i = selected.iterator();
		while (i.hasNext())
		{
			onSelection(i.next());
		}
		return true;
	}

	public boolean onSelection(int which)
	{
		return true;
	}
}
