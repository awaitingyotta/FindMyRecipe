package food2fork.com.findmyrecipe;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;
import food2fork.com.findmyrecipe.json.JsonRecipeSearchResult;
import food2fork.com.findmyrecipe.utils.ServerUtility;
import food2fork.com.findmyrecipe.utils.Utility;

/**
 * @author Alexei Ivanov
 * Credits: Alex Lockwood (http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html)
 */
public class SearchTaskFragment extends Fragment {

    private int mCount;
    private Exception mException;
    private SearchTask mTask;
    private TaskCallbacks mCallbacks;

    public interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(List<Recipe> result);
    }

    public static SearchTaskFragment newInstance(String query) {
        SearchTaskFragment fragment = new SearchTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
        fragment.setArguments(bundle);
        return fragment;
    }

    public Exception getException() {
        return mException;
    }

    public int getCount() {
        return mCount;
    }


    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String query;
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        Bundle arguments = getArguments();
        if (arguments != null) {
            query = arguments.getString("query");
            if (query != null && !query.isEmpty()) {
                // Create and execute the background task.
                mTask = new SearchTask();
                mTask.execute(query);
            }
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class SearchTask extends AsyncTask<String, Integer, List<Recipe>> {


        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected List<Recipe> doInBackground(String... params) {
            List<Recipe> recipes;
            JsonRecipeSearchResult searchResult;
            try {
                String query = params[0];
                searchResult = ServerUtility.searchForRecipes(query);
                recipes = Utility.mapRecipes(searchResult.getRecipes());
                mCount = searchResult.getCount();
                return recipes;
            } catch (AppErrorException e) {
                mException = e;
                return null;
            } catch (ServerFaultException e) {
                mException = e;
                return null;
            } catch (NetworkErrorException e) {
                mException = e;
                return null;
            } catch (NullPointerException e) {
                mException = e;
                return null;
            } catch (Exception e) { // for params exceptions
                mException = e;
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<Recipe> result) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(result);
            }
        }
    }
}
