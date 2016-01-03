package shinigami.no.sekai.filenavigator;

import sekai.no.shinigami.filenavigator.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

public class TextInputDialog extends DialogFragment {

	private static final String ARG_FILE_NAME = "TextInputDialog.FILE_NAME";
	private static final String ARG_OK_MSG = "TextInputDialog.OK_MSG";

	private EditText mEditTextInput;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Import the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		/*
		 * Inflate and set the layout for the dialog. Pass null as the parent
		 * view because it's going to the dialog layout
		 */
		int view = R.layout.text_input;
		mEditTextInput = (EditText) inflater.inflate(view, null);
		Bundle args = getArguments();
		if (args.containsKey(ARG_FILE_NAME)) {
			mEditTextInput.setText(args.getString(ARG_FILE_NAME));
			mEditTextInput.setSelectAllOnFocus(true);
		}

		builder.setView(mEditTextInput)
				// Add action buttons
				.setPositiveButton(args.getCharSequence(ARG_OK_MSG),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener.onTextInput(mEditTextInput.getText()
										.toString());
							}
						}).setNegativeButton(R.string.goback, null);

		// Create the AlertDialog object and return it
		return builder.create();
	}

	public static TextInputDialog newInstance(String okMsg) {
		TextInputDialog f = new TextInputDialog();

		Bundle args = new Bundle();
		args.putString(ARG_OK_MSG, okMsg);
		f.setArguments(args);

		return f;
	}

	public static TextInputDialog newInstance(String okMsg, String fileName) {
		TextInputDialog f = newInstance(okMsg);

		Bundle args = f.getArguments();
		args.putString(ARG_FILE_NAME, fileName);
		f.setArguments(args);

		return f;
	}

	public interface OnTextInputListener {
		public void onTextInput(String input);
	}

	private OnTextInputListener mListener;

	@Override
	public void onAttach(Activity activity) throws ClassCastException {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the OnTextInput so we can send events to the host
			mListener = (OnTextInputListener) activity;
		} catch (ClassCastException cce) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement OnTextInputListener");
		}
	}
}
