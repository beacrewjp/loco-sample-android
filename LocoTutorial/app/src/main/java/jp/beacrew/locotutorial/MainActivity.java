package jp.beacrew.locotutorial;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import java.util.ArrayList;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.beacrew.loco.BCLAction;
import jp.beacrew.loco.BCLBeacon;
import jp.beacrew.loco.BCLError;
import jp.beacrew.loco.BCLInitState;
import jp.beacrew.loco.BCLManager;
import jp.beacrew.loco.BCLManagerEventListener;
import jp.beacrew.loco.BCLParam;
import jp.beacrew.loco.BCLRegion;

public class MainActivity extends AppCompatActivity implements BCLManagerEventListener{

    private static String uri;
    private BCLManager mBclmanager;
    private String APIKEY ="ENTER YOUR SDK SECRET";
    private AlertDialog dialog;
    private static Dialog mDialog;
    private boolean webDialogFlg = false;
    private MyNotification myNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        myNotification = new MyNotification(getApplicationContext());
        ButterKnife.bind(this);

        mBclmanager = new BCLManager(getApplicationContext());
        mBclmanager.setListener(this);
        mBclmanager.initWithApiKey(APIKEY,true);
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
     * @param bclInitState　現在のステータス
     */
    @Override
    public void onStateChange(final BCLInitState bclInitState) {

        if (bclInitState.equals(BCLInitState.SCANNING)) {
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
     * @param bclAction　アクションの情報
     */
    @Override
    public void onActionDetected(final BCLAction bclAction) {
        if(MyLifecycleHandler.isForeground()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (final BCLParam bclParam : bclAction.getParams()) {
                        if (bclParam.getKey().equals("page")) {
                            if (webDialogFlg == true) {
                                dialog.dismiss();
                                dialog = null;
                            }
                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(bclParam.getValue());
                            builder.setMessage("製品カタログを表示します。");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //EventLogを作成します。
                                    mBclmanager.addEventLog("Open", bclAction.getParams().get(0).getValue());
                                    webDialogFlg = false;
                                    uri = bclParam.getValue();

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
            });
        }
    }

    /**
     * LocoSDKがビーコンやGPSのリージョンに進入した際に呼ばれます
     * @param bclRegion　進入したリージョンの情報
     */
    @Override
    public void onRegionIn(final BCLRegion bclRegion) {
        if (!MyLifecycleHandler.isForeground()) {
            myNotification.regionNotification(bclRegion);
        }
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


    /**
     * infoボタンのイベントハンドラ
     * インフォメーションを表示します。
     */
    @OnClick(R.id.img_menu)
    public void onMenuClick() {
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        startActivity(intent);
    }


    private void permissionCheck() {
        if (Build.VERSION.SDK_INT < 23) {
            permissionCheckResult(
                    2, new int[]{PackageManager.PERMISSION_GRANTED});
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request missing location permission.
                Log.d("LocoTutorial", "パーミッションなし");
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);


                }
            } else {
                // Location permission has been granted, continue as usual.
                Log.d("LocoTutorial", "パーミッションあり");
                permissionCheckResult(
                        2, new int[]{PackageManager.PERMISSION_GRANTED});
            }
        }
    }

    public void permissionCheckResult(int requestCode, int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocoTutorial", "パーミッション許可");

            } else {
                Log.d("LocoTutorial", "パーミッション拒否");
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
