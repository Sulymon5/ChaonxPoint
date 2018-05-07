package com.chatonx.application.presenters.groups;

import com.chatonx.application.activities.groups.AddNewMembersToGroupActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.interfaces.Presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 26/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupPresenter implements Presenter {
    private AddNewMembersToGroupActivity view;
    private Realm realm;
    private CompositeDisposable mDisposable;

    public AddNewMembersToGroupPresenter(AddNewMembersToGroupActivity addMembersToGroupActivity) {
        this.view = addMembersToGroupActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        APIService mApiService = APIService.with(view);
        UsersService mUsersContacts = new UsersService(realm, view, mApiService);
        mDisposable.addAll(mUsersContacts.getLinkedContacts().subscribe(view::ShowContacts, throwable -> AppHelper.LogCat("AddNewMembersToGroupPresenter " + throwable.getMessage())));
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