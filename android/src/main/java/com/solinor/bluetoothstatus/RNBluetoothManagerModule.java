package com.solinor.bluetoothstatus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class RNBluetoothManagerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private BluetoothAdapter btAdapter;

    BroadcastReceiver mbluetoothStateBroadcastReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    JSONObject jsonObject = new JSONObject();
                    switch (blueState){
                        case BluetoothAdapter.STATE_OFF:
                            try {
                                jsonObject.put("state","off");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            callbackBluetoothStateNofitication(jsonObject);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            try {
                                jsonObject.put("state","on");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            callbackBluetoothStateNofitication(jsonObject);
                            break;
                    }
                    break;
            }
        }
    };

    public RNBluetoothManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        if (Build.VERSION.SDK_INT >= 34 && reactContext.getApplicationInfo().targetSdkVersion >= 34) {
            reactContext.registerReceiver(mbluetoothStateBroadcastReceive, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            reactContext.registerReceiver(mbluetoothStateBroadcastReceive, intentFilter);
        }
    }

    public void callbackBluetoothStateNofitication(JSONObject params) {
        WritableMap writableMap = jsonObject2WriteableMap(params);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("bluetoothStateNotifications", writableMap);
    }

    public WritableMap jsonObject2WriteableMap(JSONObject jsonObject) {
        try {
            WritableMap writableMap = Arguments.createMap();
            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Object object = jsonObject.get(key);
                if (object instanceof String) {
                    writableMap.putString(key, jsonObject.getString(key));
                } else if (object instanceof Boolean) {
                    writableMap.putBoolean(key, jsonObject.getBoolean(key));
                } else if (object instanceof Integer) {
                    writableMap.putInt(key, jsonObject.getInt(key));
                } else if (object instanceof Double) {
                    writableMap.putDouble(key, jsonObject.getDouble(key));
                } else if (object instanceof JSONObject) {
                    writableMap.putMap(key, jsonObject2WriteableMap(jsonObject.getJSONObject(key)));
                } else {
                    writableMap.putNull(key);
                }
            }
            return writableMap;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String getName() {
        return "RNBluetoothManager";
    }

    @ReactMethod
    public void getBluetoothState(Callback callback) {
        boolean isEnabled = false;
        if (btAdapter != null) {
            isEnabled = btAdapter.isEnabled();
        }
        callback.invoke(null, isEnabled);
    }

    @ReactMethod
    public void setBluetoothOn(Callback callback) {
        if (btAdapter != null) {
            btAdapter.enable();
        }
        callback.invoke(null, btAdapter != null);
    }

    @ReactMethod
    public void setBluetoothOff(Callback callback) {
        if (btAdapter != null) {
            btAdapter.disable();
        }
        callback.invoke(null, btAdapter != null);
    }
}