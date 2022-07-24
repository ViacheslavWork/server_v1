package com.template

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

const val WEB_ACTIVITY_URL = "com.template.WEB_ACTIVITY_URL"
private const val webViewStateKey = "webViewState"

class WebActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)



        webView = findViewById(R.id.web_view)
        if (savedInstanceState != null)
            savedInstanceState.getBundle(webViewStateKey)?.let { webView.restoreState(it) };
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView
            .apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true
            }
        intent.getStringExtra(WEB_ACTIVITY_URL)?.let { webView.loadUrl(it) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
//        return super.onKeyDown(keyCode, event)
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()
        webView.saveState(bundle)
        outState.putBundle(webViewStateKey, bundle)
    }

}