package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import android.util.LruCache;

import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;
import food2fork.com.findmyrecipe.utils.Utility;

/**
 * @author Alexei Ivanov
 */
public class SearchResult {
    private RecipeListAdapter adapter;
    private LruCache<String, Bitmap> memoryCache;
    private String hashCode;
    private String searchTerm;
    private int page;


    public SearchResult(RecipeListAdapter adapter, LruCache<String, Bitmap> memoryCache, String searchTerm, int page) {
        this.memoryCache = memoryCache;
        this.searchTerm = searchTerm;
        this.hashCode = Utility.getHashCode(searchTerm, page);
        this.adapter = adapter;
        this.page = page;
    }

    public String getHashCode(){
        return hashCode;
    }

    public RecipeListAdapter getAdapter() {
        return adapter;
    }

    public LruCache<String, Bitmap> getMemoryCache() {
        return memoryCache;
    }

    public String getSearchTerm(){
        return searchTerm;
    }

    public int getPage() {
        return page;
    }
}
