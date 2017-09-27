package food2fork.com.findmyrecipe;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author Alexei Ivanov
 * unused
 */
public class RecipeSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.food2fork.findmyrecipe.RecipeSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecipeSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
