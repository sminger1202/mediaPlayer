package com.youku.local;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.LogTag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Util {
	public final static String TAG = "Util";

	public static File URItoFile(String URI) {
		return new File(Uri.decode(URI).replace("file://", ""));
	}

	public static String URItoFileName(String URI) {
		return URItoFile(URI).getName();
	}

	public static String stripTrailingSlash(String s) {
		if (s.endsWith("/") && s.length() > 1)
			return s.substring(0, s.length() - 1);
		return s;
	}

	public static boolean hasExternalStorage() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static String[] getStorageDirectories() {
		String[] dirs = null;
		BufferedReader bufReader = null;
		try {
			bufReader = new BufferedReader(new FileReader("/proc/mounts"));
			ArrayList<String> list = new ArrayList<String>();
			list.add(Environment.getExternalStorageDirectory().getPath());
			String line;
			while ((line = bufReader.readLine()) != null) {
				if (line.contains("vfat") || line.contains("exfat")
						|| line.contains("/mnt") || line.contains("/Removable")) {
					StringTokenizer tokens = new StringTokenizer(line, " ");
					String s = tokens.nextToken();
					s = tokens.nextToken(); // Take the second token, i.e. mount
											// point

					if (list.contains(s))
						continue;

					if (line.contains("/dev/block/vold")) {
						if (!line.startsWith("tmpfs")
								&& !line.startsWith("/dev/mapper")
								&& !s.startsWith("/mnt/secure")
								&& !s.startsWith("/mnt/shell")
								&& !s.startsWith("/mnt/asec")
								&& !s.startsWith("/mnt/obb")
								&& !s.startsWith("/mnt/fuse")) {
							list.add(s);
						}
					}
				}
			}

			dirs = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				dirs[i] = list.get(i);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (bufReader != null) {
				try {
					bufReader.close();
				} catch (IOException e) {
				}
			}
		}
		return dirs;
	}

	public static String[] getMediaDirectories() {
		ArrayList<String> list = new ArrayList<String>();
		String[] storageDirectories = Util.getStorageDirectories();
		Logger.d(LogTag.TAG_LOCAL, Arrays.toString(storageDirectories));
		Arrays.toString(storageDirectories);
		list.addAll(Arrays.asList(storageDirectories));
		return list.toArray(new String[list.size()]);
	}

	public static String getFileName(String path) {
		if (TextUtils.isEmpty(path))
			return "";
		else {
			int separatorIndex = path.lastIndexOf(".");
			return (separatorIndex < 0) ? path : path.substring(
					separatorIndex + 1, path.length());
		}
	}

	public static void deleteFile(final File file) {
		if (file == null)
			return;
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
				return;
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				if (files != null) {
					for (File temp : files) {
						deleteFile(temp);
					}
				}
			}
			file.delete();
		}
	}
}
