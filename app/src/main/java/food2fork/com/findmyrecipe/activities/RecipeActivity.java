package food2fork.com.findmyrecipe.activities;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;
import food2fork.com.findmyrecipe.adapters.IngredientsListAdapter;
import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;
import food2fork.com.findmyrecipe.json.JsonRecipeDetails;

/**
 * @author Alexei Ivanov
 */
public class RecipeActivity extends BaseActivity {

    private RelativeLayout mButtonsAndInfoLayout;

    private Button mViewInstructionsButton;
    private Button mViewOriginalButton;

    private ListView mIngredientsListView;
    private TextView mIngredientsHeader;
    private TextView mPublisherTextView;
    private TextView mRankTextView;
    private TextView mInfoHeader;

    private ImageView mRecipeImage;

    private String mOriginalUrl;
    private String mInstructionsUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Recipe recipe;
        Bundle extras = getIntent().getExtras();

        try {
            recipe = (Recipe) extras.getSerializable(Utility.RECIPE);
        } catch (Exception e) {
            showToast(R.string.recipe_error);
            return;
        }

        if (recipe == null) {
            showToast(R.string.recipe_error);
        } else if (recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()) {
            showToast(R.string.recipe_error);
        } else {
            setTitle(recipe.getTitle());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                View imageLayout = findViewById(R.id.recipe_image_layout);
                imageLayout.setVisibility(View.GONE);
//                LinearLayout main = (LinearLayout) findViewById(R.id.recipe_main_layout);
//                main.setOrientation(LinearLayout.HORIZONTAL);
            }
            mButtonsAndInfoLayout = (RelativeLayout) findViewById(R.id.recipe_buttons_layout);

            mViewInstructionsButton = (Button) findViewById(R.id.recipe_instructions_button);
            mViewOriginalButton = (Button) findViewById(R.id.recipe_original_button);

            mIngredientsListView = (ListView) findViewById(R.id.recipe_ingredients_listview);

            mPublisherTextView = (TextView) findViewById(R.id.recipe_info_publisher);
            mIngredientsHeader = (TextView) findViewById(R.id.recipe_ingredients_header);
            mRankTextView = (TextView) findViewById(R.id.recipe_info_rank);
            mInfoHeader = (TextView) findViewById(R.id.recipe_info_header);
            mRecipeImage = (ImageView) findViewById(R.id.recipe_details_image);

            mViewInstructionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = getString(R.string.instructions_title);
                    showWebViewWindow(mInstructionsUrl, title);
                }
            });

            mViewOriginalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = getString(R.string.original_title);
                    showWebViewWindow(mOriginalUrl, title);
                }
            });

            mViewInstructionsButton.setEnabled(false);
            mViewOriginalButton.setEnabled(false);

            // if the image is already available, this load the image from memory cache
            // if not, this will initiate a url fetch in a separate thread
            // please note that this has to be done after mRecipeImage has been initiated from xml (findViewById has returned a reference)
            recipe.loadImage(mRecipeImage);

            String query = Utility.RECIPE_ID_PARAM + recipe.getRecipeId();
            new RecipeTask().execute(query);
        }
    }

    private void showWebViewWindow(String url, String title) {
        Bundle extras = new Bundle();
        extras.putString(Utility.URL, url);
        extras.putString(Utility.TITLE, title);
        navigateToActivity(WebViewActivity.class, extras);
    }

    private class RecipeTask extends AsyncTask<String, Void, Recipe> {
        private ProgressDialog mProgressDialog;
        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(RecipeActivity.this, "", getResources().getString(R.string.please_wait));
        }
        @Override
        protected Recipe doInBackground(String... params) {
            Recipe recipe;
            JsonRecipeDetails result;

            try {
                String query = params[0];
                result = ServerUtility.getRecipe(query);
                recipe = Utility.convertRecipe(result.getRecipe());
                return recipe;
            } catch (AppErrorException e) {
                exception = e;
                return null;
            } catch (ServerFaultException e) {
                exception = e;
                return null;
            } catch (NetworkErrorException e) {
                exception = e;
                return null;
            } catch (NullPointerException e) {
                exception = e;
                return null;
            } catch (Exception e) { // for params exceptions
                exception = e;
                return null;
            }

        }

        @Override
        protected void onPostExecute(Recipe recipe) {
            if (mProgressDialog != null) mProgressDialog.dismiss();

            if (exception == null) {
                String[] ingredients = recipe.getIngredients();
                String publisher = getString(R.string.publisher_label) + recipe.getPublisher();
                String rank = getString(R.string.rank_label) + String.valueOf(recipe.getSocialRank());

                if (ingredients == null || ingredients.length == 0) {
                    ingredients = new String[] { getString(R.string.no_ingredients)};
                }

                mIngredientsListView.setAdapter(new IngredientsListAdapter(RecipeActivity.this, ingredients));
                mPublisherTextView.setText(publisher);
                mRankTextView.setText(rank);

                mInstructionsUrl = recipe.getF2fUrl();
                mOriginalUrl = recipe.getSourceUrl();

                if (mInstructionsUrl != null && !mInstructionsUrl.isEmpty()) {
                    mViewInstructionsButton.setEnabled(true);
                }
                if (mOriginalUrl != null && !mOriginalUrl.isEmpty()) {
                    mViewOriginalButton.setEnabled(true);
                }

                mButtonsAndInfoLayout.setVisibility(View.VISIBLE);
                mIngredientsHeader.setVisibility(View.VISIBLE);
                mInfoHeader.setVisibility(View.VISIBLE);

            } else {
                RecipeActivity.this.displayMessage(exception);
            }
        }
    }

}
