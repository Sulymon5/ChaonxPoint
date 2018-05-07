package com.chatonx.application.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.chatonx.application.adapters.recyclerView.contacts.BlockedContactsAdapter;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.models.RegisterIdModel;
import com.chatonx.application.models.users.contacts.ContactsModel;

import com.chatonx.application.R;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.models.users.contacts.ContactsModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import io.reactivex.disposables.Disposable;
import io.realm.DynamicRealm;
import io.socket.client.Socket;


public class ActivityMyPoints extends AppCompatActivity implements View.OnClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private TextView mContentView;
    private ProgressBar pointProgressBar;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);

            }
            //TODO: implement select chatonx contact to share point to
          Toast toast = Toast.makeText(ActivityMyPoints.this,"Coming soon....", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM,0,0);
            toast.show();
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_my_points);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.point_text_view);
        pointProgressBar = findViewById(R.id.points_progress_bar);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.share_point_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.withdraw_point_button).setOnClickListener(this);

    }

    /**
     * Performs call to the server on resume and get the logged in user's accumulated points.
     *
     *
     * */
    @Override
    protected void onResume() {
        super.onResume();
            //TODO: replace the static LOGGED_IN_USER with actual loggedin user id
            //TODO: replace server url with actual server url
            new PointNetwork(this,this).execute(
                    getString(R.string.get_user_point_url)+"points/get_user_points.php?user_id="+1);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }


    private void hide() {
        // Hide UI first
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        mControlsView.setVisibility(View.VISIBLE);
        mVisible = true;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.withdraw_point_button:
                Toast.makeText(this,"withdrawal is coming soon....",Toast.LENGTH_LONG).show();
                break;

        }
    }

    private static class PointNetwork extends AsyncTask<String,Void,String>{
        Context cxt;
        ActivityMyPoints activity;
        public PointNetwork(Context cxt,ActivityMyPoints activity){
            this.cxt = cxt;
            this.activity = activity;
        }
        @Override
        protected String doInBackground(String... strings) {
            return backgroundTask(strings);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null){
                try {
                    JSONObject obj = new JSONObject(s);
                    total_point =  obj.getDouble("point");

                    activity.pointProgressBar.setVisibility(View.GONE);
                    activity.mContentView.setVisibility(View.VISIBLE);
                    activity.mContentView.setText(String.format(Locale.getDefault(),"%f\nAP",total_point));


                } catch (JSONException e) {
                    activity.pointProgressBar.setVisibility(View.GONE);
                    Toast.makeText(cxt,"Unexpected error occurred",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    Log.e(ActivityMyPoints.class.getName()+":>"+JSONException.class.getName()+"",e.getMessage());
                }
            }else{
                Toast.makeText(cxt,"A network error occurred",Toast.LENGTH_LONG).show();
                activity.pointProgressBar.setVisibility(View.GONE);
            }

        }
    }

    public static String backgroundTask(@NonNull String... params){
        String line = "";
        try {
            URLConnection conn = new URL(params[0]).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);


            ObjectOutputStream objOut = new ObjectOutputStream(conn.getOutputStream());

            BufferedReader in = new BufferedReader
                    (new InputStreamReader(conn.getInputStream()));

            StringBuffer sb = new StringBuffer("");


            while ((line = in.readLine()) != null) {
                // if (isCancelled()) break;
                sb.append(line);

            }
            in.close();

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private static double total_point;

    private static final long LOGGED_IN_USER = 6;
    

    public void updateRegisteredId(String registeredId) {
        RegisterIdModel registerIdModel = new RegisterIdModel();
        registerIdModel.setRegisteredId(registeredId);
        List<Disposable> mDisposable = null;
        mDisposable.add(APIHelper.initialApiUsersContacts().updateRegisteredId(registerIdModel).subscribe(qResponse -> {
            if (qResponse.isSuccess()) {
                AppHelper.LogCat(qResponse.getMessage());
                JSONObject json = new JSONObject();
                try {
                    json.put("userId", PreferenceManager.getID(this));
                    json.put("register_id", qResponse.getRegistered_id());

                    Socket mSocket = null;
                    mSocket.emit(AppConstants.SOCKET_UPDATE_REGISTER_ID, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                AppHelper.LogCat(qResponse.getMessage());
            }

        }, throwable -> {
            AppHelper.LogCat(throwable.getMessage());
        }))
        ;

    }

}

