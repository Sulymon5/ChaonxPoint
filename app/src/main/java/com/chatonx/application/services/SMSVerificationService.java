package com.chatonx.application.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.chatonx.application.activities.main.MainActivity;
import com.chatonx.application.activities.main.PreMainActivity;
import com.chatonx.application.activities.main.welcome.CompleteRegistrationActivity;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 23/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SMSVerificationService extends IntentService {


    public SMSVerificationService() {
        super(SMSVerificationService.class.getSimpleName());
    }

   // private CompositeDisposable mDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
      //  mDisposable = new CompositeDisposable();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String code = intent.getStringExtra("code");
            boolean registration = intent.getBooleanExtra("register", true);
            verifyUser(code, registration);
        }
    }

    @SuppressLint("CheckResult")
    private void verifyUser(String code, boolean registration) {
        if (registration) {
          /*  mDisposable.add(*/APIHelper.initializeAuthService().verifyUser(code).subscribe(joinModelResponse -> {
                if (joinModelResponse.isSuccess()) {
                    PreferenceManager.setIsNewUser(SMSVerificationService.this, true);
                    PreferenceManager.setID(SMSVerificationService.this, joinModelResponse.getUserID());
                    PreferenceManager.setToken(SMSVerificationService.this, joinModelResponse.getToken());
                    PreferenceManager.setIsWaitingForSms(SMSVerificationService.this, false);
                    PreferenceManager.setPhone(SMSVerificationService.this, PreferenceManager.getMobileNumber(SMSVerificationService.this));
                    if (joinModelResponse.isHasBackup()) {
                        PreferenceManager.saveBackupFolder(SMSVerificationService.this, joinModelResponse.getBackup_hash());
                        PreferenceManager.setHasBackup(SMSVerificationService.this, true);
                        Intent intent = new Intent(SMSVerificationService.this, PreMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (joinModelResponse.isHasProfile()) {
                        PreferenceManager.setHasBackup(SMSVerificationService.this, false);
                        PreferenceManager.setIsNeedInfo(SMSVerificationService.this, false);
                        Intent intent = new Intent(SMSVerificationService.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        PreferenceManager.setHasBackup(SMSVerificationService.this, false);
                        PreferenceManager.setIsNeedInfo(SMSVerificationService.this, true);
                        Intent intent = new Intent(SMSVerificationService.this, CompleteRegistrationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeWelcomeActivity"));

                } else {
                    AppHelper.CustomToast(SMSVerificationService.this, joinModelResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat("SMS verification failure  SMSVerificationService" + throwable.getMessage());
            })
            ;
        } else {
           /* mDisposable.add(*/APIHelper.initialApiUsersContacts().deleteAccountConfirmation(code).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeDeleteAccountActivity"));
                } else {
                    AppHelper.CustomToast(SMSVerificationService.this, statusResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat("SMS verification failure  SMSVerificationService" + throwable.getMessage());
            })
            ;

        }
    }
}
