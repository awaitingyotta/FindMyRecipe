package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import android.widget.ImageView;
import java.io.Serializable;

import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;

/**
 * @author Alexei Ivanov
 */
public class Recipe implements Serializable {

    private static final long serialVersionUID = 1L;

    private String publisher,f2fUrl,publisherUrl,title,sourceUrl, recipeId, imageUrl;
    private String[] ingredients;
    private double socialRank;
    private int page;

    public Recipe(String publisher, String f2fUrl, String publisherUrl, String title,
                  String sourceUrl, String recipeId, String imageUrl, double socialRank, int page){
        this.publisherUrl = publisherUrl;
        this.socialRank = socialRank;
        this.sourceUrl = sourceUrl;
        this.publisher = publisher;
        this.recipeId = recipeId;
        this.imageUrl = imageUrl;
        this.f2fUrl = f2fUrl;
        this.title = title;
        this.page = page;
    }

    public Recipe(String publisher, String f2fUrl, String publisherUrl, String title,
                  String sourceUrl, String recipeId, String imageUrl, String[] ingredients, double socialRank, int page){
        this.publisherUrl = publisherUrl;
        this.ingredients = ingredients;
        this.socialRank = socialRank;
        this.sourceUrl = sourceUrl;
        this.publisher = publisher;
        this.recipeId = recipeId;
        this.imageUrl = imageUrl;
        this.f2fUrl = f2fUrl;
        this.title = title;
        this.page = page;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getF2fUrl() {
        return f2fUrl;
    }

    public String getPublisherUrl() {
        return publisherUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public double getSocialRank() {
        return socialRank;
    }

    public int getPage() {
        return page;
    }

    public void loadImage(final RecipeListAdapter adapter) {
        // this method is to be used by the search activity
        // first try to use an existing bitmap
        Bitmap image = Utility.getBitmapFromMemCache(recipeId);
        if (image != null) {
            adapter.notifyDataSetChanged();
        } else {
            // if no usable bitmap is available, fetch image from server
            ServerUtility.getImageBitmap(new AsyncTaskCompleteListener() {
                @Override
                public void onSuccess(Bitmap image) {
                    Utility.addBitmapToMemoryCache(recipeId, image);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure() {
                    // use default image
                }
            }, imageUrl);
        }
    }

    public void loadImage(final ImageView container) {
        // this method is to be used by the recipe activity
        // first try to use an existing bitmap
        Bitmap image = Utility.getBitmapFromMemCache(recipeId);
        if (image != null) {
            container.setImageBitmap(image);
            container.refreshDrawableState();
        } else  {
            // if no usable bitmap is available, fetch image from server
            ServerUtility.getImageBitmap(new AsyncTaskCompleteListener() {
                @Override
                public void onSuccess(Bitmap image) {
                    Utility.addBitmapToMemoryCache(recipeId, image);
                    container.setImageBitmap(image);
                    container.refreshDrawableState();
                }

                @Override
                public void onFailure() {
                    // use default image
                }
            }, imageUrl);
        }
    }
}
