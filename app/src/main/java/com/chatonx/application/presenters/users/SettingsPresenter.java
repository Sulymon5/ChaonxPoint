package com.chatonx.application.presenters.users;


import com.chatonx.application.activities.settings.SettingsActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.Presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class SettingsPresenter implements Presenter {
    private final SettingsActivity view;
    private final Realm realm;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;

    public SettingsPresenter(SettingsActivity settingsActivity) {
        this.view = settingsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        mDisposable = new CompositeDisposable();
        APIService mApiService = APIService.with(view);
        mUsersContacts = new UsersService(realm, view, mApiService);
        loadData();
    }

    public void loadData() {
        mDisposable.addAll(mUsersContacts.getContactInfo(PreferenceManager.getID(view)).subscribe(view::ShowContact, throwable -> {
            AppHelper.LogCat(throwable.getMessage());
        }))
        ;
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