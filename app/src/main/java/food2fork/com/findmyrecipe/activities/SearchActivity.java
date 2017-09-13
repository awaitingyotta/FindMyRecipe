package food2fork.com.findmyrecipe.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.ServerUtility;
import food2fork.com.findmyrecipe.Utility;
import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;
import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;
import food2fork.com.findmyrecipe.json.JsonRecipeSearchResult;

public class SearchActivity extends BaseActivity {

    private ListView mListView;
    private String mQuery;
    private SearchView mSearchView;
//    private ActionBar mActionBar;
    private RecipeListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mListView = (ListView) findViewById(R.id.search_list);
        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
//        mActionBar = getSupportActionBar();
//        if (mActionBar != null) {
//            mActionBar.setElevation(0);
//            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//            mActionBar.setCustomView(R.layout.action_bar);
//
//        }
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.hasFocus()){
            mSearchView.clearFocus();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        mSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        mSearchView.setIconified(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
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

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query, 1);
        }
    }

    private void search(String query, int page) {
        query = query.trim();
        mQuery = query;
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
