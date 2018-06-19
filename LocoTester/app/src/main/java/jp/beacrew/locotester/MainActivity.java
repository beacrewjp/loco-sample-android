package jp.beacrew.locotester;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.beacrew.loco.BCLAction;
import jp.beacrew.loco.BCLBeacon;
import jp.beacrew.loco.BCLCluster;
import jp.beacrew.loco.BCLError;
import jp.beacrew.loco.BCLInitState;
import jp.beacrew.loco.BCLManager;
import jp.beacrew.loco.BCLManagerEventListener;
import jp.beacrew.loco.BCLRegion;

public class MainActivity extends Activity implements BCLManagerEventListener{

    private TextView txt_logs;
    private ScrollView infoScarollView;
    private BCLManager mBclmanager;
    private String APIKEY ="ENTER YOUR SDK SECRET";
    static final String BR = System.getProperty("line.separator");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        txt_logs = findViewById(R.id.txt_logs);
        infoScarollView = findViewById(R.id.logs_scroll);
        mBclmanager = new BCLManager(getApplicationContext());
        mBclmanager.setListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //LocoSDKを正しく終了させる為に呼び出します
        mBclmanager.terminateService();
    }

    @OnClick(R.id.img_pin)
    public void onPinClick() {
        final EditText editView = new EditText(this);
        editView.setText(APIKEY);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("APIKEYを入力してください");
        dialog.setView(editView);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                APIKEY = String.valueOf(editView.getText());
            }
        });

        dialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        dialog.show();

    }

    /**
     * LocoSDKの初期化を開始します
     */
    @OnClick(R.id.img_init)
    public void onInitClick() {
        if (mBclmanager.getState() != BCLInitState.INITIALIZED) {
            mBclmanager.initWithApiKey(APIKEY, false);
        }
    }

    /**
     * ビーコンのスキャンを開始します
     */
    @OnClick(R.id.img_start)
    public void onStartClick() {
        txt_logs.append(getNowDate() + "  ScanStart" + BR);
        mBclmanager.scanStart();
    }

    /**
     * ビーコンのスキャンを停止します
     */
    @OnClick(R.id.img_stop)
    public void onStopClick() {
        txt_logs.append(getNowDate() + "  ScanStop" + BR);
        mBclmanager.scanStop();
    }

    /**
     * LocoSDKの初期化によって得られた情報（Locoダッシュボードで入力されたデータ）を表示します
     */
    @OnClick(R.id.img_info)
    public void onInfoClick() {
        final String[] items = {"DeviceID", "Nearest BeaconID", "Beacons", "Clusters", "Regions", "Actions"};
        int defaultItem = 0; // デフォルトでチェックされているアイテム
        final List<Integer> checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        new AlertDialog.Builder(this)
                .setTitle("Selector")
                .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItems.clear();
                        checkedItems.add(which);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!checkedItems.isEmpty()) {
                            Log.d("checkedItem:", "" + checkedItems.get(0));
                            switch (checkedItems.get(0)) {
                                case 0:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txt_logs.append(getNowDate() + "  [DeviceID]  \n" + mBclmanager.getDeviceId() + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 1:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txt_logs.append(getNowDate() + "  [Nearest BeaconID]  \n" + mBclmanager.getNearestBeaconId() + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 2:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                            ArrayList<BCLBeacon> bclBeacons = mBclmanager.getBeacons();
                                            String beacons = gson.toJson(bclBeacons);

                                            txt_logs.append(getNowDate() + "  [Beacons]  \n" + beacons + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 3:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                            ArrayList<BCLCluster> bclClusters = mBclmanager.getClusters();
                                            String clusters = gson.toJson(bclClusters);

                                            txt_logs.append(getNowDate() + "  [Clusters]  \n" + clusters + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 4:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                            ArrayList<BCLRegion> bclRegions = mBclmanager.getRegions();
                                            String regions = gson.toJson(bclRegions);

                                            txt_logs.append(getNowDate() + "  [Regions]  \n" + regions + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 5:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                            ArrayList<BCLAction> bclActions = mBclmanager.getActions();
                                            String actions = gson.toJson(bclActions);

                                            txt_logs.append(getNowDate() + "  [Actions]  \n" + actions + "\n");
                                            infoScarollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;

                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * LocoSDKのステータスが変化すると呼ばれます
     * @param bclInitState　現在のステータス
     */
    @Override
    public void onStateChange(final BCLInitState bclInitState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#ffffff\">" + getNowDate() + "  State  " + bclInitState.toString() + "</font><br>");
                txt_logs.append(csHTML);
            }
        });

    }

    /**
     * LocoSDKがビーコンを検知（レンジング）すると呼ばれます
     * @param bclBeacons　検知したビーコンの情報
     */
    @Override
    public void onBeaconDetected(final ArrayList<BCLBeacon> bclBeacons) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (BCLBeacon bclBeacon : bclBeacons) {
                    CharSequence csHTML = Html.fromHtml("<font color=\"#ffffff\">" + getNowDate()  + "  Ranging  " + bclBeacon.getName() + "</font><br>");
                    txt_logs.append(csHTML);
                    infoScarollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        });


    }

    /**
     * LocoSDKがActionを検知すると呼ばれます
     * @param bclAction　アクションの情報
     */
    @Override
    public void onActionDetected(final BCLAction bclAction) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#ffff00\">" + getNowDate()  + "  Action  " +  bclAction.getName() + "</font><br>");
                txt_logs.append(csHTML);
                infoScarollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    /**
     * LocoSDKがビーコンやGPSのリージョンに進入した際に呼ばれます
     * @param bclRegion　進入したリージョンの情報
     */
    @Override
    public void onRegionIn(final BCLRegion bclRegion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#00ff00\">" + getNowDate()  + "  RegionIn  " + bclRegion.getName() + "</font><br>");

                txt_logs.append(csHTML);
                infoScarollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    /**
     * LocoSDKがビーコンやGPSのリージョンから退出した際に呼ばれます
     * @param bclRegion　退出したリージョンの情報
     */
    @Override
    public void onRegionOut(final BCLRegion bclRegion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#00ffff\">" + getNowDate()  + "  RegionOut  " + bclRegion.getName() + "</font><br>");
                txt_logs.append(csHTML);
                infoScarollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    /**
     * LocoSDKがエラーを検知した際に呼ばれます
     * @param bclError　LocoSDKからのエラー
     */
    @Override
    public void onError(final BCLError bclError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#ff0000\">" + getNowDate()  + "  ErrorCode  " + bclError.getCode() + "</font><br>");
                txt_logs.append(csHTML);
                csHTML = Html.fromHtml("<font color=\"#ff0000\">" + getNowDate()  + "  ErrorMessage  " + bclError.getMessage() + "</font><br>");
                txt_logs.append(csHTML);
                infoScarollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT < 23) {
            permissionCheckResult(
                    2, new int[]{PackageManager.PERMISSION_GRANTED});
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request missing location permission.
                Log.d("LocoServiceAppMain", "パーミッションなし");
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                } else {
                     requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);


                }
            } else {
                // Location permission has been granted, continue as usual.
                Log.d("LocoServiceAppMain", "パーミッションあり");
                permissionCheckResult(
                        2, new int[]{PackageManager.PERMISSION_GRANTED});
            }
        }
    }

    public void permissionCheckResult(int requestCode, int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocoServiceAppMain", "パーミッション許可");

            } else {
                Log.d("LocoServiceAppMain", "パーミッション拒否");
            }
        }
    }
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
