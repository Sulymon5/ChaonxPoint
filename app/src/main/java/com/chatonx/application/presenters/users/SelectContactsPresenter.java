package com.chatonx.application.presenters.users;


import com.chatonx.application.activities.BlockedContactsActivity;
import com.chatonx.application.activities.messages.TransferMessageContactsActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.interfaces.Presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SelectContactsPresenter implements Presenter {
    private TransferMessageContactsActivity transferMessageContactsActivity;
    private BlockedContactsActivity blockedContactsActivity;
    private Realm realm;
    private CompositeDisposable mDisposable;

    public SelectContactsPresenter(TransferMessageContactsActivity transferMessageContactsActivity) {
        this.transferMessageContactsActivity = transferMessageContactsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    public SelectContactsPresenter(BlockedContactsActivity blockedContactsActivity) {
        this.blockedContactsActivity = blockedContactsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        if (transferMessageContactsActivity != null) {
            APIService mApiService = APIService.with(this.transferMessageContactsActivity);
            UsersService mUsersContacts = new UsersService(realm, this.transferMessageContactsActivity, mApiService);
            mDisposable.addAll(mUsersContacts.getLinkedContacts().subscribe(transferMessageContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            }))
            ;
        } else {
            APIService mApiService = APIService.with(this.blockedContactsActivity);
            UsersService mUsersContacts = new UsersService(realm, this.blockedContactsActivity, mApiService);
            mDisposable.addAll(mUsersContacts.getBlockedContacts().subscribe(blockedContactsActivity::ShowContacts, throwable -> {
                AppHelper.LogCat("Error contacts selector " + throwable.getMessage());
            }))
            ;
        }

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        realm.close();
        if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }
}