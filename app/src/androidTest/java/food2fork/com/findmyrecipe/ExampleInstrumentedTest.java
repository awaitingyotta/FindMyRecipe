package food2fork.com.findmyrecipe;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import food2fork.com.findmyrecipe.json.JsonRecipeDetails;
import food2fork.com.findmyrecipe.json.JsonRecipeSearchResult;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("food2fork.com.findmyrecipe", appContext.getPackageName());
    }

    @Test
    public void findRecipes() throws Exception {
        // this test will try to find recipes with beef
        // the search is expected to yield 30 results

        List<Recipe> recipes = null;
        JsonRecipeSearchResult result;
        String query =  Utility.QUERY_PARAM + "beef";

        try {
            result = ServerUtility.searchForRecipes(query);
            recipes = Utility.mapRecipes(result.getRecipes());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            return;
        }

        assertTrue(result.getCount() == 30); // this shows that the search was successful
        assertTrue(recipes.size() == 30); // this shows that the mapping was successful
    }

    @Test
    public void downloadRecipe() throws Exception {
        // this test will try to download recipe #35107 and check the following:
        //      - the name of this recipe
        //      - that the image url is correct
        // used recipe #47078 to verify that this test would fail given another recipe than expected

        Recipe recipe = null;
        JsonRecipeDetails result;
        int rId = 35107; //47078

        try {
            String query =  Utility.RECIPE_ID_PARAM + rId;
            result = ServerUtility.getRecipe(query);
            recipe = Utility.convertRecipe(result.getRecipe());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            return;
        }

        assertEquals("Bacon Double Cheese Burger Dip", recipe.getTitle());
        assertEquals("http://static.food2fork.com/Bacon2BDouble2BCheese2BBurger2BDip2B5002B3557cdaa745d.jpg", recipe.getImageUrl());
    }
}
