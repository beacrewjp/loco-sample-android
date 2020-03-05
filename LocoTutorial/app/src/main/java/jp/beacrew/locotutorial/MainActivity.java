package jp.beacrew.locotutorial;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import jp.beacrew.loco.BCLAction;
import jp.beacrew.loco.BCLBeacon;
import jp.beacrew.loco.BCLError;
import jp.beacrew.loco.BCLManager;
import jp.beacrew.loco.BCLManagerEventListener;
import jp.beacrew.loco.BCLParam;
import jp.beacrew.loco.BCLRegion;
import jp.beacrew.loco.BCLState;

public class MainActivity extends AppCompatActivity implements BCLManagerEventListener{

    private BCLManager mBclmanager;
    private String APIKEY ="ENTER YOUR SDK SECRET";

    private AlertDialog dialog;
    private static Dialog mDialog;
    private boolean webDialogFlg = false;
    private MyNotification myNotification;
    private ImageView imgMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        myNotification = new MyNotification(getApplicationContext());

        BCLManager.setUseForegroundService(true);
        mBclmanager = BCLManager.getInstance(getApplicationContext());
        mBclmanager.setListener(this);
        mBclmanager.initWithApiKey(APIKEY,true);

        /**
         * infoボタンのイベントハンドラ
         * インフォメーションを表示します。
         */
        imgMenu = (ImageView)findViewById(R.id.img_menu);
        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LocoTutorial", "onResume");

    }

    @Override
    protected void onStart() {
        super.onStart();
        //位置情報の許可を取得しているかチェックします
        permissionCheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LocoTutorial", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LocoTutorial", "onDestroy");
        //LocoSDKを正しく終了させる為に呼び出します
        mBclmanager.terminateService();
    }

    /**
     * LocoSDKのステータスが変化すると呼ばれます
     * @param bclState　現在のステータス
     */
    @Override
    public void onStateChange(BCLState bclState) {
        if (bclState.equals(BCLState.SCANNING)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDialog = beaconSearchDialog(MainActivity.this);
                    mDialog.show();
                }
            });

        }
    }

    /**
     * LocoSDKがビーコンを検知（レンジング）すると呼ばれます
     * @param bclBeacons　検知したビーコンの情報
     */
    @Override
    public void onBeaconDetected(final ArrayList<BCLBeacon> bclBeacons) {
    }

    /**
     * LocoSDKがActionを検知すると呼ばれます
     * @param bclAction アクションの情報
     * @param s アクションのType（Beacon、RegionIn、RegionOut）
     * @param o アクションの発生オブジェクト（BCLBeacon、BCLRegion）
     */
    @Override
    public void onActionDetected(final BCLAction bclAction, String s, Object o) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ArrayList<BCLParam> bclParams = bclAction.getParams();
                for (BCLParam bclParam : bclParams) {
                    if (bclParam.getKey().equals("type")) {
                        if (bclParam.getValue().equals("web")) {
                            showWebDialog(bclParams);

                        } else if (bclParam.getValue().equals("push")) {
                            if(!MyLifecycleHandler.isForeground()) {
                                actionMessage(bclParams);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * LocoSDKがビーコンやGPSのリージョンに進入した際に呼ばれます
     * @param bclRegion　進入したリージョンの情報
     */
    @Override
    public void onRegionIn(final BCLRegion bclRegion) {

    }

    /**
     * LocoSDKがビーコンやGPSのリージョンから退出した際に呼ばれます
     * @param bclRegion　退出したリージョンの情報
     */
    @Override
    public void onRegionOut(final BCLRegion bclRegion) {
    }

    /**
     * LocoSDKがエラーを検知した際に呼ばれます
     * @param bclError　LocoSDKからのエラー
     */
    @Override
    public void onError(final BCLError bclError) {
    }

    private void permissionCheck() {

        if (Build.VERSION.SDK_INT >= 23) {

            boolean permissionAccessFineLocationApproved =
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (permissionAccessFineLocationApproved) {

                if (Build.VERSION.SDK_INT >= 29) {
                    boolean backgroundLocationPermissionApproved =
                            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED;

                    if (backgroundLocationPermissionApproved) {
                        // App can access location both in the foreground and in the background.
                        // Start your service that doesn't have a foreground service type
                        // defined.
                    } else {
                        // App can only access location in the foreground. Display a dialog
                        // warning the user that your app must have all-the-time access to
                        // location in order to function properly. Then, request background
                        // location.
                        requestPermissions(new String[]{
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                1000);
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= 29) {
                    // App doesn't have access to the device's location at all. Make full request
                    // for permission.
                    requestPermissions(new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            },
                            1000);
                } else {
                    requestPermissions(new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            1000);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1000) {

            if (Build.VERSION.SDK_INT >= 29) {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // success!
                    Log.d("LocoTutorial", "Backgroundパーミッション許可");

                } else {
                    // Permission was denied or request was cancelled
                    Log.d("LocoTutorial", "Backgroundパーミッション拒否");
                    new AlertDialog.Builder(this)
                            .setTitle("注意")
                            .setMessage("本アプリは位置情報を常に使用します、設定画面から位置情報の許可を常に使用するにして下さい")
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            // システムのアプリ設定画面
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    })
                            .show();
                }
            } else {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // success!
                    Log.d("LocoTutorial", "パーミッション許可");

                } else {
                    // Permission was denied or request was cancelled
                    Log.d("LocoTutorial", "パーミッション拒否");
                    new AlertDialog.Builder(this)
                            .setTitle("注意")
                            .setMessage("本アプリは位置情報を常に使用します、設定画面から位置情報の許可を常に使用するにして下さい")
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            // システムのアプリ設定画面
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    })
                            .show();
                }
            }
        }
    }

    private Dialog beaconSearchDialog(final Activity activity) {

        Dialog dialog = new Dialog(activity,R.style.transparent);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount=0.0f;
        lp.y = 50;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(R.layout.simple_progress);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    activity.finish();
                }
                return false;
            }
        });
        dialog.setCancelable(false);
        return dialog;
    }

    private void showWebDialog(ArrayList<BCLParam> bclParams) {

        for (BCLParam bclParam : bclParams) {
            if (bclParam.getKey().equals("page")) {

                if (webDialogFlg == true) {
                    dialog.dismiss();
                    dialog = null;
                }

                final String uri = bclParam.getValue();
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(bclParam.getValue());
                builder.setMessage("製品カタログを表示します。");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //EventLogを作成します。
                        mBclmanager.addEventLog("Open", uri);
                        webDialogFlg = false;

                        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                        intent.putExtra("URI", uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        webDialogFlg = false;
                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getAction() == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                            webDialogFlg = false;
                        }
                        return false;
                    }
                });

                dialog = builder.create();
                dialog.show();
                webDialogFlg = true;
            }
        }
    }

    private void actionMessage(ArrayList<BCLParam> bclParams) {

        for (BCLParam bclParam : bclParams) {
            if (bclParam.getKey().equals("message")) {
                myNotification.actionNotification(bclParam.getValue());
            }
        }

    }
}
