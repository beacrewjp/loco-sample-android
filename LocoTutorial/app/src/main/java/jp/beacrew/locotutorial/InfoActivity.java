package jp.beacrew.locotutorial;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoActivity extends Activity {
    private Boolean showingWebActivity = false;
    private ImageView imgInfoMenu;
    private ImageView imgInfoHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infoactivity);

        Intent intent = getIntent();
        showingWebActivity = intent.getBooleanExtra("WebActivity", false);

        imgInfoMenu = (ImageView)findViewById(R.id.img_infomenu);
        imgInfoMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgInfoHome = (ImageView)findViewById(R.id.img_infohome);
        imgInfoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showingWebActivity) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });

    }
}
