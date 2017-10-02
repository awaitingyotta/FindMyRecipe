package food2fork.com.findmyrecipe.activities;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import food2fork.com.findmyrecipe.MaterialSearchView;
import food2fork.com.findmyrecipe.MenuItemClickListener;
import food2fork.com.findmyrecipe.OnImagesLoadedListener;
import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Recipe;
import food2fork.com.findmyrecipe.SearchState;
import food2fork.com.findmyrecipe.SearchHistory;
import food2fork.com.findmyrecipe.SearchResult;
import food2fork.com.findmyrecipe.SearchTaskFragment;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;
import food2fork.com.findmyrecipe.adapters.RecipeListAdapter;

/**
 * @author Alexei Ivanov
 */
public class SearchActivity extends BaseActivity implements SearchTaskFragment.TaskCallbacks {

    private static final int MAX_COUNT = 30;
    private static final int HIDE_KEYBOARD_DELAY = 500;
    private static final String SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT";

    private int mPage;
    private int mCount;
    private boolean mKeyboardIsVisible;

    private ProgressDialog mProgressDialog;
    private Exception exception = null;

    private String mQuery;
    private ListView mListView;
    private SearchHistory mHistory;
    private RecipeListAdapter mAdapter;
    private MaterialSearchView mSearchView;

    private FloatingActionButton mButtonPrevious;
    private FloatingActionButton mButtonNext;
    private FragmentManager mFragmentManager;
    private SearchTaskFragment mTaskFragment;

    public String getQuery() {
        return mQuery;
    }

    public int getPage() {
        return mPage;
    }

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

        mPage = 1;
        mButtonPrevious = (FloatingActionButton) findViewById(R.id.button_previous);
        mButtonNext = (FloatingActionButton) findViewById(R.id.button_next);

        mButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

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
                return false;
            }
        });

        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                // do some magic
            }

            @Override
            public void onSearchViewClosed() {
                if (mKeyboardIsVisible) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateButtonsVisibility();
                        }
                    }, HIDE_KEYBOARD_DELAY); // giving it appropriate delay to hide the keyboard
                } else {
                    updateButtonsVisibility();
                }
            }
        });

        mListView = (ListView) findViewById(R.id.search_list);
        mFragmentManager = getFragmentManager();
        mTaskFragment = (SearchTaskFragment) mFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);

//        if (mTaskFragment == null) {
//            mTaskFragment = SearchTaskFragment.newInstance(mQuery);
//            mFragmentManager.beginTransaction().add(mTaskFragment, SEARCH_FRAGMENT_TAG).commit();
//        }
//        mSuggestions = new SearchRecentSuggestions(this,
//                RecipeSuggestionProvider.AUTHORITY, RecipeSuggestionProvider.MODE);

        // Get the intent, verify the action and get the query
//        handleIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        //  Credits: Alex Lockwood (http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html)
        if (mAdapter != null) {
            ArrayList<Recipe> recipes = mAdapter.getRecipes();
            state.putParcelableArrayList("recipes", recipes);
            state.putString("mQuery", mQuery);
            state.putInt("mCount", mCount);
            state.putInt("mPage", mPage);
        }
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        //  Credits: Alex Lockwood (http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html)
        super.onRestoreInstanceState(state);
        ArrayList<Recipe> recipes = state.getParcelableArrayList("recipes");
        mQuery = state.getString("mQuery");
        mCount = state.getInt("mCount");
        mPage = state.getInt("mPage");
        mFragmentManager = getFragmentManager();
        mTaskFragment = (SearchTaskFragment) mFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);

        if (recipes != null) {
            loadListView(recipes);
        }
        updateButtonsVisibility();
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mKeyboardIsVisible = false;
            mSearchView.closeSearch();
        } else if (mButtonPrevious.getVisibility() == View.VISIBLE) {
            previous();
        } else if (mHistory.sizeOf() > 1) {
            getPreviousSearchResult(Integer.MIN_VALUE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        mSearchView.setMenuItem(item, new MenuItemClickListener() {
            @Override
            public void onClick() {
                mSearchView.setSuggestions(mHistory.getHistoryCache());
                mButtonPrevious.setVisibility(View.GONE);
                mButtonNext.setVisibility(View.GONE);
                mSearchView.setQuery(mQuery, false);
                mKeyboardIsVisible = true; // setMenuItem() will invoke showSearch() which will request focus on the text view
            }
        });

        return super.onCreateOptionsMenu(menu);
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

    private void search(String query, int page) {
        query = query.trim();
        if (query.equals(mQuery) && page == mPage) {
            return;
        }

        mPage = page;
        mQuery = query;
        SearchResult result = Utility.getSearchResult(Utility.getHashCode(mQuery, mPage));
        if (result != null) {
            updateListViewWithResult(result);   // this should update the count
            updateButtonsVisibility(); // this should be called after count has been updated
\        } else {
            mHistory.push(mQuery);
            try {
                query = URLEncoder.encode(query, ServerUtility.ENCODING);
            } catch (UnsupportedEncodingException e) {
                // do not do any encoding
            }
            query = Utility.QUERY_PARAM + query;
            if (page > 1) {
                query += Utility.PAGE_PARAM + page;
            } else {
                mButtonPrevious.setVisibility(View.GONE);
            }

            mTaskFragment = SearchTaskFragment.newInstance(query);
            mFragmentManager.beginTransaction().add(mTaskFragment, SEARCH_FRAGMENT_TAG).commit();
        }
    }

    // we use page parameter here so that we remember to update mPage before calling this method
    private void getPreviousSearchResult(int page) {
        // if page equals minimum value, back button has been pressed
        if (page == Integer.MIN_VALUE) {
            Utility.removeSearchResult(Utility.getHashCode(mHistory.pop(), mPage)) ; // pop the most recent
            mQuery = mHistory.pop(); // pop the previous
            mPage = 1;
        } else { // should simply be going back one page
            mPage = page;
        }
        SearchResult result = Utility.getSearchResult(Utility.getHashCode(mQuery, mPage));
        if (result != null) {
            updateListViewWithResult(result);
        } else {
            // the implementation of setQuery does its own null and emptiness checks
            mSearchView.setQuery(mQuery, true); // this will perform a new search, which will push current query onto the history stack
        }
    }

    private void updateListViewWithResult(SearchResult result) {
        mPage = result.getPage();
        mQuery = result.getSearchTerm(); // should not be necessary
        Utility.setBitmapMemCache(result.getMemoryCache());
        mAdapter = result.getAdapter();
        mAdapter.notifyDataSetChanged();
        mCount = mAdapter.getCount();
        mListView.setAdapter(mAdapter);
        mListView.refreshDrawableState();
//            mHistory.push(mQuery); // push the previous (which is now the current) query back onto the stack
    }

    private void updateButtonsVisibility() {
        if (mPage > 1) {
            mButtonPrevious.setVisibility(View.VISIBLE);
        } else {
            mButtonPrevious.setVisibility(View.GONE);
        }
        if (mCount == MAX_COUNT) {
            mButtonNext.setVisibility(View.VISIBLE);
        } else {
            mButtonNext.setVisibility(View.GONE);
        }
    }

    private void loadListView(final List<Recipe> recipes) {
        if (mQuery == null) {
            return;
        }
        mAdapter = new RecipeListAdapter(this, recipes);
        Utility.resetBitmapCache(); // clear the cache before loading new images and new adapter
        for (Recipe r : recipes) {
            r.loadImage(mAdapter);
        }

        mAdapter.setOnImagesLoadedListener(new OnImagesLoadedListener() {
            @Override
            public void imagesLoaded(SearchState state) {  // at this point cache shall contain the (new) images for this query
                String hashCode = Utility.getHashCode(state.getQuery(), state.getPage());
                if (!Utility.resultHistoryContainsResult(hashCode)) {
                    saveSearchResult(new SearchResult(state.getAdapter(), state.getCache(), state.getQuery(), state.getPage()));
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                     if(!mAdapter.getRecipes().isEmpty() && mAdapter.getRecipes().get(position) != null) {
                         Bundle extras = new Bundle();
                         extras.putSerializable(Utility.RECIPE, mAdapter.getRecipes().get(position));
                         navigateToActivity(RecipeActivity.class, extras);
                     }
                 }
             }
        );
    }

    private void saveSearchResult(SearchResult searchResult) {
        Utility.addSearchResult(searchResult);
    }

    private void previous() {
        mPage--;
        updateButtonsVisibility();
        getPreviousSearchResult(mPage);
//        search(mQuery, page);
    }

    private void next() {
        int page = mPage + 1; // keep this in order to prevent duplicate requests ->
                              // search method will compare given page against current

        // try fetching search result from cache
        SearchResult result = Utility.getSearchResult(Utility.getHashCode(mQuery, page));
        if (result != null) {
            mPage = page; // update the page too
            updateButtonsVisibility();
            updateListViewWithResult(result);
        } else {
            search(mQuery, page);
        }
    }


    // SearchTaskFragment.TaskCallbacks implementation beyond this comment

    @Override
    public void onPreExecute() {
        mProgressDialog = ProgressDialog.show(SearchActivity.this, "", getResources().getString(R.string.please_wait));
    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(final List<Recipe> result) {
        if(!SearchActivity.this.isDestroyed()) {
            if (mProgressDialog != null) mProgressDialog.dismiss();

            if (mTaskFragment.getException() == null) {
                if (!result.isEmpty()){
                    if (mListView == null) mListView = (ListView) findViewById(R.id.search_list);
                    mCount = mTaskFragment.getCount();
                    loadListView(result);
                    mSearchView.clearFocus();
                    updateButtonsVisibility();
                } else {
                    showToast(R.string.no_results);
                }
                mSearchView.setQuery(mQuery, false);
            } else {
                SearchActivity.this.displayMessage(exception);
            }
        }
    }

    // unused methods beyond this comment

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_search) {
//            mSearchView.requestFocus();
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public boolean onSearchRequested() {
//        handleIntent(getIntent());
//        return super.onSearchRequested();
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        setIntent(intent);
//        handleIntent(intent);
//    }

//    private void handleIntent(Intent intent) {
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            mSuggestions.saveRecentQuery(query, null);
//            search(query, mPage);
//        }
//    }

}