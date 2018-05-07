package com.chatonx.application.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;

import com.chatonx.application.R;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.models.RegisterIdModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.socket.client.Socket;

public class PointActivity extends Activity {




    @Override

    protected void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_point);

    }
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
