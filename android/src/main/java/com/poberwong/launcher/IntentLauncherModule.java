package com.poberwong.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.*;

import java.io.Console;
import java.util.Set;
import java.util.Iterator;

/**
 * Created by poberwong on 16/6/30.
 */
public class IntentLauncherModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final int REQUEST_CODE = 12;
    private static final String ATTR_ACTION = "action";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_CATEGORY = "category";
    private static final String TAG_EXTRA = "extra";
    private static final String ATTR_DATA = "data";
    private static final String ATTR_FLAGS = "flags";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_CLASS_NAME = "className";
    Promise promise;
    ReactApplicationContext reactContext;

    public IntentLauncherModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "IntentLauncher";
    }

    /**
     * 选用方案
     * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     * getReactApplicationContext().startActivity(intent);
     */
    @ReactMethod
    public void startActivity(ReadableMap params, final Promise promise) {
        this.promise = promise;
        Intent intent = new Intent();

        if (params.hasKey(ATTR_CLASS_NAME)) {
            ComponentName cn;
            if (params.hasKey(ATTR_PACKAGE_NAME)) {
                cn = new ComponentName(params.getString(ATTR_PACKAGE_NAME), params.getString(ATTR_CLASS_NAME));
            } else {
                cn = new ComponentName(getReactApplicationContext(), params.getString(ATTR_CLASS_NAME));
            }
            intent.setComponent(cn);
        }
        if (params.hasKey(ATTR_ACTION)) {
            intent.setAction(params.getString(ATTR_ACTION));
        }
        if (params.hasKey(ATTR_DATA)) {
            intent.setData(Uri.parse(params.getString(ATTR_DATA)));
        }
        if (params.hasKey(ATTR_TYPE)) {
            intent.setType(params.getString(ATTR_TYPE));
        }
        if (params.hasKey(TAG_EXTRA)) {
            intent.putExtras(Arguments.toBundle(params.getMap(TAG_EXTRA)));
        }
        if (params.hasKey(ATTR_FLAGS)) {
            intent.addFlags(params.getInt(ATTR_FLAGS));
        }
        if (params.hasKey(ATTR_CATEGORY)) {
            intent.addCategory(params.getString(ATTR_CATEGORY));
        }
        getReactApplicationContext().startActivityForResult(intent, REQUEST_CODE, null); // 暂时使用当前应用的任务栈
    }

    @ReactMethod
    public void startOtherApp(ReadableMap params, final Promise promise) {
        this.promise = promise;

        if (params.hasKey(ATTR_PACKAGE_NAME)) {
            Intent launchIntent = this.reactContext.getPackageManager().getLaunchIntentForPackage(params.getString(ATTR_PACKAGE_NAME));
            if (launchIntent != null) {
                getReactApplicationContext().startActivity(launchIntent); // null pointer check in case package name was not found
                return;
            } else {
                this.promise.reject("could not start app");
                return;
            }
        }
        this.promise.reject("package name missing");
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        WritableMap params = Arguments.createMap();
        if (intent != null) {
            params.putInt("resultCode", resultCode);

            Uri data = intent.getData();
            if (data != null) {
                params.putString("data", data.toString());
            }

            Bundle extras = intent.getExtras();
            if (extras != null) {
                params.putMap("extra", Arguments.fromBundle(extras));
            }
        }

        this.promise.resolve(params);
    }
}
