package com.chatonx.application.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.chatonx.application.R;
import com.chatonx.application.activities.main.welcome.WelcomeActivity;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.backup.RealmBackupRestore;
import com.chatonx.application.helpers.ForegroundRuning;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.NetworkListener;
import com.chatonx.application.services.MainService;

/**
 * Created by Abderrahim El imame on 8/18/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NetworkChangeListener extends BroadcastReceiver {

    private boolean is_Connected = false;
    public static NetworkListener networkListener;

    public NetworkChangeListener() {
        super();
    }

    @Override
    public void onReceive(Context mContext, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (PreferenceManager.getToken(mContext) != null) {
                APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
                    if (networkModel.isConnected()) {
                        is_Connected = true;
                    } else {
                        is_Connected = false;

                        if (ForegroundRuning.get().isForeground()) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                            alert.setMessage(R.string.your_session_expired);
                            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                                PreferenceManager.setToken(mContext, null);
                                PreferenceManager.setID(mContext, 0);
                                PreferenceManager.setSocketID(mContext, null);
                                PreferenceManager.setPhone(mContext, null);
                                PreferenceManager.setIsWaitingForSms(mContext, false);
                                PreferenceManager.setMobileNumber(mContext, null);
                                RealmBackupRestore.deleteData(mContext);
                                AppHelper.deleteCache(mContext);
                                Intent mIntent1 = new Intent(mContext, WelcomeActivity.class);
                                mIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(ChatonXApplication.getInstance().getBaseContext(), 0, mIntent1, PendingIntent.FLAG_ONE_SHOT);
                                AlarmManager mgr = (AlarmManager) ChatonXApplication.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
                                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                                System.exit(2);
                            });
                            alert.setCancelable(false);
                            alert.show();
                        }
                    }
                }, throwable -> {
                    is_Connected = false;
                });


                new Handler().postDelayed(() -> isNetworkAvailable(mContext), 2000);
            }
        }
    }

    private void isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean is_Connecting = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (networkListener != null) {
            networkListener.onNetworkConnectionChanged(is_Connecting, is_Connected);
        }

        if (!is_Connecting && !is_Connected) {
            mContext.getApplicationContext().stopService(new Intent(mContext.getApplicationContext(), MainService.class));
            AppHelper.LogCat("Connection is not available");
        } else if (is_Connecting && is_Connected) {
            AppHelper.LogCat("Connection is available");
            AppHelper.restartService();
            new Handler().postDelayed(() -> {
                MainService.unSentMessages(mContext);
            }, 2000);
        } else {
            AppHelper.LogCat("Connection is available but waiting for network");
            //mContext.getApplicationContext().stopService(new Intent(mContext.getApplicationContext(), MainService.class));
        }
    }
}
