package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;

import food2fork.com.findmyrecipe.json.JsonRecipe;


public class Utility {

    public static final String URL = "URL";
    public static final String RECIPE = "recipe";
    public static final String RECIPE_ID_PARAM = "&rId=";
    public static final String QUERY_PARAM = "&q=";
    public static final String PAGE_PARAM ="&page=";
    public static final String TITLE = "title";

    private static LruCache<String, Bitmap> mMemoryCache;
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
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        // do not init here, if mMemoryCache is null here, mMemoryCache.get(key) will yield nothing
        return mMemoryCache == null ? null : mMemoryCache.get(key);
    }

    public static void clearBitmapCache() {
        // use with caution - should only be used before each new search
        if (mMemoryCache != null) mMemoryCache.evictAll();
    }

    private static void initBitmapCache() {
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }
}
