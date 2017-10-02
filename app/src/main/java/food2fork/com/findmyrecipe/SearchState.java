package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import android.util.LruCache;

import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;

/**
 * @author Alexei Ivanov
 */
public class SearchState {

    private int page;
    private String query;
    private RecipeListAdapter adapter;
    private LruCache<String, Bitmap> cache;

    public SearchState(int page, String query, RecipeListAdapter adapter, LruCache<String, Bitmap> cache) {
        this.adapter = adapter;
        this.cache = cache;
        this.query = query;
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public String getQuery() {
        return query;
    }

    public RecipeListAdapter getAdapter() {
        return adapter;
    }

    public LruCache<String, Bitmap> getCache() {
        return cache;
    }

    public void setCache(LruCache<String, Bitmap> cache) {
        this.cache = cache;
    }
}
