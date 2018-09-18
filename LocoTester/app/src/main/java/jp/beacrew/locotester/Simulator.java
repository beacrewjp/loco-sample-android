package jp.beacrew.locotester;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.beacrew.loco.BCLBeacon;

import jp.beacrew.loco.BCLCluster;
import jp.beacrew.loco.BCLManager;

public class Simulator extends Activity {
    private ListView list_Beacon;
    private ArrayList<ListItem> beaconArrayList;
    private ImageArrayAdapter arrayAdapter;
    private ArrayList<ObjectAnimator> objectAnimatorList;
    private BCLManager bclManager;
    private ArrayList<BCLCluster> clusterlist;
    private ArrayList<BCLBeacon> beaconlist;
    private ArrayList<BeaconTransmitter> beaconTransmitterArrayList;
    static final String BR = System.getProperty("line.separator");
    private Context mApplicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);

        mApplicationContext = getApplicationContext();
        ButterKnife.bind(this);
        objectAnimatorList = new ArrayList<>();
        list_Beacon = (ListView) findViewById(R.id.list_beacon);

        bclManager = BCLManager.getInstance(mApplicationContext);
        clusterlist = bclManager.getClusters();
        beaconTransmitterArrayList = new ArrayList<>();
        beaconArrayList = new ArrayList<>();
        beaconlist = new ArrayList<>();

//Advertise

        for (BCLCluster bclCluster : clusterlist) {
            ListItem clusterItem = new ListItem();
            clusterItem.setCluseterName("   " + bclCluster.getName());

            ArrayList<BCLBeacon> bclBeacons = bclCluster.getBeacons();
            if (bclBeacons.size() > 0) {
                beaconlist.add(new BCLBeacon());
                createBeaconTransmitter();
                beaconArrayList.add(clusterItem);

                for (BCLBeacon bclBeacon : bclBeacons) {
                    ListItem beaconItem = new ListItem();
                    beaconItem.setImageId(R.drawable.icons8ibeacon100);
                    beaconItem.setText("NAME: " + bclBeacon.getName());
                    beaconItem.setSubText("UUID:" + bclBeacon.getUuid() + BR + "MAJOR:" + bclBeacon.getMajor() + " MINOR:" + bclBeacon.getMinor());
                    beaconlist.add(bclBeacon);
                    createBeaconTransmitter();
                    beaconArrayList.add(beaconItem);
                }
            }
        }

        ImageArrayAdapter.ImageArrayAdapterCallback imageArrayAdapterCallback = new ImageArrayAdapter.ImageArrayAdapterCallback() {
            @Override
            public void onSwitchCheck(int position) {


                BCLBeacon bclBeacon = beaconlist.get(position);
                if (bclBeacon != null) {
                    Beacon beacon = new Beacon.Builder()
                            .setBluetoothName(bclBeacon.getName())
                            .setId1(bclBeacon.getUuid())
                            .setId2(String.valueOf(bclBeacon.getMajor()))
                            .setId3(String.valueOf(bclBeacon.getMinor()))
                            .setManufacturer(0x004C)
                            .build();
                    advertiseBeacon(beacon, position, bclBeacon.getName());
                }
            }
        };

        arrayAdapter = new ImageArrayAdapter(getApplicationContext(), R.layout.list_view_image_item, beaconArrayList, imageArrayAdapterCallback);
        list_Beacon.setAdapter(arrayAdapter);

    }

    protected void onDestroy() {
        super.onDestroy();
        stopAllAdvertise();
    }

    @OnClick(R.id.img_logger2)
    public void onLoggerClick() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        stopAllAdvertise();
        finish();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private void createBeaconTransmitter() {
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitterArrayList.add(beaconTransmitter);
    }

    private void advertiseBeacon(Beacon beacon, int position, final String beaconName) {

//送信開始
        if (!beaconTransmitterArrayList.get(position).isStarted()) {
            beaconTransmitterArrayList.get(position).startAdvertising(beacon, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.d("advertiseTest", "onStartSuccess");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    //失敗
                    Log.d("advertiseTest", "onStartFailure");
                }
            });
        } else {
            beaconTransmitterArrayList.get(position).stopAdvertising();
            Log.d("advertiseTest", "StopAdvertise");
        }

    }

    private void stopAllAdvertise() {
        if (beaconTransmitterArrayList != null) {
            for (int idx = 0; idx < beaconTransmitterArrayList.size(); idx++) {
                beaconTransmitterArrayList.get(idx).stopAdvertising();
            }
        }
    }
}
