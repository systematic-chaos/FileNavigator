package shinigami.no.sekai.filenavigator;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import sekai.no.shinigami.filenavigator.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class PropertiesDialog extends DialogFragment {

	private static final String ARG_PATH = "PropertiesDialog.PATH";

	private View mView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		/*
		 * Inflate and set the layout for the dialog. Pass null as the parent
		 * view because it's going to the dialog layout
		 */
		int layout = R.layout.file_properties;
		mView = inflater.inflate(layout, null);

		displayFileData(new File(getArguments().getString(ARG_PATH)));

		builder.setView(mView)
		// Add action button
				.setNeutralButton(R.string.goback, null);

		return builder.create();
	}

	/*
	 * Create a new instance of PropertiesDialog, providing "file" as an input
	 * argument
	 */
	public static PropertiesDialog newInstance(String filePath) {
		PropertiesDialog f = new PropertiesDialog();

		// Supply received file as an argument
		Bundle args = new Bundle();
		args.putString(ARG_PATH, filePath);
		f.setArguments(args);

		return f;
	}

	private void displayFileData(File file) {
		((TextView) mView.findViewById(R.id.textViewName)).setText(file
				.getName());
		((TextView) mView.findViewById(R.id.textViewPath)).setText(file
				.getPath());
		StringBuilder permissions = new StringBuilder();
		if (file.isDirectory())
			permissions.append('d');
		if (file.isFile())
			permissions.append('f');
		if (file.isHidden())
			permissions.append('h');
		if (file.isAbsolute())
			permissions.append('a');
		if (file.canRead())
			permissions.append('r');
		if (file.canWrite())
			permissions.append('w');
		if (file.canExecute())
			permissions.append('x');
		((TextView) mView.findViewById(R.id.textViewPermissions))
				.setText(permissions);
		((TextView) mView.findViewById(R.id.textViewSize)).setText(FileUtils
				.byteCountToDisplaySize(file.length()));
		((TextView) mView.findViewById(R.id.textViewLastTimeModified))
				.setText(DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.LONG).format(new Date(file.lastModified())));
	}
}
