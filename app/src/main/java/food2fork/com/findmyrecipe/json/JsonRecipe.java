package food2fork.com.findmyrecipe.json;

/**
 * Created by ai on 07.09.2017.
 */

public class JsonRecipe extends BaseJson {

    private String publisher,f2f_url,publisher_url,title,source_url,recipe_id,image_url;
    private String[] ingredients;
    private double social_rank;
    private int page;

    public JsonRecipe() {

    }

    public String getPublisher() {
        return publisher;
    }

    public String getF2f_url() {
        return f2f_url;
    }

    public String getPublisher_url() {
        return publisher_url;
    }

    public String getTitle() {
        return title;
    }

    public String getSource_url() {
        return source_url;
    }

    public String getRecipe_id() {
        return recipe_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public double getSocial_rank() {
        return social_rank;
    }

    public int getPage() {
        return page;
    }
}
