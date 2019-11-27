package jp.beacrew.locotester;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.beacrew.loco.BCLAction;
import jp.beacrew.loco.BCLBeacon;
import jp.beacrew.loco.BCLCluster;
import jp.beacrew.loco.BCLError;
import jp.beacrew.loco.BCLManager;
import jp.beacrew.loco.BCLManagerEventListener;
import jp.beacrew.loco.BCLRegion;
import jp.beacrew.loco.BCLState;


public class MainActivity extends AppCompatActivity implements BCLManagerEventListener{

    private BCLManager mBclmanager;
    private TextView txt_logs;
    private ScrollView infoScrollView;
    private String APIKEY = "ENTER YOUR SDK SECRET";
    static final String BR = System.getProperty("line.separator");
    private Context mApplicationContext;

    private ImageView imgInit;
    private ImageView imgStart;
    private ImageView imgStop;
    private ImageView imgInfo;
    private ImageView imgSimulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mApplicationContext = getApplicationContext();

        txt_logs = findViewById(R.id.txt_logs);
        infoScrollView = findViewById(R.id.logs_scroll);
        mBclmanager = BCLManager.getInstance(mApplicationContext);
        mBclmanager.setListener(this);
        //ButterKnife.bind(this);

        imgInit = (ImageView)findViewById(R.id.img_init);
        imgInit.setOnClickListener(new View.OnClickListener() {
    @Override
            public void onClick(View view) {
        if (mBclmanager.getState() != BCLState.READY) {
            mBclmanager.initWithApiKey(APIKEY, false);
        }

    }
        });

    /**
     * ビーコンのスキャンを開始します
     */
        imgStart = (ImageView)findViewById(R.id.img_start);
        imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        if(mBclmanager.getState().equals(BCLState.READY)) {
            txt_logs.append(getNowDate() + " [info]    ScanStart" + BR);
            mBclmanager.scanStart();
        }
    }
        });

    /**
     * ビーコンのスキャンを停止します
     */
        imgStop = (ImageView)findViewById(R.id.img_stop);
        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        if(mBclmanager.getState().equals(BCLState.SCANNING)) {
            txt_logs.append(getNowDate() + " [info]    ScanStop" + BR);
            mBclmanager.scanStop();
        }
    }
        });

    /**
     * LocoSDKの初期化によって得られた情報（Locoダッシュボードで入力されたデータ）を表示します
     */
        imgInfo = (ImageView)findViewById(R.id.img_info);
        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        final String[] items = {"DeviceID", "Nearest BeaconID", "Beacons", "Clusters", "Regions", "Actions", "Add Event Log"};
        int defaultItem = 0; // デフォルトでチェックされているアイテム
        final List<Integer> checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
                new AlertDialog.Builder(MainActivity.this)
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
                                        txt_logs.append("[DeviceID]  \n" + mBclmanager.getDeviceId() + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 1:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        txt_logs.append("[Nearest BeaconID]  \n" + mBclmanager.getNearestBeaconId() + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 2:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                        ArrayList<BCLBeacon> bclBeacons = mBclmanager.getBeacons();
                                        HashMap beaconHash = new HashMap();
                                        beaconHash.put("beacons", bclBeacons);
                                        String beacons = gson.toJson(beaconHash);

                                        txt_logs.append("[Beacons]  \n" + beacons + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 3:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                        ArrayList<BCLCluster> bclClusters = mBclmanager.getClusters();
                                        HashMap clusterHash = new HashMap();
                                        clusterHash.put("clusters", bclClusters);
                                        String clusters = gson.toJson(clusterHash);

                                        txt_logs.append("[Clusters]  \n" + clusters + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 4:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                        ArrayList<BCLRegion> bclRegions = mBclmanager.getRegions();
                                        HashMap regionHash = new HashMap();
                                        regionHash.put("regions", bclRegions);
                                        String regions = gson.toJson(regionHash);

                                        txt_logs.append("[Regions]  \n" + regions + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 5:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                        ArrayList<BCLAction> bclActions = mBclmanager.getActions();
                                        HashMap actionHash = new HashMap();
                                        actionHash.put("actions", bclActions);
                                        String actions = gson.toJson(actionHash);

                                        txt_logs.append("[Actions]  \n" + actions + "\n");
                                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                    break;
                                case 6:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                                    final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                                        dialog.setTitle("Add Event Log");
                                                    LinearLayout layout = new LinearLayout(MainActivity.this);
                                        layout.setOrientation(LinearLayout.VERTICAL);
                                                    TextView txtTitleKey = new TextView(MainActivity.this);
                                        txtTitleKey.setText("KEY");
                                                    TextView txtTitleValue = new TextView(MainActivity.this);
                                        txtTitleValue.setText("VALUE");

                                                    final EditText editTextKey = new EditText(MainActivity.this);
                                        editTextKey.setInputType(InputType.TYPE_CLASS_TEXT);
                                        editTextKey.setWidth(100);
                                                    final EditText editTextValue = new EditText(MainActivity.this);
                                        editTextValue.setInputType(InputType.TYPE_CLASS_TEXT);
                                        editTextValue.setWidth(100);

                                        layout.addView(txtTitleKey);
                                        layout.addView(editTextKey);
                                        layout.addView(txtTitleValue);
                                        layout.addView(editTextValue);
                                        dialog.setView(layout);

                                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (!mBclmanager.getState().equals(BCLState.UNINITIALIZED)) {
                                                    mBclmanager.addEventLog(editTextKey.getText().toString(), editTextValue.getText().toString());
                                                    txt_logs.append("[AddEventlog]  \n" + "key: " + editTextKey.getText().toString() + "\n" + "value: " + editTextValue.getText().toString() + "\n");
                                                    infoScrollView.fullScroll(View.FOCUS_DOWN);
                                                }
                                            }
                                        });

                                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        dialog.show();
                                        }
                                    });

                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
        });

        imgSimulator = (ImageView)findViewById(R.id.img_simulator);
        imgSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Simulator.class);

        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }
        });


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

    @Override
    public void onStateChange(final BCLState bclState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence csHTML = Html.fromHtml("<font color=\"#ffffff\">" + getNowDate() + " [Status]    " + bclState.toString() + "</font><br>");
                txt_logs.append(csHTML);
            }
        });

    }

    @Override
    public void onBeaconDetected(final ArrayList<BCLBeacon> bclBeacons) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            for (BCLBeacon bclBeacon : bclBeacons) {
                CharSequence csHTML = Html.fromHtml("<font color=\"#ffffff\">" + getNowDate()  + " [Ranging]    " + bclBeacon.getName() + "</font><br>");
                txt_logs.append(csHTML);
                infoScrollView.fullScroll(View.FOCUS_DOWN);
            }
            }
        });

    }

    @Override
    public void onActionDetected(final BCLAction bclAction, String s, Object o) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            CharSequence csHTML = Html.fromHtml("<font color=\"#ffff00\">" + getNowDate()  + " [Action]    " +  bclAction.getName() + "</font><br>");
            txt_logs.append(csHTML);
            infoScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onRegionIn(final BCLRegion bclRegion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            CharSequence csHTML = Html.fromHtml("<font color=\"#00ff00\">" + getNowDate()  + " [RegionIn]    " + bclRegion.getName() + "</font><br>");

            txt_logs.append(csHTML);
            infoScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    @Override
    public void onRegionOut(final BCLRegion bclRegion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            CharSequence csHTML = Html.fromHtml("<font color=\"#00ffff\">" + getNowDate()  + " [RegionOut]  " + bclRegion.getName() + "</font><br>");
            txt_logs.append(csHTML);
            infoScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onError(final BCLError bclError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            CharSequence csHTML = Html.fromHtml("<font color=\"#ff0000\">" + getNowDate()  + " [Error]    " + bclError.getMessage() + "</font><br>");
            txt_logs.append(csHTML);
            infoScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
