package com.chatonx.application.presenters.messages;


import com.chatonx.application.activities.search.SearchConversationsActivity;
import com.chatonx.application.api.apiServices.ConversationsService;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.interfaces.Presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SearchConversationsPresenter implements Presenter {
    private SearchConversationsActivity mSearchConversationsActivity;
    private Realm realm;
    private CompositeDisposable mDisposable;

    public SearchConversationsPresenter(SearchConversationsActivity mSearchConversationsActivity) {
        this.mSearchConversationsActivity = mSearchConversationsActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        ConversationsService mConversationsService = new ConversationsService(realm);
        mDisposable.add(mConversationsService.getConversations().subscribe(mSearchConversationsActivity::ShowConversation, mSearchConversationsActivity::onErrorLoading))
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