package shinigami.no.sekai.filenavigator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FileUtils;

import sekai.no.shinigami.filenavigator.R;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

public class FileNavigator extends FragmentActivity implements
		OnItemClickListener, OnItemLongClickListener, OnLongClickListener,
		FileDialog.OnClickListener, TextInputDialog.OnTextInputListener {

	public static final int MODE_DEFAULT = 0;
	public static final int MODE_OPEN = 1;
	public static final int MODE_PASTE = 2;

	protected GridView mFileListView;
	protected ImageButton mBackButton, mNextButton, mUpButton, mHomeButton;
	protected File mCurrentDir, mSelectedItem, mCopiedItem, mMovedItem;
	protected Stack<File> mBackStack, mForwardStack;
	protected List<NavigationItem> mListData;
	protected int mMode;
	protected FileFilter mFileFilter, mDirectoryFilter;

	public static final String[] KEYS = { "image1", "text1" };

	private static final String ARG_BACK_STACK = "BACK_STACK";
	private static final String ARG_FORWARD_STACK = "FORWARD_STACK";
	private static final String ARG_CURRENT_DIR = "CURRENT_DIR";
	private static final String ARG_COPIED_ITEM = "COPIED_ITEM";
	private static final String ARG_MOVED_ITEM = "MOVED_ITEM";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setupLayout();
		mBackStack = new Stack<File>();
		mForwardStack = new Stack<File>();

		if (savedInstanceState == null) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				setFileFilter();
				loadFileList(getInitialDir());
			}
			setupButtonsState();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ArrayList<NavigationItem> stack = new ArrayList<NavigationItem>(
				mBackStack.size());
		for (File f : mBackStack) {
			stack.add(createNavigationItem(f));
		}
		outState.putParcelableArrayList(ARG_BACK_STACK, stack);
		stack = new ArrayList<NavigationItem>(mForwardStack.size());
		for (File f : mForwardStack) {
			stack.add(createNavigationItem(f));
		}
		outState.putParcelableArrayList(ARG_FORWARD_STACK, stack);
		outState.putParcelable(ARG_CURRENT_DIR,
				createNavigationItem(mCurrentDir));
		outState.putParcelable(ARG_COPIED_ITEM,
				createNavigationItem(mCopiedItem));
		outState.putParcelable(ARG_MOVED_ITEM, createNavigationItem(mMovedItem));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		ArrayList<NavigationItem> stack = savedInstanceState
				.getParcelableArrayList(ARG_BACK_STACK);
		for (NavigationItem ni : stack) {
			mBackStack.add(createFile(ni));
		}
		stack = savedInstanceState.getParcelableArrayList(ARG_FORWARD_STACK);
		for (NavigationItem ni : stack) {
			mForwardStack.add(createFile(ni));
		}
		mCurrentDir = createFile((NavigationItem) savedInstanceState
				.getParcelable(ARG_CURRENT_DIR));
		mCopiedItem = createFile((NavigationItem) savedInstanceState
				.getParcelable(ARG_COPIED_ITEM));
		mMovedItem = createFile((NavigationItem) savedInstanceState
				.getParcelable(ARG_MOVED_ITEM));

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			setFileFilter();
			loadFileList(mCurrentDir);
		}
		setupButtonsState();
	}

	protected void setupLayout() {
		mFileListView = (GridView) findViewById(R.id.gridview);
		mFileListView.setOnItemClickListener(this);
		mFileListView.setOnItemLongClickListener(this);
		((ViewGroup) mFileListView.getParent()).setOnLongClickListener(this);
		mFileListView.setEmptyView(findViewById(R.id.emptyview));

		mBackButton = (ImageButton) findViewById(R.id.imageButtonBack);
		mNextButton = (ImageButton) findViewById(R.id.imageButtonNext);
		mUpButton = (ImageButton) findViewById(R.id.imageButtonUp);
		mHomeButton = (ImageButton) findViewById(R.id.imageButtonHome);
	}

	protected File getInitialDir() {
		File initialDir = Environment.getExternalStorageDirectory();
		if (!mDirectoryFilter.accept(initialDir)) {
			initialDir = new File("/");
		}
		return initialDir;
	}

	protected void setFileFilter() {
		mFileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// If a file or directory is unreadable, or hidden, don't show
				// it in the list.
				// Otherwise, show all directories and files left in the list.
				return pathname != null && pathname.canRead()
						&& !pathname.isHidden();
			}

		};

		mDirectoryFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// Return true if the file exists, is actually a directory,
				// can be read and listed, and it is not hidden.
				return pathname != null && pathname.exists()
						&& pathname.isDirectory() && pathname.canRead()
						&& pathname.canExecute() && !pathname.isHidden();
			}
		};
	}

	public void loadFileList(File dir) {
		String[] from = KEYS;
		int[] to = { R.id.image1, R.id.text1 };
		List<NavigationItem> fileListData = initializeFileListData(dir);
		if (fileListData != null) {
			mCurrentDir = dir;
			mListData = fileListData;
			List<Map<String, ?>> data = new ArrayList<Map<String, ?>>(
					mListData.size());
			for (NavigationItem ni : mListData) {
				data.add(ni.getMap(KEYS));
			}
			SimpleAdapter mListAdapter = new SimpleAdapter(this, data,
					R.layout.row, from, to);
			mFileListView.setAdapter(mListAdapter);
		}
	}

	protected List<NavigationItem> initializeFileListData(File dir) {
		List<NavigationItem> adapterFiles = null;
		File[] filesInDir = dir.listFiles(mFileFilter);
		if (filesInDir != null) {
			adapterFiles = new ArrayList<NavigationItem>(filesInDir.length);
			for (File f : filesInDir) {
				adapterFiles.add(createNavigationItem(f));
			}
		}
		return adapterFiles;
	}

	public static NavigationItem createNavigationItem(File f) {
		return f != null ? new NavigationItem(f.getPath(), f.isDirectory(),
				f.canRead(), f.canWrite(), f.canExecute()) : null;
	}

	public static File createFile(NavigationItem ni) {
		return ni != null ? new File(ni.getPath()) : null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		open(mSelectedItem = createFile(mListData.get(position)));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		FileDialog dialog = FileDialog.newInstance(mListData.get(position),
				mMode = MODE_OPEN);
		mSelectedItem = createFile(mListData.get(position));
		dialog.show(getSupportFragmentManager(), getPackageName() + "/"
				+ getLocalClassName());
		return true;
	}

	public void back(View v) {
		File backDir;
		if (!mBackStack.isEmpty()
				&& mDirectoryFilter.accept(backDir = mBackStack.pop())) {
			mForwardStack.push(mCurrentDir);
			loadFileList(backDir);
		}
		setupButtonsState();
	}

	public void next(View v) {
		File forwardDir;
		if (!mForwardStack.isEmpty()
				&& mDirectoryFilter.accept(forwardDir = mForwardStack.pop())) {
			mBackStack.push(mCurrentDir);
			loadFileList(forwardDir);
		}
		setupButtonsState();
	}

	public void up(View v) {
		File parent = mCurrentDir.getParentFile();
		if (mDirectoryFilter.accept(parent)) {
			mBackStack.push(mCurrentDir);
			loadFileList(parent);
			mForwardStack.clear();
			setupButtonsState();
		}
	}

	public void home(View v) {
		File home = getInitialDir();
		if (!home.equals(mCurrentDir)) {
			mBackStack.push(mCurrentDir);
			loadFileList(home);
			mForwardStack.clear();
			setupButtonsState();
		}
	}

	protected void setupButtonsState() {
		mBackButton.setEnabled(!mBackStack.isEmpty());
		mNextButton.setEnabled(!mForwardStack.isEmpty());
		mUpButton.setEnabled(mCurrentDir.getParentFile() != null);
	}

	@Override
	public boolean onLongClick(View v) {
		mMode = mCopiedItem != null || mMovedItem != null ? MODE_PASTE
				: MODE_DEFAULT;
		FileDialog dialog = FileDialog.newInstance(
				createNavigationItem(mCurrentDir), mMode);
		dialog.show(getSupportFragmentManager(), getPackageName() + "/"
				+ getLocalClassName());
		return true;
	}

	@Override
	public void onClick(int[] menuItems, int which) {
		switch (menuItems[which]) {
		case R.string.open:
			open(mSelectedItem);
			break;
		case R.string.copy:
			copy(mSelectedItem);
			break;
		case R.string.move:
			move(mSelectedItem);
			break;
		case R.string.delete:
			delete(mSelectedItem);
			break;
		case R.string.rename:
			TextInputDialog.newInstance(getString(R.string.rename),
					mSelectedItem.getName()).show(getSupportFragmentManager(),
					getPackageName() + "/" + getLocalClassName());
			break;
		case R.string.paste:
			paste(mCurrentDir);
			break;
		case R.string.newDirectory:
			TextInputDialog.newInstance(getString(R.string.newDirectory)).show(
					getSupportFragmentManager(),
					getPackageName() + "/" + getLocalClassName());
			break;
		case R.string.properties:
			displayProperties(mMode == MODE_OPEN ? mSelectedItem : mCurrentDir);
			break;
		}
	}

	public void open(File dir) {
		if (dir.isDirectory() && dir.canRead() && dir.canExecute()) {
			mBackStack.push(mCurrentDir);
			loadFileList(dir);
			mForwardStack.clear();
			setupButtonsState();
		}
	}

	public void copy(File file) {
		if (file.canRead()) {
			mCopiedItem = file;
			mMovedItem = null;
		}
	}

	public void move(File file) {
		if (file.canRead() && file.getParentFile().canWrite()) {
			mMovedItem = file;
			mCopiedItem = null;
		}
	}

	public void delete(File file) {
		if (file.getParentFile().canWrite()) {
			FileUtils.deleteQuietly(file);
			if (file.equals(mCopiedItem))
				mCopiedItem = null;
			if (file.equals(mMovedItem))
				mMovedItem = null;
			if (mBackStack.contains(file))
				mBackStack = (Stack<File>) mBackStack.subList(0,
						mBackStack.indexOf(file));
			if (mForwardStack.contains(file))
				mForwardStack = (Stack<File>) mForwardStack.subList(0,
						mForwardStack.indexOf(file));
			loadFileList(mCurrentDir);
		}
	}

	public void renameFile(File file, String newFileName) {
		if (file.canWrite()) {
			File oldFile = new File(file.getPath());
			file.renameTo(new File(file.getPath().substring(0,
					file.getPath().lastIndexOf('/')), newFileName));
			if (file.equals(mCopiedItem))
				mCopiedItem = file;
			if (file.equals(mMovedItem))
				mMovedItem = file;
			int pos;
			while ((pos = mBackStack.indexOf(oldFile)) > 0)
				mBackStack.setElementAt(file, pos);
			while ((pos = mForwardStack.indexOf(oldFile)) > 0)
				mForwardStack.setElementAt(file, pos);
			loadFileList(mCurrentDir);
		}
	}

	public void paste(File destDir) {
		if (destDir.isDirectory() && destDir.canWrite()) {
			if (mCopiedItem != null) {
				if (mCopiedItem.isFile()) {
					try {
						FileUtils.copyFileToDirectory(mCopiedItem, destDir);
					} catch (IOException ioe) {
						Log.e(getPackageName() + "/" + getLocalClassName(),
								"IOException: " + ioe.getMessage());
					}
				} else {
					try {
						FileUtils
								.copyDirectoryToDirectory(mCopiedItem, destDir);
					} catch (IOException ioe) {
						Log.e(getPackageName() + "/" + getLocalClassName(),
								"IOException: " + ioe.getMessage());
					}
				}
			}
			if (mMovedItem != null) {
				try {
					FileUtils.moveToDirectory(mMovedItem, destDir, true);
				} catch (IOException ioe) {
					Log.e(getPackageName() + "/" + getLocalClassName(),
							"IOException: " + ioe.getMessage());
				}
				mCopiedItem = new File(destDir, mMovedItem.getName());
				mMovedItem = null;
			}
			loadFileList(mCurrentDir);
		}
	}

	public void newDirectory(File dir, String newDirName) {
		if (dir.isDirectory() && dir.canWrite()) {
			new File(dir, newDirName).mkdir();
			loadFileList(mCurrentDir);
		}
	}

	public void displayProperties(File file) {
		PropertiesDialog dialog = PropertiesDialog.newInstance(file.getPath());
		dialog.show(getSupportFragmentManager(), getPackageName() + "/"
				+ getLocalClassName());
	}

	@Override
	public void onTextInput(String input) {
		if (mMode == MODE_OPEN) {
			renameFile(mSelectedItem, input);
		} else {
			newDirectory(mCurrentDir, input);
		}

	}
}
