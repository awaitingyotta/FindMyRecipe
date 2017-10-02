package food2fork.com.findmyrecipe;

/**
 * @author Alexei Ivanov
 */
public interface OnImagesLoadedListener {
    void imagesLoaded(SearchState state); // we need to pass a new reference to the image cache
}
