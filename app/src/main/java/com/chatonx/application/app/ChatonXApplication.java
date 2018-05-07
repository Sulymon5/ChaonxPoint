package com.chatonx.application.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.accountkit.AccountKit;
import com.orhanobut.logger.Logger;
import com.chatonx.application.BuildConfig;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.ExceptionHandler;
import com.chatonx.application.helpers.Files.backup.Backup;
import com.chatonx.application.helpers.Files.backup.GoogleDriveBackupHandler;
import com.chatonx.application.helpers.Files.backup.RealmMigrations;
import com.chatonx.application.helpers.ForegroundRuning;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.NetworkListener;
import com.chatonx.application.receivers.NetworkChangeListener;
import com.chatonx.application.services.BootService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ChatonXApplication extends MultiDexApplication {
    public static volatile Handler applicationHandler = null;
    static ChatonXApplication mInstance;
    public static final long TIMEOUT = 60 * 1000;
    private static Socket mSocket = null;

    public static void connectSocket() {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.timeout = TIMEOUT; //set -1 to  disable it
        options.reconnection = true;
        options.reconnectionDelay = (long) 3000;
        options.reconnectionDelayMax = (long) 60000;
        options.reconnectionAttempts = 99999;
        options.query = "token=" + AppConstants.APP_KEY_SECRET;


        try {
            mSocket = IO.socket(new URI(EndPoints.BACKEND_CHAT_SERVER_URL), options);
        } catch (URISyntaxException e) {
            AppHelper.LogCat("URISyntaxException" + e.getMessage());
        }
        if (!mSocket.connected())
            mSocket.connect();

    }


    public Socket getSocket() {
        return mSocket;
    }

    public static synchronized ChatonXApplication getInstance() {
        return mInstance;
    }

    public void setmInstance(ChatonXApplication mInstance) {
        ChatonXApplication.mInstance = mInstance;
    }

    public static void setupCrashlytics() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build();
        Fabric.with(mInstance, crashlyticsKit, new Crashlytics());
        Crashlytics.setUserEmail(PreferenceManager.getPhone(getInstance()));
        Crashlytics.setUserName(PreferenceManager.getPhone(getInstance()));
        Crashlytics.setUserIdentifier(String.valueOf(PreferenceManager.getID(getInstance())));

    }

    @Override
    public void onCreate() {
        super.onCreate();
        setmInstance(this);
        applicationHandler = new Handler(getApplicationContext().getMainLooper());
        if (AppConstants.ENABLE_FACEBOOK_ACCOUNT_KIT)
            AccountKit.initialize(getApplicationContext(), () -> AppHelper.LogCat(" AccountKit onInitialized "));
        if (AppConstants.CRASH_LYTICS)
            ChatonXApplication.setupCrashlytics();
        initRealm();
        ForegroundRuning.init(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BootService.class));

        } else {
            startService(new Intent(this, BootService.class));
        }



        if (AppConstants.DEBUGGING_MODE)
            Logger.init(AppConstants.TAG).hideThreadInfo();

        if (AppConstants.ENABLE_CRASH_HANDLER)
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (!PreferenceManager.getLanguage(this).equals(""))
            setDefaultLocale(this, new Locale(PreferenceManager.getLanguage(this)));
        else {
            if (Locale.getDefault().toString().startsWith("en_")) {
                PreferenceManager.setLanguage(this, "en");
            }
        }

    }


    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }

    public void setConnectivityListener(NetworkListener listener) {
        NetworkChangeListener.networkListener = listener;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // MainService.disconnectSocket();
        if (!getRealmDatabaseInstance().isClosed()) {
            getRealmDatabaseInstance().close();
        }
    }

    @NonNull
    public Backup getBackup() {
        return new GoogleDriveBackupHandler();
    }


    @SuppressLint("DefaultLocale")
    public static RealmConfiguration getRealmDatabaseConfiguration() {
        return new RealmConfiguration
                .Builder()
                .name(AppConstants.DatabaseName)
                .schemaVersion(AppConstants.DatabaseVersion)
                .migration(new RealmMigrations())
                .build();
    }

    public static Realm getRealmDatabaseInstance() {
        return Realm.getInstance(getRealmDatabaseConfiguration());
    }

    public static void DeleteRealmDatabaseInstance() {
        Realm.deleteRealm(getRealmDatabaseConfiguration());
    }

    public void initRealm() {
        Realm.init(this);
    }

}
