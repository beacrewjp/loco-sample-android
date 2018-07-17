package jp.beacrew.locotutorial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoActivity extends Activity {
    private Boolean showingWebActivity = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infoactivity);

        ButterKnife.bind(this);
        Intent intent = getIntent();
        showingWebActivity = intent.getBooleanExtra("WebActivity", false);

    }

    @OnClick(R.id.img_infomenu)
    public void onMenuClick() {
        finish();
    }

    @OnClick(R.id.img_infohome)
    public void onHomeClick() {
        if (showingWebActivity) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            finish();
        }
    }
}
