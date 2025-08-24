package com.ahlibank.tokenization;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import java.util.Map;

public class D1HmsMessageService extends HmsMessageService {

    private static final String TAG = "Outsystems==> Huawei : " + D1HmsMessageService.class.getSimpleName();
    private static final String HMS_TOKEN_PREFIX = "HMS:";

    private D1Task d1Task = null;
    private boolean isHuaweiMainProvider = true;

    // Stubbed implementations
    private AppPushServiceListener mPushServiceListener;
    private FcmApiAdapter mFcmAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        // Manually initialize stubs
        mPushServiceListener = new AppPushServiceListener();
        mFcmAdapter = new FcmApiAdapter();

        if (mFcmAdapter != null && mFcmAdapter.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            isHuaweiMainProvider = false;
            Log.i(TAG, "Google Play Services available. FCM is preferred over HMS.");
        } else {
            Log.i(TAG, "Huawei Push Kit is the main push provider.");
        }
    }

    @Override
    public void onNewToken(String token) {
        if (!isHuaweiMainProvider) {
            Log.i(TAG, "Ignoring HMS token as FCM is the main provider.");
            return;
        }

        final String finalToken = HMS_TOKEN_PREFIX + token;
        Log.i(TAG, "Huawei Push Token: " + finalToken);

        if (mPushServiceListener != null) {
            mPushServiceListener.updateToken(finalToken);
        }

        if (d1Task == null) {
            d1Task = new D1Task(getApplicationContext());
        }

        d1Task.updatePushToken(token, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(@Nullable Void ignored) {
                Log.i(TAG, "Huawei UpdatePushToken Success: " + token);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                Log.e(TAG, "Huawei UpdatePushToken On Error: " + exception.toString());
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!isHuaweiMainProvider) {
            Log.i(TAG, "Ignoring HMS message as FCM is the main provider.");
            return;
        }

        Map<String, String> dataMap = remoteMessage.getDataOfMap();
        Log.i(TAG, "Huawei onMessageReceived: " + dataMap);

        if (mPushServiceListener != null && dataMap != null && !dataMap.isEmpty()) {
            mPushServiceListener.onMessageReceived(dataMap);
        }

        if (d1Task == null) {
            d1Task = new D1Task(getApplicationContext());
        }

        if (dataMap != null && !dataMap.isEmpty()) {
            d1Task.processNotification(dataMap, new D1Task.Callback<Map<String, String>>() {
                @Override
                public void onSuccess(Map<String, String> response) {
                    Log.i(TAG, "Huawei ProcessNotification OnSuccess: " + response);
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Log.e(TAG, "Huawei ProcessNotification OnError: " + e.toString());
                }
            });
        } else {
            Log.w(TAG, "Received empty HMS data payload.");
        }
    }

    // ======= Stub Classes =======

    public static class AppPushServiceListener {
        public void updateToken(String token) {
            Log.d(TAG, "Stub: updateToken called with " + token);
        }

        public void onMessageReceived(Map<String, String> dataMap) {
            Log.d(TAG, "Stub: onMessageReceived called with data: " + dataMap);
        }
    }

    public static class FcmApiAdapter {
        public int isGooglePlayServicesAvailable(Context context) {
            return ConnectionResult.SERVICE_MISSING; // Or ConnectionResult.SUCCESS to simulate FCM availability
        }
    }

    public static class D1Task {
        private final Context context;

        public D1Task(Context context) {
            this.context = context;
        }

        public void updatePushToken(String token, Callback<Void> callback) {
            // Stub logic
            Log.d(TAG, "Stub: updatePushToken called");
            callback.onSuccess(null);
        }

        public void processNotification(Map<String, String> dataMap, Callback<Map<String, String>> callback) {
            // Stub logic
            Log.d(TAG, "Stub: processNotification called");
            callback.onSuccess(dataMap);
        }

        public interface Callback<T> {
            void onSuccess(T result);
            void onError(@NonNull Exception e);
        }
    }
}
