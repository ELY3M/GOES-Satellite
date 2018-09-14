package own.goessatellite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import com.koushikdutta.async.http.body.StringBody;

public class MyWebView extends ActionBarActivity {
    static boolean verbose = false;
    Context ctxt;
    WebView mWebView;
    String url;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ctxt = this;
        setContentView(R.layout.webview);
        setTitle(R.string.app_name);
        this.url = getIntent().getStringExtra("own.goessatellite.webview.url");

        Log.d("WEBVIEW", "Webview>" + this.url);

        this.mWebView = (WebView) findViewById(R.id.webview);
        this.mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = this.mWebView.getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        this.mWebView.setLongClickable(true);
        this.mWebView.setWebViewClient(new WebViewClient());
        this.mWebView.loadUrl(this.url);
        getSupportActionBar().setTitle("Web viewer");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webviewmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.webview_share) {
            doShare();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doShare() {
        WebView webview = this.mWebView;
        HitTestResult result = webview.getHitTestResult();
        String url = webview.getUrl();
        Intent i = new Intent("android.intent.action.SEND");
        i.setType(StringBody.CONTENT_TYPE);
        i.putExtra("android.intent.extra.SUBJECT", "Shared URL from "+ R.string.app_name);
        i.putExtra("android.intent.extra.TEXT", url.replace("%3A", ":"));
        startActivity(Intent.createChooser(i, "Share URL"));
    }


}
