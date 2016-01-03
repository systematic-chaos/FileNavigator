package shinigami.no.sekai.filenavigator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import sekai.no.shinigami.filenavigator.R;
import android.os.Parcel;
import android.os.Parcelable;

public class NavigationItem implements Parcelable {
	private String path;
	private boolean directory, readable, writable, executable;

	public NavigationItem(String p, boolean d, boolean r, boolean w, boolean e) {
		path = p;
		directory = d;
		readable = r;
		writable = w;
		executable = e;
	}

	public String getPath() {
		return path;
	}

	public boolean isDirectory() {
		return directory;
	}

	public boolean isReadable() {
		return readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean isExecutable() {
		return executable;
	}

	public String getName() {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	public File getParent() {
		return new File(path).getParentFile();
	}

	public int getImageResourceId() {
		int imageResourceId;
		if (directory) {
			if (writable) {
				imageResourceId = R.drawable.folder_icon;
			} else {
				imageResourceId = R.drawable.folder_icon_light;
			}
		} else {
			imageResourceId = R.drawable.file_icon;
		}
		return imageResourceId;
	}

	public Map<String, ?> getMap(String[] keys) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(keys[0], getImageResourceId());
		data.put(keys[1], getName());
		return data;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		boolean[] stat = { directory, readable, writable, executable };
		dest.writeBooleanArray(stat);
	}

	public static final Parcelable.Creator<NavigationItem> CREATOR = new Parcelable.Creator<NavigationItem>() {
		public NavigationItem createFromParcel(Parcel in) {
			return new NavigationItem(in);
		}

		public NavigationItem[] newArray(int size) {
			return new NavigationItem[size];
		}
	};

	private NavigationItem(Parcel in) {
		path = in.readString();
		boolean[] stat = in.createBooleanArray();
		directory = stat[0];
		readable = stat[1];
		writable = stat[2];
	}
}
