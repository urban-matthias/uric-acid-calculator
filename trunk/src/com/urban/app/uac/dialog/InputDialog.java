package com.urban.app.uac.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.widget.EditText;

import com.urban.app.uac.R;

public class InputDialog extends Builder implements OnClickListener, DialogInterface.OnDismissListener
{
	public static final int	UNSIGNED_NUMBER		= InputType.TYPE_CLASS_NUMBER;
	public static final int	SIGNED_NUMBER		= InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
	public static final int	SIGNED_FLOAT_NUMBER	= InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
	public static final int	TEXT				= InputType.TYPE_CLASS_TEXT;
	public static final int	CAPITALS			= InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;

	protected EditText		value				= null;
	protected Context		context				= null;
	protected AlertDialog	dialog				= null;
	protected boolean		neutralClicked		= false;

	public InputDialog(Context context, int title, int inputType, String initialValue)
	{
		super(context);
		init(context, title, -1, inputType, initialValue);
	}

	public InputDialog(Context context, int title, int neutralButtonText, int inputType, String initialValue)
	{
		super(context);
		init(context, title, neutralButtonText, inputType, initialValue);
	}

	private void init(Context context, int title, int neutralButtonText, int inputType, String initialValue)
	{
		this.context = context;

		setTitle(title);

		value = new EditText(context);
		value.setId(1199);
		value.setText(initialValue);
		value.setInputType(inputType);
		value.setSingleLine();
		setView(value);

		setPositiveButton(R.string.btn_ok, this);
		setNegativeButton(R.string.btn_cancel, this);
		if (neutralButtonText != -1)
		{
			setNeutralButton(neutralButtonText, this);
		}
	}

	public void onClick(DialogInterface di, int which)
	{
		if (which == DialogInterface.BUTTON_POSITIVE)
		{
			if (onOkClicked(value.getText().toString()))
			{
				di.dismiss();
			}
		}
		else if (which == DialogInterface.BUTTON_NEGATIVE)
		{
			if (onCancelClicked())
			{
				di.dismiss();
			}
		}
		else if (which == DialogInterface.BUTTON_NEUTRAL)
		{
			neutralClicked = true;
			if (onNeutralClicked())
			{
				di.dismiss();
			}
		}
	}

	public boolean onCancelClicked()
	{
		return true;
	}

	public boolean onNeutralClicked()
	{
		return true;
	}

	public boolean onOkClicked(String input)
	{
		return true;
	}

	@Override
	public AlertDialog create()
	{
		dialog = super.create();
		dialog.setOnDismissListener(this);
		return dialog;
	}

	public void onDismiss(DialogInterface di)
	{
		if (neutralClicked)
		{
			neutralClicked = false;
			dialog.show();
		}
	}
}
