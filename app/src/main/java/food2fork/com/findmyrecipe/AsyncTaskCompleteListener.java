package food2fork.com.findmyrecipe;

import android.graphics.Bitmap;
import java.io.Serializable;

interface AsyncTaskCompleteListener extends Serializable {

    static final long serialVersionUID = 1L;

    void onSuccess(Bitmap image);
    void onFailure();

}
