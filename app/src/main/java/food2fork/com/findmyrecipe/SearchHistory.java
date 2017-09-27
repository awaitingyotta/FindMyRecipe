package food2fork.com.findmyrecipe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import food2fork.com.findmyrecipe.utils.DictionaryOpenHelper;

/**
 * @author Alexei Ivanov
 */
public class SearchHistory {

    private int counter; // this is used to id search terms in the db
    private SQLiteDatabase database; // the local database of search terms
    private ArrayList<String> historyCache; // previous + current
    private ArrayList<String> currentHistory; // history stack (should only be accessed through pop or push)
    private ArrayList<String> previousHistory; // search terms used previously

    public SearchHistory(Context context) {
        String term;
        Cursor cursor;
        DictionaryOpenHelper dictionary = new DictionaryOpenHelper(context);
        previousHistory = new ArrayList<>();
        currentHistory = new ArrayList<>();
        historyCache = new ArrayList<>();
        database = dictionary.getWritableDatabase();

//        clear(); // use this to clear the db (should be an option in settings) - leave commented out otherwise

        cursor = database.rawQuery(DictionaryOpenHelper.SELECT_SUGGESTIONS_QUERY, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                term = cursor.getString(cursor.getColumnIndex(DictionaryOpenHelper.VALUES_COLUMN_NAME));
                if (term != null && !term.isEmpty()) {
                    previousHistory.add(term);
                }
            }
            cursor.close();
        }
        historyCache.addAll(previousHistory); // we combine the 2 for this user session
        counter = previousHistory.size(); // previous searches should remain untouched
    }

    public String pop(){
        return sizeOf() == 0 ? null : currentHistory.remove(currentHistory.size()-1);
    }

    public boolean push(String term){
        if (currentHistory != null) {
            final int size = currentHistory.size();
            // we add to both lists, since pop() removes items from the current list,
            // but only if the item is new to the list
            if (!currentHistory.contains(term)) {
                currentHistory.add(term);
            }
            if (!historyCache.contains(term)) {
                historyCache.add(term);
            }
            return currentHistory.size() > size;
        }
        return false;
    }

    public int sizeOf() {
        return currentHistory == null ? 0 : currentHistory.size();
    }

    public ArrayList<String> getHistoryCache() {
        return historyCache;
    }

    public void save() {
        long success;
        ContentValues values;
        historyCache.removeAll(previousHistory); // we remove previous searches - they have already been saved
        for (String term : historyCache) {
            values = new ContentValues();
            values.put(DictionaryOpenHelper.KEYS_COLUMN_NAME, DictionaryOpenHelper.KEY + ( counter++ ));
            values.put(DictionaryOpenHelper.VALUES_COLUMN_NAME , term);
            success = database.insert(DictionaryOpenHelper.TABLE_NAME, null, values);
            if (success > -1) { // add item to previous history if db save was successful
                // since we have already removed all values from history cache that are found in
                // previous history, we do not need to check if the item is already there
                previousHistory.add(term);
            }
        }
    }

    public void clear() {
        database.execSQL(DictionaryOpenHelper.DELETE_SUGGESTIONS_QUERY);
    }

    public void close() {
        database.close();
    }

}
