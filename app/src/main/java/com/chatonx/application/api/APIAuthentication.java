package com.chatonx.application.api;


import com.chatonx.application.app.EndPoints;
import com.chatonx.application.models.auth.JoinModelResponse;
import com.chatonx.application.models.auth.LoginModel;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Olatech
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */
public interface APIAuthentication {
    /**
     * method to join
     *
     * @param loginModel this is parameter for join method
     */

    @POST(EndPoints.JOIN)
    Observable<JoinModelResponse> join(@Body LoginModel loginModel);

    /**
     * method to resend SMS request
     *
     * @param phone this is parameter for resend method
     */

    @FormUrlEncoded
    @POST(EndPoints.RESEND_REQUEST_SMS)
    Observable<JoinModelResponse> resend(@Field("phone") String phone);

    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.VERIFY_USER)
    Observable<JoinModelResponse> verifyUser(@Field("code") String code);


}
