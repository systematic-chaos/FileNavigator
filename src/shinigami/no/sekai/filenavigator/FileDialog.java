package shinigami.no.sekai.filenavigator;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;

import sekai.no.shinigami.filenavigator.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FileDialog extends DialogFragment {

	private static final String ARG_NAVIGATION_ITEM = "FileDialog.NAVIGATION_ITEM";
	private static final String ARG_DIALOG_MODE = "FileDialog.DIALOG_MODE";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		Bundle args = getArguments();
		NavigationItem file = args.getParcelable(ARG_NAVIGATION_ITEM);
		int mode = args.getInt(ARG_DIALOG_MODE);
		final int[] menuItemsIds = getMenuItems(file, mode);
		String[] menuItems = new String[menuItemsIds.length];
		for (int n = 0; n < menuItemsIds.length; n++) {
			menuItems[n] = getString(menuItemsIds[n]);
		}
		builder.setItems(menuItems, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onClick(menuItemsIds, which);
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}

	public static FileDialog newInstance(NavigationItem file, int mode) {
		FileDialog f = new FileDialog();

		// Supply NavigationItem input as an argument
		Bundle args = new Bundle();
		args.putParcelable(ARG_NAVIGATION_ITEM, file);
		args.putInt(ARG_DIALOG_MODE, mode);
		f.setArguments(args);

		return f;
	}

	public interface OnClickListener {
		public void onClick(int[] menuItemsIds, int which);
	}

	private OnClickListener mListener;

	@Override
	public void onAttach(Activity activity) throws ClassCastException {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the OnClickListener so we can send events to the host
			mListener = (OnClickListener) activity;
		} catch (ClassCastException cce) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement DialogInterface.OnClickListener");
		}
	}

	public int[] getMenuItems(NavigationItem file, int mode) {
		ArrayList<Integer> menuItems = new ArrayList<Integer>();
		if (mode == FileNavigator.MODE_OPEN) {
			if (file.isDirectory() && file.isReadable() && file.isExecutable())
				menuItems.add(R.string.open);
			if (file.isWritable())
				menuItems.add(R.string.rename);
			File parent = file.getParent();
			if (parent.canRead() && parent.canExecute()) {
				menuItems.add(R.string.copy);
				if (parent.canWrite()) {
					menuItems.add(R.string.move);
					menuItems.add(R.string.delete);
				}
			}
		} else {
			if (file.isWritable()) {
				if (mode == FileNavigator.MODE_PASTE)
					menuItems.add(R.string.paste);
				menuItems.add(R.string.newDirectory);
			}
		}
		menuItems.add(R.string.properties);
		return ArrayUtils.toPrimitive(menuItems.toArray(new Integer[0]));
	}
}
