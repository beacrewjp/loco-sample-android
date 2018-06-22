package jp.beacrew.locotutorial;

import android.app.Application;

/**
 * アプリのバックグラウンド・フォアグラウンドを判定する為にAppliationClassを使用
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }

}
