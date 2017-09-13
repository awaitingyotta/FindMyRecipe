package food2fork.com.findmyrecipe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Utility;

public class RecipeListAdapter extends BaseAdapter {


    private List<Recipe> recipes;
    private LayoutInflater mInflater;

    public RecipeListAdapter(Context context, List<Recipe> results) {
        recipes = results;
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.recipe_list_row, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.label = convertView.findViewById(R.id.recipes_list_recipe_label);
        holder.miniature = convertView.findViewById(R.id.recipes_list_recipe_icon);

        holder.label.setText(recipes.get(position).getTitle());

        if (Utility.getBitmapFromMemCache(recipes.get(position).getRecipeId()) != null) {
            holder.miniature.setImageBitmap(Utility.getBitmapFromMemCache(recipes.get(position).getRecipeId()));
        } else {
            holder.miniature.setImageResource(R.drawable.ic_meal);
        }

//        for (View view : holder.getViews()) {
//            if(view.getClass() == TextView.class) ((TextView)view).setTypeface(Utility.getMainFont(parent.getContext()));
//        }


        return convertView;
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
