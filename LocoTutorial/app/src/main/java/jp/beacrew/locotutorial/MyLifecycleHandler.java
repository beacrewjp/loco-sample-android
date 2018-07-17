package jp.beacrew.locotutorial;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static boolean isForeground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity) {
            isForeground = true;
        } else if (activity instanceof WebActivity) {
            isForeground = true;
        } else if (activity instanceof InfoActivity) {
            isForeground = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof MainActivity) {
            isForeground = false;
        } else if (activity instanceof WebActivity) {
            isForeground = false;
        } else if (activity instanceof InfoActivity) {
            isForeground = false;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * アプリが前面にいるかどうかを取得します.
     * @return Foregroundにいたらtrue,backgroundにいたらfalseをかえします
     */
    public static boolean isForeground() {
        return isForeground;
    }


}
