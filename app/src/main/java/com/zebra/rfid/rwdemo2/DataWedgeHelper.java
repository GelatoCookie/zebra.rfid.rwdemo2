package com.zebra.rfid.rwdemo2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class DataWedgeHelper {
    private final Context context;

    public DataWedgeHelper(Context context) {
        this.context = context;
    }

    public void sendDataWedgeIntent(String action, Bundle extras) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (extras != null) intent.putExtras(extras);
        try {
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send DataWedge intent: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void sendProfileConfig(Bundle setConfigBundle) {
        Bundle extras = new Bundle();
        extras.putBundle(RWDemoIntentParams.ACTION_EXTRA_SET_CONFIG, setConfigBundle);
        extras.putString(RWDemoIntentParams.EXTRA_SEND_RESULT, "true");
        extras.putString(RWDemoIntentParams.EXTRA_COMMAND_IDENTIFIER, "RFID_CONFIG");
        sendDataWedgeIntent(RWDemoIntentParams.ACTION, extras);
    }

    public void sendSoftRfidTrigger(boolean start) {
        Bundle extras = new Bundle();
        extras.putString(RWDemoIntentParams.ACTION_EXTRA_SOFT_RFID_TRIGGER, start ? "START_SCANNING" : "STOP_SCANNING");
        sendDataWedgeIntent(RWDemoIntentParams.ACTION, extras);
    }

    public void sendSoftBarcodeTrigger(boolean start) {
        Bundle extras = new Bundle();
        extras.putString(RWDemoIntentParams.ACTION_EXTRA_SOFT_SCAN_TRIGGER, start ? "START_SCANNING" : "STOP_SCANNING");
        sendDataWedgeIntent(RWDemoIntentParams.ACTION, extras);
    }

    public void getAvailableProfiles() {
        Bundle extras = new Bundle();
        extras.putString(RWDemoIntentParams.GET_AVAILABLE_PROFILE_LIST, "");
        sendDataWedgeIntent(RWDemoIntentParams.ACTION, extras);
    }

    public void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
