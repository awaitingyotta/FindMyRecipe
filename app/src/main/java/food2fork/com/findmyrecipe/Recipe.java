package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import java.io.Serializable;

import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;

/**
 * @author Alexei Ivanov
 */
public class Recipe implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

    private String publisher,f2fUrl,publisherUrl,title,sourceUrl, recipeId, imageUrl;
    private String[] ingredients;
    private double socialRank;
    private int page;

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    protected Recipe(Parcel in) {
        ingredients = in.createStringArray();
        publisherUrl = in.readString();
        socialRank = in.readDouble();
        sourceUrl = in.readString();
        publisher = in.readString();
        recipeId = in.readString();
        imageUrl = in.readString();
        f2fUrl = in.readString();
        title = in.readString();
        page = in.readInt();
    }

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
                    adapter.increaseLoadedImagesCount();
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure() {
                    adapter.notifyDataSetChanged();
                    adapter.increaseLoadedImagesCount();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(ingredients);
        parcel.writeString(publisherUrl);
        parcel.writeDouble(socialRank);
        parcel.writeString(sourceUrl);
        parcel.writeString(publisher);
        parcel.writeString(recipeId);
        parcel.writeString(imageUrl);
        parcel.writeString(f2fUrl);
        parcel.writeString(title);
        parcel.writeInt(page);
    }
}
