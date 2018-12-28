package com.inspur.plugin.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class ScanPlugin extends CordovaPlugin {
    private static final String TAG = "ScanPlugin";

    BroadcastReceiver receiver = null;

    private CallbackContext scanCallbackContext = null;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if(action.equals("start")) {
            if (this.scanCallbackContext != null) {
              removeScanListener();
            }
            this.scanCallbackContext = callbackContext;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.android.server.scannerservice.broadcast");
            if (this.receiver == null) {
                this.receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String scanResult = intent.getStringExtra("scannerdata");
                        try {
                            notifyScanResult(scanResult);
                        } catch (JSONException e) {
                            Log.e(TAG, "qualityScan error:", e);
                        }
                    }
                };
                webView.getContext().registerReceiver(this.receiver, intentFilter);
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } else if(action.equals("stop")) {
            removeScanListener();
            this.sendUpdate(new JSONObject(), false);
            this.scanCallbackContext = null;
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    public void onDestroy() {
        removeScanListener();
    }

    @Override
    public void onReset() {
        removeScanListener();
    }

    private void removeScanListener() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering battery receiver: " + e.getMessage(), e);
            }
        }
    }

    private void notifyScanResult(String scanResult) throws JSONException {
        Log.e(TAG, "scanResult:"+scanResult);
        JSONObject jo = new JSONObject();
        jo.put("data", scanResult);
        sendUpdate(jo, true);
    }

    private void sendUpdate(JSONObject info, boolean keepCallback) {
        if (this.scanCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.scanCallbackContext.sendPluginResult(result);
        }
    }
}
