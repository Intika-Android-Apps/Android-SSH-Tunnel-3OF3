package org.sshtunnel.beta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity {

	private File currentDir;
	private FileArrayAdapter adapter;

	private void fill(File f) {
		File[] dirs = f.listFiles();
		this.setTitle(getString(R.string.current_dir) + ": " + f.getName());
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory())
					dir.add(new Option(ff.getName(),
							getString(R.string.folder), ff.getAbsolutePath()));
				else {
					fls.add(new Option(ff.getName(),
							getString(R.string.file_size) + ff.length(), ff
									.getAbsolutePath()));
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!f.getName().equalsIgnoreCase("sdcard"))
			dir.add(0,
					new Option("..", getString(R.string.parent_dir), f
							.getParent()));
		adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view,
				dir);
		this.setListAdapter(adapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentDir = new File("/sdcard/");
		fill(currentDir);
	}

	private void onFileClick(Option o) {
		try {
			File folder = new File("/data/data/org.sshtunnel.beta/.ssh");
			if (!folder.exists())
				folder.mkdirs();
			else if (!folder.isDirectory()) {
				folder.delete();
				folder.mkdirs();
			}
			File outputFile = new File(
					"/data/data/org.sshtunnel.beta/.ssh/private_key");
			if (!outputFile.exists())
				outputFile.createNewFile();
			File inputFile = new File(o.getPath());
			FileInputStream input = new FileInputStream(o.getPath());
			if (inputFile.exists() && inputFile.length() < 128 * 1024 && validateFile(input)) {
				input = new FileInputStream(o.getPath());
				FileOutputStream output = new FileOutputStream(
				"/data/data/org.sshtunnel.beta/.ssh/private_key");
				copyFile(input, output);
				Toast.makeText(this,
						getString(R.string.file_toast) + o.getPath(),
						Toast.LENGTH_SHORT).show();
			} else {
				outputFile.delete();
				Toast.makeText(this,
						getString(R.string.file_error) + o.getPath(),
						Toast.LENGTH_SHORT).show();
			}
		} catch (FileNotFoundException e) {
			// Nothing
		} catch (IOException e) {
			// Nothing
		}

		finish();
	}

	private boolean validateFile(InputStream in) throws IOException {
		int count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			if (line.contains("PRIVATE KEY"))
				return true;
			if (count++ > 20)
				return false;
		}
		return false;
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase(getString(R.string.folder))
				|| o.getData().equalsIgnoreCase(getString(R.string.parent_dir))) {
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else {
			onFileClick(o);
		}
	}
}