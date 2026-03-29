/**
 * Copyright (C) 2019 Zebra Technologies Corporation and/or its affiliates.
 * All rights reserved.
 */
package com.zebra.rfid.rwdemo2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zebra.rfid.rwdemo2.RWDemoIntentParams.*;


/**
 * RWDemoActivity is  a demo application that designed to read RFID Tags.
 * To read RFID tags RWDemoActivity uses intent API of a DataWedge Application.
 */
public class RWDemoActivity extends Activity implements OnClickListener,    OnMenuItemClickListener {
    private View progressOverlay;
    private TextView progressMessageText;

    private static final String TAG = RWDemoActivity.class.getSimpleName();

    private static final String SOURCE_TAG = "com.symbol.datawedge.source";
    private static final String DATA_STRING_TAG = "com.symbol.datawedge.data_string";
    private static final String MSR_DATA_TAG = "com.symbol.datawedge.msr_data";

    private static final String TRIGGER_START = "START_SCANNING";
    private static final String TRIGGER_STOP = "STOP_SCANNING";

    private static final String DATA_INTENT = "StartingIntent";
    private static final String DATA_TXT = "UIText";
    private final static String SCROLL_VIEW_UPDATE_THREAD_NAME = "RWDenoscollViewUpdateThread";
    private static final int VIEW_UPDATE_TIME_IN_MILLISECONDS = 100;
    private static final int BACKGROUND_GREY_COLOR_CODE = 0xffbfc7cc;

    private static final String SOURCE_SCANNER = "scanner";
    private static final String SOURCE_MSR = "msr";
    private static final String DATA_MSR = "[encrypted MSR data]";
    private static final String ENDLINE_CHAR = "\n";

    private static final String COMMAND_ID = "COMMAND_IDENTIFIER";
    private static boolean rfidScanState = false;
    private static boolean barcodeScanState = false;


    public static boolean DEBUG = true;

    private static float disabledButtonAlphaVlue = 0.2f;
    private static float enabledButtonAlphaVlue = 1f;
    private static Context thisContext = null;
    private static boolean rwDemoProfileActivated = false;
    private static ProgressBar progressBar;
    private static final int PROGRESS_BAR_WIDTH = 50;
    private static final int PROGRESS_BAR_HIGHT = 50;

    // Timer mechanisms for robust timeout
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable rfidTimeoutRunnable;
    private Runnable barcodeTimeoutRunnable;
    private static final long SCAN_TIMEOUT_MS = 5000;

    private String tvText = "";
    private ImageButton softScanTrigger;
    private ImageButton barcodeScanTrigger;
    private ImageButton actionBtnClear;
    private ImageButton actionBtnReaderSelection;
    private ImageButton actionBtnFriendlyProfiles;
    private ImageButton actionBtnSettings;

    private TextView scannerStatusText;
    private TextView rfidStatusText;
    private TextView uniqueCountText;
    private TextView totalCountText;

    private Set<String> uniqueTags = new HashSet<>();
    private int totalTags = 0;

    private static int DW_DEMO_POPUP_MENU_SETTINGS = 1;

    private Intent mDataIntent = null;
    private boolean onNewIntentToOnResume;
    private long clickTime = 0;


    private void createProfile() {

        Bundle setConfigBundle = new Bundle();
        setConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_PROFILE_NAME_KEY, RWDemoIntentParams.BUNDLE_EXTRA_PROFILE_NAME_VAL);
        setConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_CONFIG_MODE_KEY, RWDemoIntentParams.BUNDLE_EXTRA_CONFIG_MODE_VAL);
        setConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_PROFILE_ENABLED_KEY, RWDemoIntentParams.BUNDLE_EXTRA_PROFILE_ENABLED_VAL);
        setConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);

        // Associate profile with this app
        Bundle appConfig = new Bundle();
        appConfig.putString(RWDemoIntentParams.BUNDLE_EXTRA_PACKAGE_NAME_KEY, getPackageName());
        appConfig.putStringArray(RWDemoIntentParams.BUNDLE_EXTRA_ACTIVITY_LIST_KEY, RWDemoIntentParams.BUNDLE_EXTRA_ACTIVITY_LIST_VAL_ARR);
        setConfigBundle.putParcelableArray(RWDemoIntentParams.BUNDLE_EXTRA_APP_LIST_KEY, new Bundle[]{appConfig});
        setConfigBundle.remove(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_CONFIG);

        // RFID Input configurations
        Bundle rfidConigParamList = new Bundle();
        rfidConigParamList.putString(RWDemoIntentParams.PLUGIN_ENABLE_PARAM_KEY, RWDemoIntentParams.PLUGIN_ENABLE_PARAM_VAL);

        Bundle rfidConfigBundle = new Bundle();
        rfidConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_RFID);
        rfidConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);
        rfidConfigBundle.putBundle(RWDemoIntentParams.BUNDLE_EXTRA_PARAM_LIST, rfidConigParamList);

        Bundle rfidFormattingConfigBundle = new Bundle();
        rfidFormattingConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_RFID_F);
        rfidFormattingConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_OUTPUT_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_INTENT);
        rfidFormattingConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);

        // Barcode Input configurations
        Bundle barcodeConfigBundle = new Bundle();
        barcodeConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_BARCODE);
        barcodeConfigBundle.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);
        Bundle barcodeProps = new Bundle();
        barcodeProps.putString(RWDemoIntentParams.BARCODE_ENABLE_PARAM_KEY, "true");
        barcodeConfigBundle.putBundle(RWDemoIntentParams.BUNDLE_EXTRA_PARAM_LIST, barcodeProps);

        // Configure intent output for captured data to be sent to this app
        Bundle intentConfig = new Bundle();
        intentConfig.putString(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_INTENT);
        intentConfig.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);
        Bundle intentProps = new Bundle();
        intentProps.putString(RWDemoIntentParams.INTENT_OUTPUT_ENABLED_KEY, RWDemoIntentParams.INTENT_OUTPUT_ENABLED_VALUE);
        intentProps.putString(RWDemoIntentParams.INTENT_ACTION_KEY, RWDemoIntentParams.INTENT_ACTION_VALUE);
        intentProps.putString(RWDemoIntentParams.INTENT_CATEGORY_KEY, RWDemoIntentParams.INTENT_CATEGORY_VALUE);
        intentProps.putString(RWDemoIntentParams.INTENT_DELIVERY_KEY, RWDemoIntentParams.INTENT_DELIVERY_VALUE);
        intentConfig.putBundle(RWDemoIntentParams.BUNDLE_EXTRA_PARAM_LIST, intentProps);

        // Configure KeyStroke for captured data to be sent to this app
        Bundle keyStrokeConfig = new Bundle();
        keyStrokeConfig.putString(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_NAME, RWDemoIntentParams.PLUGIN_NAME_KEYSTROKE);
        keyStrokeConfig.putString(RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_KEY, RWDemoIntentParams.BUNDLE_EXTRA_RESET_CONFIG_VAL);
        Bundle keyStrokeProps = new Bundle();
        keyStrokeProps.putString(RWDemoIntentParams.KEYSTROKE_ENABLED_KEY, RWDemoIntentParams.KEYSTROKE_ENABLED_VALUE);
        keyStrokeConfig.putBundle(RWDemoIntentParams.BUNDLE_EXTRA_PARAM_LIST, keyStrokeProps);


        ArrayList<Parcelable> configBundles = new ArrayList<>();
        configBundles.add(rfidConfigBundle);
        configBundles.add(rfidFormattingConfigBundle);
        configBundles.add(barcodeConfigBundle);
        configBundles.add(intentConfig);
        configBundles.add(keyStrokeConfig);

        setConfigBundle.putParcelableArrayList(RWDemoIntentParams.BUNDLE_EXTRA_PLUGIN_CONFIG, configBundles);

        Intent intent = new Intent();
        intent.setAction(RWDemoIntentParams.ACTION);
        intent.putExtra(RWDemoIntentParams.ACTION_EXTRA_SET_CONFIG, setConfigBundle);
        intent.putExtra(EXTRA_SEND_RESULT, "true");
        intent.putExtra(EXTRA_COMMAND_IDENTIFIER, "RFID_CONFIG");
        sendBroadcast(intent);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntentToOnResume = false;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {//data intent
                String data = intent.getStringExtra(DATA_STRING_TAG);
                if (data != null)
                    mDataIntent = intent;
            }
            tvText = "";
        } else {
            mDataIntent = savedInstanceState.getParcelable(DATA_INTENT);
            tvText = savedInstanceState.getString(DATA_TXT);
        }

        setContentView(R.layout.dwdemo_main);
        progressBar = findViewById(R.id.progressBar);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessageText = findViewById(R.id.progressMessage);

        // Tapping overlay stops the scan
        if (progressOverlay != null) {
            progressOverlay.setOnClickListener(v -> {
                if (rfidScanState) stopRfidScan();
                if (barcodeScanState) stopBarcodeScan();
            });
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setMainBackground();

        TextView tv = (TextView) findViewById(R.id.output_view);
        tv.setText(tvText);
        scrollDown();


        softScanTrigger = (ImageButton) findViewById(R.id.softscanbutton);
        softScanTrigger.setOnClickListener(v -> {
            Log.d(TAG, "onClick softScanTrigger pressed");
            if ((SystemClock.elapsedRealtime() - clickTime) < 500) {
                return;
            }
            clickTime = SystemClock.elapsedRealtime();
            
            // Clear UI and counts before inventory
            if (!rfidScanState) {
                clearData();
            }
            
            toggleSoftRfidTrigger();
        });

        barcodeScanTrigger = (ImageButton) findViewById(R.id.barcodeScanButton);
        barcodeScanTrigger.setOnClickListener(v -> {
            Log.d(TAG, "onClick barcodeScanTrigger pressed");
            if ((SystemClock.elapsedRealtime() - clickTime) < 500) {
                return;
            }
            clickTime = SystemClock.elapsedRealtime();
            
            // Clear UI and counts before scan
            if (!barcodeScanState) {
                clearData();
            }

            toggleSoftBarcodeTrigger();
        });

        actionBtnClear = (ImageButton) findViewById(R.id.actionButton1);
        actionBtnClear.setOnClickListener(v -> {
            if (v.getId() == actionBtnClear.getId()) {
                clearData();
            }

        });

        actionBtnReaderSelection = (ImageButton) findViewById(R.id.actionButton2);
        actionBtnReaderSelection.setImageResource(R.drawable.rfid_icon_s);
        actionBtnReaderSelection.setEnabled(false);

        actionBtnFriendlyProfiles = (ImageButton) findViewById(R.id.actionButton3);
        actionBtnFriendlyProfiles.setOnClickListener(this);

        actionBtnSettings = (ImageButton) findViewById(R.id.actionButton4);
        actionBtnSettings.setOnClickListener(v -> {
            if (v.getId() == actionBtnSettings.getId()) {
                showPopupMenu(v, RWDemoActivity.DW_DEMO_POPUP_MENU_SETTINGS);
            }

        });

        scannerStatusText = findViewById(R.id.scannerStatusText);
        rfidStatusText = findViewById(R.id.rfidStatusText);
        uniqueCountText = findViewById(R.id.uniqueCountText);
        totalCountText = findViewById(R.id.totalCountText);

        // Initial status UI
        updateStatusUI(scannerStatusText, R.string.status_scanner, getString(R.string.status_unknown));
        updateStatusUI(rfidStatusText, R.string.status_rfid, getString(R.string.status_unknown));
        updateCountUI();

        thisContext = this;
    }

    private void clearData() {
        runOnUiThread(() -> {
            TextView tv1 = (TextView) findViewById(R.id.output_view);
            tv1.setText("");
            tvText = "";
            uniqueTags.clear();
            totalTags = 0;
            updateCountUI();
        });
    }

    private void updateCountUI() {
        runOnUiThread(() -> {
            uniqueCountText.setText("Unique: " + uniqueTags.size());
            totalCountText.setText("Total: " + totalTags);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mDataIntent != null) {
            savedInstanceState.putParcelable(DATA_INTENT, mDataIntent);
        }
        savedInstanceState.putString(DATA_TXT, tvText);
        super.onSaveInstanceState(savedInstanceState);
        onNewIntentToOnResume = false;
    }


    @Override
    public void onResume() {
        super.onResume();
        rwDemoProfileActivated = false;

        if(!onNewIntentToOnResume){
            softScanTrigger.setEnabled(false);
            softScanTrigger.setAlpha(disabledButtonAlphaVlue);
            barcodeScanTrigger.setEnabled(false);
            barcodeScanTrigger.setAlpha(disabledButtonAlphaVlue);

            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

        }
        registerReceivers();
        registerForNotifications();

        // Get available profiles via intent api
        Intent intentProfGet = new Intent();
        intentProfGet.setAction(RWDemoIntentParams.ACTION);
        intentProfGet.putExtra(RWDemoIntentParams.GET_AVAILABLE_PROFILE_LIST, "");
        this.sendBroadcast(intentProfGet);


        Intent i = getIntent();
        if (mDataIntent != null) {
            i = mDataIntent;
        }
        if (i != null) {
            handleDecodeData(i);
        }

        // Fail safe mechanism to find the profile is successfully
        // switched to the RWDemo profile.
        final int softButtonUnfreezeDelayInMillisecnds = 1000;
        final int activationTimeoutInMilliseconds = 10000;

        if(!onNewIntentToOnResume){
            rwDemoProfileActivated = false;
            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                while(!rwDemoProfileActivated) {
                    if (System.currentTimeMillis() - startTime > activationTimeoutInMilliseconds) {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            playAlarmBeep(3);
                            new AlertDialog.Builder(RWDemoActivity.this)
                                    .setTitle("Activation Timeout")
                                    .setMessage("Could not enable DataWedge profile. Please ensure RFID hardware is connected and DataWedge is properly configured.")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        });
                        break;
                    }
                    try {
                        Intent intent = new Intent();
                        intent.setAction(RWDemoIntentParams.ACTION);
                        intent.putExtra(GET_ACTIVE_PROFILE, "");
                        thisContext.sendBroadcast(intent);

                        Thread.sleep(softButtonUnfreezeDelayInMillisecnds);
                    } catch (Exception e) {
                        if (DEBUG)
                            e.printStackTrace();
                    }
                }
            }).start();
        }

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop scanning to preserve battery when leaving activity
        stopRfidScan();
        stopBarcodeScan();

        // These things should happen regardless of unregistering success or not.
        rwDemoProfileActivated = true;
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        unRegisterForNotifications();
        try {
            //Un-register broadcast receiver when you receive profile list broadcast
            unregisterReceiver(datawedgeBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            //if receiver already unregistered
            if (DEBUG) {
                Log.d(TAG, e.getMessage() + "");
            }
        } catch (Exception e){
            if (DEBUG) {
                Log.d(TAG, e.getMessage() + "");
            }
        }

        System.gc();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRfidScan();
        stopBarcodeScan();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AlertDialog builder;
            try {
                builder = AboutDialogBuilder.create(this);
                builder.show();
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void showPopupMenu(View v, int dwDemoPopupMenuSettings) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.dwdemo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_about) {
                AlertDialog builder;
                try {
                    builder = AboutDialogBuilder.create(this);
                    builder.show();
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void scrollDown() {
        final Handler handler = new Handler();
        Thread scollViewUpdateThread = new Thread(() -> {
            try {
                Thread.sleep(VIEW_UPDATE_TIME_IN_MILLISECONDS);
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
            handler.post(() -> {
                ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
                sv.fullScroll(View.FOCUS_DOWN);
            });
        });
        scollViewUpdateThread.setName(SCROLL_VIEW_UPDATE_THREAD_NAME);
        scollViewUpdateThread.start();
    }

    private void setMainBackground() {
        int alphaValue = 20;

        Drawable[] layers = new Drawable[2];
        Resources res = getResources();

        ShapeDrawable rect = new ShapeDrawable();
        rect.getPaint().setColor(BACKGROUND_GREY_COLOR_CODE);
        layers[0] = rect;

        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.icmpmove);
        BitmapDrawable bd = new BitmapDrawable(bmp);
        bd.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        bd.setAlpha(alphaValue);

        layers[1] = bd;

        LayerDrawable ld = new LayerDrawable(layers);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout1);
        rl.setBackgroundDrawable(ld);
    }

    /**
     * Helper to send trigger commands to DataWedge API
     */
    private void triggerHardware(String triggerExtra, String command) {
        Intent i = new Intent();
        i.setAction(RWDemoIntentParams.ACTION);
        i.putExtra(triggerExtra, command);
        sendBroadcast(i);
    }

    private void toggleSoftRfidTrigger() {
        if (rfidScanState) {
            stopRfidScan();
        } else {
            startRfidScan();
        }
    }

    private void startRfidScan() {
        if (rfidScanState) return; // Prevent double-clicks
        rfidScanState = true;

        showProgressDialog("Reading RFID... (Timeout in 5s)");
        updateStatusUI(rfidStatusText, R.string.status_rfid, STATUS_SCANNING);

        // 1. Send explicit START command
        triggerHardware(ACTION_EXTRA_SOFT_RFID_TRIGGER, TRIGGER_START);

        // 2. Define what happens when the timer runs out
        rfidTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.i(TAG, "RFID timeout reached. Stopping hardware.");
                stopRfidScan();
            }
        };

        // 3. Start the countdown
        timeoutHandler.postDelayed(rfidTimeoutRunnable, SCAN_TIMEOUT_MS);
    }

    private void stopRfidScan() {
        if (!rfidScanState) return;
        rfidScanState = false;

        // CRITICAL: Cancel the timer so it doesn't fire anyway
        if (rfidTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(rfidTimeoutRunnable);
            rfidTimeoutRunnable = null;
        }

        // Send explicit STOP command
        triggerHardware(ACTION_EXTRA_SOFT_RFID_TRIGGER, TRIGGER_STOP);

        dismissProgressDialog();
        updateStatusUI(rfidStatusText, R.string.status_rfid, STATUS_WAITING);
    }

    private void toggleSoftBarcodeTrigger() {
        if (barcodeScanState) {
            stopBarcodeScan();
        } else {
            startBarcodeScan();
        }
    }

    private void startBarcodeScan() {
        if (barcodeScanState) return;
        barcodeScanState = true;

        showProgressDialog("Reading Barcode... (Timeout in 5s)");

        // Send explicit START command
        triggerHardware(ACTION_EXTRA_SOFT_SCAN_TRIGGER, TRIGGER_START);

        barcodeTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.i(TAG, "Barcode timeout reached. Stopping hardware.");
                stopBarcodeScan();
            }
        };
        timeoutHandler.postDelayed(barcodeTimeoutRunnable, SCAN_TIMEOUT_MS);
    }

    private void stopBarcodeScan() {
        if (!barcodeScanState) return;
        barcodeScanState = false;

        // Cancel the timer
        if (barcodeTimeoutRunnable != null) {
            timeoutHandler.removeCallbacks(barcodeTimeoutRunnable);
            barcodeTimeoutRunnable = null;
        }

        // Send explicit STOP command
        triggerHardware(ACTION_EXTRA_SOFT_SCAN_TRIGGER, TRIGGER_STOP);

        dismissProgressDialog();
    }

    @Override
    public void onNewIntent(Intent i) {
        onNewIntentToOnResume = true;
        final Intent intent = i;
        handleDecodeData(intent);
    }

    private void handleDecodeData(Intent i) {
        if (i == null)
            return;

        String data = i.getStringExtra(DATA_STRING_TAG);

        String source = i.getStringExtra(SOURCE_TAG);
        if (source == null)
            source = SOURCE_SCANNER;

        if (DEBUG) Log.d(TAG, "ECRT: handleDecodeData data=" + data + " source=" + source);

        if(source.contains("scanner")){
            playSuccessBeep();
            dismissProgressDialog();
        }

        int data_len = 0;
        if (data != null)
            data_len = data.length();

        if (data_len == 0) {
            if (source.equalsIgnoreCase(SOURCE_MSR)) {
                byte[] rawData = i.getByteArrayExtra(MSR_DATA_TAG);
                if (rawData != null) {
                    data = DATA_MSR;    //convert(rawData);
                    data_len = data.length();
                }
            }
        }

        if (data_len > 0) {
            // Update counts
            totalTags++;
            uniqueTags.add(data);
            updateCountUI();

            TextView tv = (TextView) findViewById(R.id.output_view);
            if (tvText.length() > 0) {
                tvText += ENDLINE_CHAR;
            }
            tvText += data;

            tv.setText(tvText);
            ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
            sv.post(() -> {
                ScrollView svr = (ScrollView) findViewById(R.id.scrollView1);
                svr.fullScroll(View.FOCUS_DOWN);
            });

            // Dismiss progress and stop timer when data is received
            if (SOURCE_SCANNER.equalsIgnoreCase(source)) {
                stopBarcodeScan();
            }
            // For RFID we keep progress until timeout or manual stop
        }
        setIntent(null);
        mDataIntent = null;
    }

    /**
     * AboutDialogBuilder class uses for create a about dialog box view
     * which shows description about the application and the copyright
     * information
     */
    public static class AboutDialogBuilder {
        public static AlertDialog create(Context context) {

            // Try to load the a package matching the name of our own package
            PackageInfo pInfo;
            String versionInfo = "3.1";
            int msgPadding = 5;

            String aboutTitle = context.getString(R.string.dwdemo2_about_title);
            String versionString = String.format(context.getString(R.string.dwdemo2_about_version), versionInfo);

            String aboutText = context.getString(R.string.dwdemo2_about_text);
            String copyright = context.getString(R.string.dwdemo_copyright);
            String allrights = context.getString(R.string.dwdemo2_about_allrights);

            // Set up the TextView
            final TextView message = new TextView(context);
            // We'll use a spannablestring to be able to make links clickable
            final SpannableString aboutString = new SpannableString(aboutText);

            // Set some padding
            message.setPadding(msgPadding, msgPadding, msgPadding, msgPadding);

            // Set up the final string
            StringBuilder messageText = new StringBuilder();
            messageText.append(versionString);
            messageText.append(ENDLINE_CHAR);
            messageText.append(ENDLINE_CHAR);
            messageText.append(aboutString);
            messageText.append(ENDLINE_CHAR);
            messageText.append(ENDLINE_CHAR);
            messageText.append(copyright);
            messageText.append(ENDLINE_CHAR);
            messageText.append(allrights);

            message.setText(messageText);
            message.setTextColor(context.getResources().getColor(R.color.white2));

            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(message, 0);

            AlertDialog.Builder adb = new AlertDialog.Builder(context);
            adb.setTitle(aboutTitle);
            adb.setCancelable(true);
            adb.setIcon(R.drawable.rfid_icon_s);
            adb.setPositiveButton(context.getString(android.R.string.ok), null);
            adb.setView(scrollView);
            return adb.create();

        }
    }

    /**
     * Sets the specified image button to the given state,
     * while "graying-out" the icon as well
     *
     * @param enabled   The state of the item
     * @param item      The item to modify
     * @param iconResId The icon ID
     */
    public static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item, int iconResId) {
        item.setEnabled(enabled);
        Drawable originalIcon = ctxt.getResources().getDrawable(iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a
     * Gray image. This method may be used to simulate the colour of
     * disable icons in JB ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter applied.
     */
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, Mode.SRC_IN);
        return res;
    }

    /**
     * Enables the soft button when the DW profile is switched to RWDemo.
     * Receiving the result for get the available profiles on datawedge
     * through intent API. If the RWDemo profile is not among them
     * create the RWDemo profile
     */
    private BroadcastReceiver datawedgeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (intent.hasExtra(RESULT_GET_ACTIVE_PROFILE)) {
                String activeProfile = intent.getStringExtra(RESULT_GET_ACTIVE_PROFILE);
                if(BUNDLE_EXTRA_PROFILE_NAME_VAL.equals(activeProfile)) {
                    if (DEBUG) Log.d(TAG, "ECRT: DW Activated Profile = " + activeProfile);
                    
                    if (!rwDemoProfileActivated) {
                        playSuccessBeep();
                    }
                    
                    rwDemoProfileActivated = true;
                    softScanTrigger.setEnabled(true);
                    softScanTrigger.setAlpha(enabledButtonAlphaVlue);
                    barcodeScanTrigger.setEnabled(true);
                    barcodeScanTrigger.setAlpha(enabledButtonAlphaVlue);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    rfidScanState = false;
                    barcodeScanState = false;

                    // Update UI to Activated
                    updateStatusUI(rfidStatusText, R.string.status_rfid, STATUS_ACTIVATED);

                    // Query status after profile activation
                    queryStatus();
                }
            }

            if (intent.hasExtra(RWDemoIntentParams.GET_AVAILABLE_PROFILE_LIST_RESULT)) {
                String[] profiles = intent.getExtras().getStringArray(RWDemoIntentParams.GET_AVAILABLE_PROFILE_LIST_RESULT);
                if (profiles != null) {
                    List<String> profileList = Arrays.asList(profiles);
                    if (DEBUG) {
                        for (String profile : profiles) Log.d(TAG, "ECRT: Found DW Profile = " + profile);
                    }
                    if(!profileList.contains(RWDemoIntentParams.BUNDLE_EXTRA_PROFILE_NAME_VAL)) {
                        createProfile();
                    }
                }
            }

            if (intent.hasExtra(RESULT_SCANNER_STATUS)) {
                String status = intent.getStringExtra(RESULT_SCANNER_STATUS);
                if (DEBUG) Log.d(TAG, "ECRT: Scanner Status (Query Result) = " + status);
                updateStatusUI(scannerStatusText, R.string.status_scanner, status);
                if ("WAITING".equalsIgnoreCase(status)) dismissProgressDialog();
            }

            if (intent.hasExtra(RESULT_RFID_STATUS)) {
                String status = intent.getStringExtra(RESULT_RFID_STATUS);
                if (DEBUG) Log.d(TAG, "ECRT: Rfid Status (Query Result) = " + status);
                updateStatusUI(rfidStatusText, R.string.status_rfid, status);
                if ("WAITING".equalsIgnoreCase(status)) dismissProgressDialog();
            }

            if (intent.hasExtra(EXTRA_RESULT)) {
                String result = intent.getStringExtra(EXTRA_RESULT);
                String commandId = intent.getStringExtra(EXTRA_COMMAND_IDENTIFIER);
                if (DEBUG) Log.d(TAG, "ECRT: Command RESULT = " + result + " ID = " + commandId);

                if ("RFID_CONFIG".equals(commandId)) {
                    if ("SUCCESS".equalsIgnoreCase(result)) {
                        updateStatusUI(rfidStatusText, R.string.status_rfid, STATUS_ACTIVATED);
                    } else {
                        updateStatusUI(rfidStatusText, R.string.status_rfid, STATUS_DISCONNECTED);
                    }
                }
            }

            if (action.equals(NOTIFICATION_ACTION)) {
                if (intent.hasExtra(NOTIFICATION_BUNDLE)) {
                    Bundle b = intent.getBundleExtra(NOTIFICATION_BUNDLE);
                    String notificationType = b.getString(KEY_NOTIFICATION_TYPE);
                    String status = b.getString(KEY_NOTIFICATION_STATUS);
                    
                    if (DEBUG) Log.d(TAG, "ECRT: Notification: Type=" + notificationType + " Status=" + status);

                    if (notificationType != null) {
                        if (notificationType.equals(NOTIFICATION_TYPE_SCANNER_STATUS)) {
                            updateStatusUI(scannerStatusText, R.string.status_scanner, status);
                        } else if (notificationType.equals(NOTIFICATION_TYPE_RFID_STATUS)) {
                            updateStatusUI(rfidStatusText, R.string.status_rfid, status);
                        }
                    }
                }
            }
        }
    };

    /**
     * Dynamically register broadcast receiver
     */
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESULT_ACTION);
        filter.addAction(NOTIFICATION_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(datawedgeBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(datawedgeBroadcastReceiver, filter);
        }
    }

    private void registerForNotifications() {
        Bundle b = new Bundle();
        b.putString(BUNDLE_EXTRA_APPLICATION_NAME, getPackageName());
        b.putString(BUNDLE_EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_SCANNER_STATUS);
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(ACTION_EXTRA_REGISTER_FOR_NOTIFICATION, b);
        sendBroadcast(i);

        Bundle b2 = new Bundle();
        b2.putString(BUNDLE_EXTRA_APPLICATION_NAME, getPackageName());
        b2.putString(BUNDLE_EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_RFID_STATUS);
        Intent i2 = new Intent();
        i2.setAction(ACTION);
        i2.putExtra(ACTION_EXTRA_REGISTER_FOR_NOTIFICATION, b2);
        sendBroadcast(i2);
    }

    private void unRegisterForNotifications() {
        Bundle b = new Bundle();
        b.putString(BUNDLE_EXTRA_APPLICATION_NAME, getPackageName());
        b.putString(BUNDLE_EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_SCANNER_STATUS);
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(ACTION_EXTRA_UNREGISTER_FOR_NOTIFICATION, b);
        sendBroadcast(i);

        Bundle b2 = new Bundle();
        b2.putString(BUNDLE_EXTRA_APPLICATION_NAME, getPackageName());
        b2.putString(BUNDLE_EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_RFID_STATUS);
        Intent i2 = new Intent();
        i2.setAction(ACTION);
        i2.putExtra(ACTION_EXTRA_UNREGISTER_FOR_NOTIFICATION, b2);
        sendBroadcast(i2);
    }

    private void queryStatus() {
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(ACTION_EXTRA_GET_SCANNER_STATUS, "");
        sendBroadcast(i);

        Intent i2 = new Intent();
        i2.setAction(ACTION);
        i2.putExtra(ACTION_EXTRA_GET_RFID_STATUS, "");
        sendBroadcast(i2);
    }

    private void updateStatusUI(TextView textView, int stringResId, String status) {
        runOnUiThread(() -> {
            if (status == null) return;

            String displayStatus = status;
            // Map RFID statuses to READING/STOPPED for display
            if (textView == rfidStatusText) {
                if (STATUS_SCANNING.equalsIgnoreCase(status)) {
                    displayStatus = getString(R.string.status_reading);
                    rfidScanState = true;
                } else {
                    displayStatus = getString(R.string.status_stopped);
                    rfidScanState = false;
                    // Cancel timer if hardware stopped externally
                    if (rfidTimeoutRunnable != null) {
                        timeoutHandler.removeCallbacks(rfidTimeoutRunnable);
                        rfidTimeoutRunnable = null;
                    }
                }
            } else if (textView == scannerStatusText) {
                if (!STATUS_SCANNING.equalsIgnoreCase(status)) {
                    barcodeScanState = false;
                    // Cancel timer if hardware stopped externally
                    if (barcodeTimeoutRunnable != null) {
                        timeoutHandler.removeCallbacks(barcodeTimeoutRunnable);
                        barcodeTimeoutRunnable = null;
                    }
                }
            }

            textView.setText(getString(stringResId, displayStatus));
            
            if (status.equalsIgnoreCase(STATUS_WAITING) || status.equalsIgnoreCase(STATUS_CONNECTED) || status.equalsIgnoreCase(STATUS_SCANNING) ||
                displayStatus.equalsIgnoreCase(getString(R.string.status_reading)) || displayStatus.equalsIgnoreCase(getString(R.string.status_stopped))) {
                textView.setTextColor(getResources().getColor(R.color.status_green));
            } else if (status.equalsIgnoreCase(STATUS_ACTIVATED)) {
                textView.setTextColor(getResources().getColor(R.color.status_blue));
            } else {
                textView.setTextColor(getResources().getColor(R.color.status_red));
            }
            
            // Fail-safe dismiss progress if status is WAITING or IDLE
            if (STATUS_WAITING.equalsIgnoreCase(status) || "IDLE".equalsIgnoreCase(status)) {
                dismissProgressDialog();
            }
        });
    }

    private void showProgressDialog(String message) {
        runOnUiThread(() -> {
            if (progressOverlay != null) {
                progressMessageText.setText(message);
                progressOverlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void dismissProgressDialog() {
        runOnUiThread(() -> {
            if (progressOverlay != null) {
                progressOverlay.setVisibility(View.GONE);
            }
        });
    }

    private void playAlarmBeep(int count) {
        new Thread(() -> {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            for (int i = 0; i < count; i++) {
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            toneGen.release();
        }).start();
    }

    private void playSuccessBeep() {
        new Thread(() -> {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 400);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            toneGen.release();
        }).start();
    }

}
