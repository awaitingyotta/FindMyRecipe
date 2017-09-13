package food2fork.com.findmyrecipe.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Field;

import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;

public class BaseActivity extends AppCompatActivity {

    private InputMethodManager mInputMethodManager;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        mActionBar = getSupportActionBar();
        if (mActionBar != null && this.getClass() != SearchActivity.class){
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mActionBar = getSupportActionBar();
        if (mActionBar == null) return;
        if (title == null || title.length() == 0) {
            mActionBar.setTitle(this.getTitle());
        }
        else {
            mActionBar.setTitle(title);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    protected void navigateToActivity(Class<? extends BaseActivity> activityClass, Bundle extras) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtras(extras);
        startActivity(intent);
    }

    protected void displayMessage(Exception exception) {
        if (exception.getClass().equals(ServerFaultException.class) || exception.getClass().equals(NetworkErrorException.class)) {
            exception.printStackTrace();
            showToast(R.string.server_or_network_error);
        } else if (exception.getClass().equals(AppErrorException.class)) {
            exception.printStackTrace();
            showToast(R.string.app_error);
        } else if (exception.getClass().equals(NullPointerException.class)) {
            exception.printStackTrace();
            showToast(R.string.no_results);
        } else {
            exception.printStackTrace();
            showToast(R.string.unknown_error);
        }
    }

    protected void showToast(int resourceId) {
        Toast toast = Toast.makeText(getApplicationContext(), resourceId, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout text = (LinearLayout) toast.getView();
        text.setGravity(Gravity.CENTER);
        toast.setView(text);
        toast.show();
    }

    protected void showOrHideKeyboard(View view, boolean show) {
        if (view == null) view = this.getCurrentFocus();
        if (mInputMethodManager == null) mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(view != null && mInputMethodManager != null) {
            if (show) {
                mInputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
            else {
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
