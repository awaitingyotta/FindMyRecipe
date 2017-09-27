package food2fork.com.findmyrecipe.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Alexei Ivanov
 * based on: https://developer.android.com/guide/topics/data/data-storage.html
 */
public class DictionaryOpenHelper extends SQLiteOpenHelper {

    public static final String KEY = "key";
    public static final String TABLE_NAME = "recipe_suggestions";
    public static final String KEYS_COLUMN_NAME = "recipe_keys";
    public static final String VALUES_COLUMN_NAME = "recipe_values";
    public static final String SELECT_SUGGESTIONS_QUERY = "SELECT * FROM " + TABLE_NAME +";";
    public static final String DELETE_SUGGESTIONS_QUERY = "DELETE FROM " + TABLE_NAME +";";

    private static final String TAG = "DictionaryOpenHelper";
    private static final String DATABASE_NAME = "findmyrecipe_db";

    private static final int DATABASE_VERSION = 2;
    private static final String DROP_SUGGESTIONS_TABLE_QUERY = "DROP TABLE IF EXISTS " + TABLE_NAME +";";
    private static final String CREATE_SUGGESTIONS_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEYS_COLUMN_NAME + " TEXT, " +
                    VALUES_COLUMN_NAME + " TEXT);";

    public DictionaryOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SUGGESTIONS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(DROP_SUGGESTIONS_TABLE_QUERY);
        onCreate(db);
    }

}
