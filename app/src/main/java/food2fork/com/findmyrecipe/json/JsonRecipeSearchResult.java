package food2fork.com.findmyrecipe.json;

import java.util.List;

import food2fork.com.findmyrecipe.Recipe;

/**
 * Created by ai on 07.09.2017.
 */

public class JsonRecipeSearchResult extends BaseJson {
    private int count;
    private List<JsonRecipe> recipes;

    public JsonRecipeSearchResult () {

    }

    public List<JsonRecipe> getRecipes(){
        return recipes;
    }

    public int getCount() {
        return count;
    }
}
