package com.chatonx.application.presenters.users;


import com.chatonx.application.activities.search.SearchContactsActivity;
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
public class SearchContactsPresenter implements Presenter {
    private SearchContactsActivity mSearchContactsActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;

    public SearchContactsPresenter(SearchContactsActivity mSearchContactsActivity) {
        this.mSearchContactsActivity = mSearchContactsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        APIService mApiService = APIService.with(this.mSearchContactsActivity);
        mUsersContacts = new UsersService(realm, this.mSearchContactsActivity, mApiService);
        loadLocalData();
    }

    private void loadLocalData() {
        mDisposable.addAll(mUsersContacts.getAllContacts().subscribe(mSearchContactsActivity::ShowContacts, mSearchContactsActivity::onErrorLoading))
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