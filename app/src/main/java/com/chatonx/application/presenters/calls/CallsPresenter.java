package com.chatonx.application.presenters.calls;

import com.chatonx.application.R;
import com.chatonx.application.activities.call.CallDetailsActivity;
import com.chatonx.application.activities.call.IncomingCallActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.fragments.home.CallsFragment;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.interfaces.Presenter;
import com.chatonx.application.models.calls.CallsInfoModel;
import com.chatonx.application.models.calls.CallsModel;
import com.chatonx.application.models.users.Pusher;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallsPresenter implements Presenter {

    private CallsFragment callsFragment;
    private CallDetailsActivity callDetailsActivity;
    private IncomingCallActivity incomingCallActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private int userID;
    private int callID;
    private CompositeDisposable mDisposable;

    public Realm getRealm() {
        return realm;
    }

    public CallsPresenter(CallsFragment callsFragment) {
        this.callsFragment = callsFragment;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }

    public CallsPresenter(IncomingCallActivity incomingCallActivity, int userID) {
        this.incomingCallActivity = incomingCallActivity;
        this.userID = userID;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    public CallsPresenter(CallDetailsActivity callDetailsActivity) {
        this.callDetailsActivity = callDetailsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        if (incomingCallActivity != null) {
            APIService mApiService = APIService.with(incomingCallActivity);
            mUsersContacts = new UsersService(realm, incomingCallActivity, mApiService);
            getCallerInfo(userID);
        } else if (callDetailsActivity != null) {
            APIService mApiService = APIService.with(callDetailsActivity);
            mUsersContacts = new UsersService(realm, callDetailsActivity, mApiService);
            callID = callDetailsActivity.getIntent().getIntExtra("callID", 0);
            userID = callDetailsActivity.getIntent().getIntExtra("userID", 0);


            getCallerDetailsInfo(userID);
            getCallDetails(callID);
            getCallsDetailsList(callID);
        } else {
            if (!EventBus.getDefault().isRegistered(callsFragment))
                EventBus.getDefault().register(callsFragment);
            APIService mApiService = APIService.with(callsFragment.getActivity());
            mUsersContacts = new UsersService(realm, callsFragment.getActivity(), mApiService);
            getCallsList();
        }
    }

    private void getCallerDetailsInfo(int userID) {
        mDisposable.addAll(mUsersContacts.getContactInfo(userID).subscribe(contactsModel -> {
            callDetailsActivity.showUserInfo(contactsModel);
        }, throwable -> {
            AppHelper.LogCat(throwable.getMessage());
        }))
        ;

    }

    private void getCallDetails(int callID) {
        mDisposable.addAll(
                mUsersContacts.getCallDetails(callID).subscribe(callsModel -> {
                    callDetailsActivity.showCallInfo(callsModel);
                }, AppHelper::LogCat));
    }

    private void getCallsDetailsList(int callID) {

        try {
            mDisposable.addAll(
                    mUsersContacts.getAllCallsDetails(callID).subscribe(callsInfoModels -> {
                        callDetailsActivity.UpdateCallsDetailsList(callsInfoModels);
                    }, AppHelper::LogCat));

        } catch (Exception e) {
            AppHelper.LogCat("calls presenter " + e.getMessage());
        }
    }

    private void getCallsList() {

        callsFragment.onShowLoading();
        try {
            mDisposable.addAll(
                    mUsersContacts.getAllCalls().subscribe(callsModels -> {
                        callsFragment.UpdateCalls(callsModels);
                        callsFragment.onHideLoading();
                    }, callsFragment::onErrorLoading, callsFragment::onHideLoading));

        } catch (Exception e) {
            AppHelper.LogCat("calls presenter " + e.getMessage());
        }
    }

    private void getCallerInfo(int userID) {
        mDisposable.addAll(
                mUsersContacts.getContactInfo(userID).subscribe(contactsModel -> {
                    incomingCallActivity.showUserInfo(contactsModel);
                }, throwable -> {
                    AppHelper.LogCat(throwable.getMessage());
                }));
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (callsFragment != null)
            EventBus.getDefault().unregister(callsFragment);
        realm.close();
        if (mDisposable != null)
            mDisposable.dispose();
    }

    public void removeCall() {
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        AppHelper.showDialog(callDetailsActivity, callDetailsActivity.getString(R.string.delete_call_dialog));
        realm.executeTransactionAsync(realm1 -> {
            CallsModel callsModel = realm1.where(CallsModel.class).equalTo("id", callID).findFirst();
            RealmResults<CallsInfoModel> callsInfoModel = realm1.where(CallsInfoModel.class).equalTo("callId", callsModel.getId()).findAll();
            callsInfoModel.deleteAllFromRealm();
            callsModel.deleteFromRealm();
        }, () -> {
            AppHelper.hideDialog();
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CALL_ITEM, callID));
            callDetailsActivity.finish();

        }, error -> {
            AppHelper.LogCat(error.getMessage());
            AppHelper.hideDialog();
        });

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {
        getCallsList();
    }

    @Override
    public void onStop() {

    }
}
