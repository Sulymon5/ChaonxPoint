package com.chatonx.application.presenters.calls;


import com.chatonx.application.activities.search.SearchCallsActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.interfaces.Presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SearchCallsPresenter implements Presenter {
    private SearchCallsActivity mSearchCallsActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;

    public SearchCallsPresenter(SearchCallsActivity mSearchCallsActivity) {
        this.mSearchCallsActivity = mSearchCallsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        APIService mApiService = APIService.with(this.mSearchCallsActivity);
        mUsersContacts = new UsersService(realm, this.mSearchCallsActivity, mApiService);
        loadLocalData();
    }

    private void loadLocalData() {
        mDisposable.addAll(mUsersContacts.getAllCalls().subscribe(mSearchCallsActivity::ShowCalls, mSearchCallsActivity::onErrorLoading));
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