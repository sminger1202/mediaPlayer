package com.youku.local;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.baseproject.utils.Logger;
import com.youku.thumbnailer.UThumbnailer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaDatabase {
	public final static String TAG = "MediaDatabase";

	private static MediaDatabase instance;

	private SQLiteDatabase mDb;
	private final String DB_NAME = "local_media";
	private final int DB_VERSION = 1;
	private final int CHUNK_SIZE = 50;

	private final String MEDIA_TABLE_NAME = "media_table";
	private final String MEDIA_LOCATION = "location";
	private final String MEDIA_DURATION = "duration";
	private final String MEDIA_PROGRESS = "progress";
	private final String MEDIA_TITLE = "title";
	private final String MEDIA_THUMBNAIL = "thumbnail";

	public enum MediaColumn {
		MEDIA_TABLE_NAME, MEDIA_PATH, MEDIA_DURATION, MEDIA_PROGRESS, MEDIA_TITLE, MEDIA_THUMBNAIL
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	private MediaDatabase(Context context) {
		// create or open database
		DatabaseHelper helper = new DatabaseHelper(context);
		this.mDb = helper.getWritableDatabase();
	}

	public synchronized static MediaDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new MediaDatabase(context.getApplicationContext());
		}
		return instance;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		public void dropMediaTableQuery(SQLiteDatabase db) {
			String query = "DROP TABLE " + MEDIA_TABLE_NAME + ";";
			db.execSQL(query);
		}

		public void createMediaTableQuery(SQLiteDatabase db) {
			String query = "CREATE TABLE IF NOT EXISTS " + MEDIA_TABLE_NAME
					+ " (" + MEDIA_LOCATION + " TEXT PRIMARY KEY NOT NULL, "
					+ MEDIA_DURATION + " INTEGER, " + MEDIA_PROGRESS
					+ " INTEGER, " + MEDIA_THUMBNAIL + " TEXT, " + MEDIA_TITLE
					+ " TEXT" + ");";
			db.execSQL(query);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 创建表
			createMediaTableQuery(db);
			// 删除缩略图文件夹
			if (Scanner.isUplayerSupported) {
				UThumbnailer.deleteThumbnailerFolder();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion < DB_VERSION && newVersion == DB_VERSION) {
				dropMediaTableQuery(db);
				createMediaTableQuery(db);
			}
		}
	}

	/**
	 * Add a new media to the database. The picture can only added by update.
	 * 
	 * @param media
	 *            which you like to add to the database
	 */
	public synchronized void addMedia(Media media) {

		ContentValues values = new ContentValues();
		Logger.d(TAG, "Add to database:" + media.getLocation());
		values.put(MEDIA_LOCATION, media.getLocation());
		values.put(MEDIA_DURATION, media.getDuration());
		values.put(MEDIA_PROGRESS, media.getProgress());
		values.put(MEDIA_TITLE, media.getTitle());
		values.put(MEDIA_THUMBNAIL, media.getThumbnailPath());
		mDb.replace(MEDIA_TABLE_NAME, "NULL", values);

	}

	/**
	 * Check if the item is already in the database
	 * 
	 * @param location
	 *            of the item (primary key)
	 * @return True if the item exists, false if it does not
	 */
	public synchronized boolean mediaItemExists(String location) {
		try {
			Cursor cursor = mDb.query(MEDIA_TABLE_NAME,
					new String[] { MEDIA_LOCATION }, MEDIA_LOCATION + "=?",
					new String[] { location }, null, null, null);
			boolean exists = cursor.moveToFirst();
			cursor.close();
			return exists;
		} catch (Exception e) {
			Logger.e(TAG, "Query failed");
			return false;
		}
	}

	/**
	 * Get all paths from the items in the database
	 * 
	 * @return list of File
	 */
	@SuppressWarnings("unused")
	private synchronized HashSet<File> getMediaFiles() {

		HashSet<File> files = new HashSet<File>();
		Cursor cursor;

		cursor = mDb.query(MEDIA_TABLE_NAME, new String[] { MEDIA_LOCATION },
				null, null, null, null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			do {
				File file = new File(cursor.getString(0));
				files.add(file);
			} while (cursor.moveToNext());
		}
		cursor.close();

		return files;
	}

	public synchronized HashMap<String, Media> getMedias(Context context) {

		Cursor cursor;
		HashMap<String, Media> medias = new HashMap<String, Media>();
		int chunk_count = 0;
		int count = 0;

		do {
			count = 0;
			cursor = mDb.rawQuery(String.format(Locale.CHINA,
					"SELECT %s,%s,%s,%s,%s FROM %s LIMIT %d OFFSET %d",
					MEDIA_DURATION, // 0 long
					MEDIA_PROGRESS, // 1 long
					MEDIA_TITLE, // 2 string
					MEDIA_LOCATION, // 3 string
					MEDIA_THUMBNAIL, // 4 string
					MEDIA_TABLE_NAME, CHUNK_SIZE, chunk_count * CHUNK_SIZE),
					null);

			if (cursor.moveToFirst()) {
				do {
					String location = cursor.getString(3);
					Media media = new Media(context, location,
							cursor.getLong(0), // MEDIA_DURATION
							cursor.getLong(1), // MEDIA_PROGRESS
							cursor.getString(2),// MEDIA_TITLE
							cursor.getString(4)); // MEDIA_THUMBNAIL

					medias.put(media.getLocation(), media);

					count++;
				} while (cursor.moveToNext());
			}

			cursor.close();
			chunk_count++;
		} while (count == CHUNK_SIZE);

		return medias;
	}

	public synchronized Media getMedia(Context context, String location) {

		Cursor cursor;
		Media media = null;

		cursor = mDb.query(MEDIA_TABLE_NAME,
				new String[] { MEDIA_DURATION, // 0
						// long
						MEDIA_PROGRESS, // 1 long
						MEDIA_TITLE, // 2 string
						MEDIA_THUMBNAIL // 3 string
				}, MEDIA_LOCATION + "=?", new String[] { location }, null,
				null, null);
		if (cursor.moveToFirst()) {
			media = new Media(context, location, cursor.getLong(0),
					cursor.getLong(1), cursor.getString(2), cursor.getString(3));
		}
		cursor.close();
		return media;
	}

	public synchronized void removeMedia(String location) {
		mDb.delete(MEDIA_TABLE_NAME, MEDIA_LOCATION + "=?",
				new String[] { location });
	}

	public void removeMedias(Set<String> locations) {
		mDb.beginTransaction();
		try {
			for (String location : locations) {
				Logger.d(TAG, "removeMedia:" + location);
				mDb.delete(MEDIA_TABLE_NAME, MEDIA_LOCATION + "=?",
						new String[] { location });
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}

	public synchronized void updateMedia(String location, MediaColumn col,
			Object object) {

		if (location == null)
			return;

		ContentValues values = new ContentValues();
		switch (col) {
		case MEDIA_DURATION:
			if (object != null)
				values.put(MEDIA_DURATION, (Long) object);
			break;
		case MEDIA_PROGRESS:
			if (object != null)
				values.put(MEDIA_PROGRESS, (Long) object);
			break;
		default:
			return;
		}
		mDb.update(MEDIA_TABLE_NAME, values, MEDIA_LOCATION + "=?",
				new String[] { location });
	}

	/**
	 * 刪除数据库，测试用
	 */
	public synchronized void emptyDatabase() {
		mDb.delete(MEDIA_TABLE_NAME, null, null);
	}
}
