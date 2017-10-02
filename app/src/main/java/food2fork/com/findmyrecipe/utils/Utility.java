package food2fork.com.findmyrecipe.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.util.LruCache;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.SearchResult;
import food2fork.com.findmyrecipe.json.JsonRecipe;

/**
 * @author Alexei Ivanov
 */
public class Utility {

    public static final String URL = "URL";
    public static final String RECIPE = "recipe";
    public static final String RECIPE_ID_PARAM = "&rId=";
    public static final String QUERY_PARAM = "&q=";
    public static final String PAGE_PARAM ="&page=";
    public static final String TITLE = "title";

    private static LruCache<String, Bitmap> memoryCache;
    private static LinkedHashMap<String, SearchResult> searchResultCache; // searches returned (<search term, adapter>)
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8; // Using 1/8th of the available memory for this memory cache as recommended.


    public static List<Recipe> mapRecipes(List<JsonRecipe> jsonObjects){
        List<Recipe> recipes = new ArrayList<>();

        for (JsonRecipe jsonObject : jsonObjects) {
            // note the use of different constructor here, so convertRecipe() cannot be applied in this case
            recipes.add(new Recipe(jsonObject.getPublisher(), jsonObject.getF2f_url(), jsonObject.getPublisher_url(),
                    jsonObject.getTitle(), jsonObject.getSource_url(), jsonObject.getRecipe_id(), jsonObject.getImage_url(),
                    jsonObject.getSocial_rank(), jsonObject.getPage()));
        }

        return recipes;
    }

    public static Recipe convertRecipe(JsonRecipe jsonObject){
        return new Recipe(jsonObject.getPublisher(), jsonObject.getF2f_url(), jsonObject.getPublisher_url(),
                jsonObject.getTitle(), jsonObject.getSource_url(), jsonObject.getRecipe_id(), jsonObject.getImage_url(),
                jsonObject.getIngredients(), jsonObject.getSocial_rank(), jsonObject.getPage());
    }

    // the following methods belong to the second solution (after SerialBitmap did not work)
    // the solution was fetched from reference of
    // https://stackoverflow.com/questions/31131640/android-java-binder-failed-binder-transaction/31133766#31133766
    // to https://developer.android.com/topic/performance/graphics/cache-bitmap.html
    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        initBitmapCache();
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        // do not init here, if memoryCache is null here, memoryCache.get(key) will yield nothing
        return memoryCache == null ? null : memoryCache.get(key);
    }

    public static void clearBitmapCache() {
        // use with caution - should only be used before each new search
        if (memoryCache != null) memoryCache.evictAll();
    }

    private static void initBitmapCache() {
        if (memoryCache == null) {
            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }

    public static String convertHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {

            }
        }
    }

    public static LruCache<String, Bitmap> getBitmapMemCache() {
        return memoryCache;
    }

    public static boolean resultHistoryContainsResult(String hashCode) {
        return searchResultCache != null && searchResultCache.containsKey(hashCode);
    }

    public static void addSearchResult(SearchResult result) {
        if (searchResultCache == null) {
            initSearchResultCache();
        }
        searchResultCache.put(result.getHashCode(), result);
    }

    public static SearchResult getSearchResult(String hashCode) {
        return searchResultCache == null ? null : searchResultCache.get(hashCode);
    }

    private static void initSearchResultCache() {
        searchResultCache = new LinkedHashMap<>();
    }

    public static boolean removeSearchResult(String hashCode) {
        return searchResultCache != null && searchResultCache.remove(hashCode) != null;
    }

    public static void setBitmapMemCache(LruCache<String,Bitmap> bitmapMemCache) {
        memoryCache = bitmapMemCache;
    }

    public static void resetBitmapCache() {
        memoryCache = null;
        initBitmapCache();
    }

    public static String getHashCode(String query, int page) {
        if (query == null || query.isEmpty()) return "";
        return String.valueOf((query+page).hashCode());
    }

}
