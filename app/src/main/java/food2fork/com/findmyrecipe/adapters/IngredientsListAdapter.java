package food2fork.com.findmyrecipe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import food2fork.com.findmyrecipe.R;

public class IngredientsListAdapter extends BaseAdapter {

    private String[] ingredients;
    private LayoutInflater mInflater;

    public IngredientsListAdapter(Context context, String[] results) {
        ingredients = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return ingredients.length;
    }

    public Object getItem(int position) {
        return ingredients[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        RecipeListAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new RecipeListAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.ingredients_list_row, null);
            convertView.setTag(holder);
        } else {
            holder = (RecipeListAdapter.ViewHolder) convertView.getTag();
        }

        holder.description = convertView.findViewById(R.id.ingredients_list_ingredients_details);
        holder.description.setText(ingredients[position]);

//        for (View view : holder.getViews()) {
//            if(view.getClass() == TextView.class) ((TextView)view).setTypeface(Utility.getMainFont(parent.getContext()));
//        }


        return convertView;
    }

    static class ViewHolder  {

        List<View> views;
        TextView description;

        public List<View> getViews(){
            return views != null ? views : initViews();
        }

        private List<View> initViews() {
            views = new ArrayList<>();
            if(description != null) views.add(description);
            return views;
        }

    }
}
