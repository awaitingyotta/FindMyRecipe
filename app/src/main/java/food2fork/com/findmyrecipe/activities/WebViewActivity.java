package food2fork.com.findmyrecipe.activities;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import food2fork.com.findmyrecipe.R;
import food2fork.com.findmyrecipe.Utility;

public class WebViewActivity extends BaseActivity {
    private WebView mInfoWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Bundle extras = getIntent().getExtras();
        String url = extras.getString(Utility.URL);
        String title = extras.getString(Utility.TITLE);

        if (url == null || url.isEmpty()) {
            showToast(R.string.url_error);
        } else {
            if (title != null && !title.isEmpty()) {
                setTitle(title);
            }

            mProgressBar = (ProgressBar) findViewById(R.id.web_view_activity_progress_bar);
            mInfoWebView = (WebView) findViewById(R.id.web_view_page);
            mInfoWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    super.onPageStarted(view, url, favicon);
                }
                @Override
                public void onPageFinished(WebView view, String url){
                    super.onPageFinished(view, url);
                    mProgressBar.setVisibility(View.GONE);
                    mInfoWebView.clearCache(true);
                }
                @Override
                public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                    super.onReceivedSslError(view,handler,error);
                }
            });

            mInfoWebView.getSettings().setAllowFileAccess(false);
            mInfoWebView.getSettings().setJavaScriptEnabled(true); // to make pages look better

            mInfoWebView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (mInfoWebView.canGoBack()) {
            mInfoWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

}
