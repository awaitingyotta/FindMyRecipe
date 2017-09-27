package food2fork.com.findmyrecipe.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import food2fork.com.findmyrecipe.MaterialSearchView;
import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.SearchHistory;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;
import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;
import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;
import food2fork.com.findmyrecipe.json.JsonRecipeSearchResult;

/**
 * @author Alexei Ivanov
 */
public class SearchActivity extends BaseActivity {

    private ListView mListView;
    private String mQuery;
    private MaterialSearchView mSearchView;
    private RecipeListAdapter mAdapter;
    private SearchHistory mHistory;
//    private SearchRecentSuggestions mSuggestions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setElevation(0);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite, getTheme()));
            setSupportActionBar(toolbar);
        }

        mHistory = new SearchHistory(this);
        mSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        mSearchView.setVisibility(View.VISIBLE);
        mSearchView.setVoiceSearch(false);
        mSearchView.setCursorDrawable(R.drawable.color_cursor_white);
        mSearchView.setEllipsize(true);
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query, 1);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                mQuery = newText;
                return false;
            }
        });

        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mSearchView.setSuggestions(mHistory.getHistoryCache());
                mSearchView.setQuery(mQuery, false);
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

//        FrameLayout container = (FrameLayout) findViewById(R.id.toolbar_container);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) container.getLayoutParams();
//        params.topMargin = -params.topMargin;
//        container.setLayoutParams(params);

        mListView = (ListView) findViewById(R.id.search_list);
//        mSuggestions = new SearchRecentSuggestions(this,
//                RecipeSuggestionProvider.AUTHORITY, RecipeSuggestionProvider.MODE);

        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        // override this if you wish to use pages - users should be able to use back button to go
        // back to the previous page
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else if (mHistory.sizeOf() > 1) {
            // if search history had more than one element, this will execute the previous search
            mHistory.pop(); // pop the most recent
            mQuery = mHistory.pop(); // pop the previous
            // query should not be null or empty at this point, as long as the above condition remains unchanged
            // the implementation of setQuery does its own null and emptiness checks
            mSearchView.setQuery(mQuery, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mSearchView.requestFocus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        handleIntent(getIntent());
        return super.onSearchRequested();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onPause() {
        mHistory.save();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mHistory.close(); // close the db
        super.onDestroy();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            mSuggestions.saveRecentQuery(query, null);
            search(query, 1);
        }
    }

    private void search(String query, int page) {
        query = query.trim();
        mQuery = query;
        mHistory.push(mQuery);
        try {
            query = URLEncoder.encode(query, ServerUtility.ENCODING);
        } catch (UnsupportedEncodingException e) {
            // do not do any encoding
        }
        query = Utility.QUERY_PARAM + query;
        if (page > 1) {
            query += Utility.PAGE_PARAM + page;
        }
        new SearchTask().execute(query);
    }

    private class SearchTask extends AsyncTask<String, Void, List<Recipe>> {
        private ProgressDialog mProgressDialog;
        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            Utility.clearBitmapCache(); // remove this line if you would like to use back button
            // however, in that case you would have to find an appropriate  way of storing and managing cached images
            mProgressDialog = ProgressDialog.show(SearchActivity.this, "", getResources().getString(R.string.please_wait));
        }
        @Override
        protected List<Recipe> doInBackground(String... params) {
            List<Recipe> recipes;
            JsonRecipeSearchResult searchResult;
            try {
                String query = params[0];
                searchResult = ServerUtility.searchForRecipes(query);
                recipes = Utility.mapRecipes(searchResult.getRecipes());
                return recipes;
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
        protected void onPostExecute(final List<Recipe> result) {
            if (mProgressDialog != null) mProgressDialog.dismiss();

            if (exception == null) {
                if (!result.isEmpty()){
                    if (mListView == null) mListView = (ListView) findViewById(R.id.search_list);
                    mAdapter = new RecipeListAdapter(SearchActivity.this, result);
                    for (Recipe r : result) {
                        r.loadImage(mAdapter);
                    }
                    mListView.setAdapter(mAdapter);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                             @Override
                             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                 if(!result.isEmpty() && result.get(position) != null) {
                                     Bundle extras = new Bundle();
                                     extras.putSerializable(Utility.RECIPE, result.get(position));
                                     navigateToActivity(RecipeActivity.class, extras);
                                 }
                             }
                         }
                    );
                    mSearchView.clearFocus();
                } else {
                    showToast(R.string.no_results);
                }
                mSearchView.setQuery(mQuery, false);
            } else {
                SearchActivity.this.displayMessage(exception);
            }
        }
    }

}
