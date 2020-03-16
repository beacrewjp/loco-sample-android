package jp.beacrew.locoposition;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import jp.beacrew.loco.BCLAction;
import jp.beacrew.loco.BCLBeacon;
import jp.beacrew.loco.BCLCluster;
import jp.beacrew.loco.BCLError;
import jp.beacrew.loco.BCLManager;
import jp.beacrew.loco.BCLManagerEventListener;
import jp.beacrew.loco.BCLRegion;
import jp.beacrew.loco.BCLState;
import jp.beacrew.loco.positioning.BCLPositioningEventListener;
import jp.beacrew.loco.positioning.BCLPositioningManager;

public class MainActivity extends AppCompatActivity implements BCLManagerEventListener, BCLPositioningEventListener {

    private ImageView img_map;
    private ImageButton btn_start;
    private ImageButton btn_stop;
    private TextView txt_status;
    private TextView txt_cluster;
    private TextView txt_position;
    private TextView txt_info;
    private ArrayList<BCLBeacon> mScanBeacons;
    private Bitmap mBitmapBuffer;
    private BCLManager mBCLManager;
    private BCLPositioningManager mBCLPositioningPluginManager;
    static final String BR = System.getProperty("line.separator");
    private String TAG = "LocoPosition";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String ApiKey = "ENTER YOUR SDK SECRET";
        setContentView(R.layout.activity_main);

        img_map = findViewById(R.id.img_map);
        txt_status = findViewById(R.id.txt_status);
        txt_cluster = findViewById(R.id.txt_cluster);
        txt_position = findViewById(R.id.txt_position);
        txt_info = findViewById(R.id.txt_info);

        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBCLManager.getState().equals(BCLState.READY)) {
                    mBCLManager.initWithApiKey(ApiKey, true);
                } else {
                    mBCLManager.scanStart();
                }
            }
        });
        btn_stop = findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBCLManager.scanStop();
            }
        });

        BCLManager.setUseForegroundService(true);
        mBCLManager =  BCLManager.getInstance(this.getApplicationContext());
        mBCLManager.setListener(this);
        mBCLPositioningPluginManager = new BCLPositioningManager();
        mBCLPositioningPluginManager.setPositioningListener(this);
        mBCLPositioningPluginManager.setBCLManager(mBCLManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBCLManager.terminateService();
        Log.d(TAG, "MainOnDestroy");
    }

    @Override
    public void onStateChange(BCLState bclState) {
        txt_status.setText(bclState.toString());
    }

    @Override
    public void onBeaconDetected(ArrayList<BCLBeacon> beacons) {
        Log.d(TAG,"onBeaconDetected");
        mScanBeacons = beacons;
        mBCLPositioningPluginManager.estimatePosition(beacons);
    }

    @Override
    public void onActionDetected(BCLAction bclAction, String s, Object o) {

    }

    @Override
    public void onRegionIn(BCLRegion bclRegion) {

    }

    @Override
    public void onRegionOut(BCLRegion bclRegion) {

    }

    @Override
    public void onEstimatePotision(double positionX, double positionY, BCLCluster bclCluster) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Paint paintCircle = new Paint();
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(Color.BLUE);
        paintCircle.setAlpha(128);

        Paint paintRssi = new Paint();
        paintRssi.setAntiAlias(true);
        paintRssi.setColor(Color.WHITE);
        paintRssi.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintRssi.setTextSize(20);

        if (mBitmapBuffer != null) {

            Bitmap currentBitmap = mBitmapBuffer.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(currentBitmap);
            for (BCLBeacon bclBeacon : bclCluster.getBeacons()) {

                for (BCLBeacon scanBeacon : mScanBeacons) {
                    if (bclBeacon.getBeaconId().equals(scanBeacon.getBeaconId())) {

                        paint.setColor(rssiToColor(scanBeacon.getRssi()));
                        canvas.drawRoundRect(bclBeacon.getBeaconX() - 20
                                , bclBeacon.getBeaconY() + 20
                                , bclBeacon.getBeaconX() + 20
                                , bclBeacon.getBeaconY() - 20
                                , 10.0f
                                , 10.0f
                                , paint);

                        canvas.drawText(String.valueOf(scanBeacon.getRssi()), bclBeacon.getBeaconX() - 15, bclBeacon.getBeaconY() + 5, paintRssi);
                        break;
                    } else {
                        paint.setColor(Color.BLACK);
                        canvas.drawRoundRect(bclBeacon.getBeaconX() - 20
                                , bclBeacon.getBeaconY() + 20
                                , bclBeacon.getBeaconX() + 20
                                , bclBeacon.getBeaconY() - 20
                                , 10.0f
                                , 10.0f
                                , paint);
                    }
                }
            }
            canvas.drawCircle((float) positionX, (float) positionY, 15, paintCircle);
            img_map.setImageBitmap(currentBitmap);

            txt_position.setText("X:" + String.format("%.0f",positionX) + BR +"Y:" + String.format("%.0f", positionY));
            txt_cluster.setText(bclCluster.getName());

        }
    }

    @Override
    public void onChangeLocatedCluster(BCLCluster bclCluster) {
        Picasso.get().load(bclCluster.getImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Bitmap workingBitmap = resize(bitmap, 800, 800);

                Log.d(TAG, "Bitmap X:" + String.valueOf(workingBitmap.getWidth()));
                Log.d(TAG, "Bitmap Y:" + String.valueOf(workingBitmap.getHeight()));

                mBitmapBuffer = workingBitmap;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed :" + e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public void onError(BCLError bclError) {
        txt_info.setText("Code: " + bclError.getCode() + " " + bclError.getMessage());
    }

    private void permissionCheck() {

        if (Build.VERSION.SDK_INT > 23) {

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
                    Log.d(TAG, "Backgroundパーミッション許可");

                } else {
                    // Permission was denied or request was cancelled
                    Log.d(TAG, "Backgroundパーミッション拒否");
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
                    Log.d(TAG, "パーミッション許可");

                } else {
                    // Permission was denied or request was cancelled
                    Log.d(TAG, "パーミッション拒否");
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

    private int rssiToColor(int rssi) {

        float hsv[] = new float[3];
        hsv[1] = 1.0f;
        hsv[2] = 0.95f;
        float hue = 0.0f;

        if (-0 < rssi) {
            hue = 0.0f;
        }else if (-55 < rssi) {
            hue = (float)rssi / 5.5f;
        } else if (-70 < rssi) {
            hue = ( (float)rssi + 55.0f ) * 2.0f - 10.0f;
        } else if (-90 < rssi) {
            hue = ( (float)rssi + 70.0f ) * 8.0f - 40.0f;
        } else if (-100 < rssi) {
            hue = ( (float)rssi + 90.0f ) * 7.0f - 200.0f;
        } else {
            hue = -270.0f;
        }

        hsv[0] = -hue;

        return Color.HSVToColor(hsv);
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

}
