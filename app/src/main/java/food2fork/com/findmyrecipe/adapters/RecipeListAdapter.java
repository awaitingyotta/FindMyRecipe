package food2fork.com.findmyrecipe.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import food2fork.com.findmyrecipe.OnImagesLoadedListener;
import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.SearchState;
import food2fork.com.findmyrecipe.activities.SearchActivity;
import food2fork.com.findmyrecipe.utils.Utility;

/**
 * @author Alexei Ivanov
 */
public class RecipeListAdapter extends BaseAdapter {

    private List<Recipe> recipes;
    private LayoutInflater inflater;
    private int loadedImagesCount;
    private OnImagesLoadedListener listener;
    private SearchActivity activity;
    private SearchState state;

    public RecipeListAdapter(Context context, List<Recipe> results) {
        recipes = results;
        inflater = LayoutInflater.from(context);
        activity = (SearchActivity) context;
        state = new SearchState(activity.getPage(), activity.getQuery(), this, null);
    }

    public int getCount() {
        return recipes.size();
    }

    public Object getItem(int position) {
        return recipes.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.recipe_list_row, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.label = convertView.findViewById(R.id.recipes_list_recipe_label);
        holder.miniature = convertView.findViewById(R.id.recipes_list_recipe_icon);

        holder.label.setText(Utility.convertHtml(recipes.get(position).getTitle()));

        if (Utility.getBitmapFromMemCache(recipes.get(position).getRecipeId()) != null) {
            holder.miniature.setImageBitmap(Utility.getBitmapFromMemCache(recipes.get(position).getRecipeId()));
        } else {
            holder.miniature.setImageResource(R.drawable.no_image);
        }



        if (listener != null && loadedImagesCount == getCount()) {
            state.setCache(Utility.getBitmapMemCache());
            listener.imagesLoaded(state); // once the images have been downloaded completely, we pass the reference
            loadedImagesCount = 0; // oh yeah
        }

//        for (View view : holder.getViews()) {
//            if(view.getClass() == TextView.class) ((TextView)view).setTypeface(Utility.getMainFont(parent.getContext()));
//        }


        return convertView;
    }

    public ArrayList<Recipe> getRecipes() {
        return new ArrayList<>(recipes);
    }

    public void increaseLoadedImagesCount() {
        loadedImagesCount++;
    }

    public void setOnImagesLoadedListener(OnImagesLoadedListener listener) {
        this.listener = listener;
    }

    static class ViewHolder  {

        TextView label;
        TextView description;
        ImageView miniature;

        List<View> views;

        public List<View> getViews(){
            return views != null ? views : initViews();
        }

        private List<View> initViews() {
            views = new ArrayList<>();

            if(description != null) views.add(description);
            if(label != null) views.add(label);
            if(miniature != null) views.add(miniature);

            return views;
        }

    }
}
