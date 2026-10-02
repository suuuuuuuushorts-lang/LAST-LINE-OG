package com.lastline.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

  private WebView web;
  private ValueCallback<Uri[]> filePathCallback;
  private static final int FILE_CHOOSER_REQUEST_CODE = 42;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    web = new WebView(this);
    web.setBackgroundColor(Color.BLACK);

    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setUseWideViewPort(true);
    s.setLoadWithOverviewMode(true);
    s.setMediaPlaybackRequiresUserGesture(false);

    web.setWebViewClient(new WebViewClient());
    web.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onShowFileChooser(WebView webView,
          ValueCallback<Uri[]> filePathCallback,
          FileChooserParams fileChooserParams) {
        // cancel any previous pending file chooser
        if (MainActivity.this.filePathCallback != null) {
          MainActivity.this.filePathCallback.onReceiveValue(null);
        }
        MainActivity.this.filePathCallback = filePathCallback;
        try {
          Intent intent = fileChooserParams.createIntent();
          startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException e) {
          MainActivity.this.filePathCallback = null;
          return false;
        }
        return true;
      }
    });
    web.loadUrl("file:///android_asset/index.html");
    setContentView(web);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
      Uri[] result = null;
      if (resultCode == RESULT_OK && data != null) {
        if (data.getClipData() != null) {
          int n = data.getClipData().getItemCount();
          result = new Uri[n];
          for (int i = 0; i < n; i++) {
            result[i] = data.getClipData().getItemAt(i).getUri();
          }
        } else if (data.getData() != null) {
          result = new Uri[]{data.getData()};
        }
      }
      if (filePathCallback != null) {
        filePathCallback.onReceiveValue(result);
        filePathCallback = null;
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (web != null && web.canGoBack()) {
      web.goBack();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onDestroy() {
    if (web != null) web.destroy();
    super.onDestroy();
  }
}
