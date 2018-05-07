package com.chatonx.application.api.apiServices;

import android.content.Context;

import com.chatonx.application.api.APIAuthentication;
import com.chatonx.application.api.APIService;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.models.auth.JoinModelResponse;
import com.chatonx.application.models.auth.LoginModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by olatech mac
 * */

public class AuthService {

    private APIAuthentication apiAuthentication;
    private Context mContext;
    private APIService mApiService;


    public AuthService(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;
    }

    /**
     * method to initialize the api auth
     *
     * @return return value
     */
    private APIAuthentication initializeApiAuth() {
        if (apiAuthentication == null) {
            apiAuthentication = this.mApiService.AuthService(APIAuthentication.class, EndPoints.BACKEND_BASE_URL);
        }
        return apiAuthentication;
    }

    public Observable<JoinModelResponse> join(LoginModel loginModel) {
        return initializeApiAuth().join(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<JoinModelResponse> resend(String phone) {
        return initializeApiAuth().resend(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<JoinModelResponse> verifyUser(String code) {
        return initializeApiAuth().verifyUser(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
