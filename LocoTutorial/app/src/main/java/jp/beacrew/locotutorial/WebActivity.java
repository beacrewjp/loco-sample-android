package jp.beacrew.locotutorial;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * webコンテンツを表示する為のFragment
 */
public class WebActivity extends Activity {
    private WebView webView;
    private String mUri = "";
    private ImageView imgWebMenu;
    private ImageView imgWebHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webactivity);
        Intent intent = getIntent();
        mUri = intent.getStringExtra("URI");

        imgWebMenu = (ImageView)findViewById(R.id.img_webmenu);
        imgWebMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                intent.putExtra("WebActivity", true);
                startActivity(intent);
            }
        });

        imgWebHome = (ImageView)findViewById(R.id.img_webhome);
        imgWebHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView = (WebView)findViewById(R.id.webcontent);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            // 新しいURLが指定されたときの処理を定義
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return false;
            }

            // ページ読み込み開始時の処理
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            // ページ読み込み完了時
            @Override
            public void onPageFinished(WebView view, String url) {

            }

            // エラー時
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String url) {
                Toast.makeText(getApplicationContext(), "通信エラー", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }


        });
        webView.loadUrl("file:///android_asset/html/" + mUri + ".html");
    }
}
