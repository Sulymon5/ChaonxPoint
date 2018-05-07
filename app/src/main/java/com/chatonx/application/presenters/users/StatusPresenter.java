package com.chatonx.application.presenters.users;

import com.chatonx.application.R;
import com.chatonx.application.activities.popups.StatusDelete;
import com.chatonx.application.activities.status.EditStatusActivity;
import com.chatonx.application.activities.status.StatusActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.Presenter;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.status.StatusModel;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusPresenter implements Presenter {

    private StatusActivity view;
    private EditStatusActivity editStatusActivity;
    private StatusDelete viewDelete;
    private Realm realm;
    private UsersService mUsersContacts;
    private APIService mApiService;
    private CompositeDisposable mDisposable;

    public StatusPresenter(StatusActivity statusActivity) {
        this.view = statusActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
        this.mApiService = APIService.with(this.view);
        this.mUsersContacts = new UsersService(this.realm, this.view, this.mApiService);
    }

    public StatusPresenter(StatusDelete statusDelete) {
        this.viewDelete = statusDelete;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }

    public StatusPresenter(EditStatusActivity editStatusActivity) {
        this.editStatusActivity = editStatusActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
        this.mApiService = APIService.with(this.editStatusActivity);
        this.mUsersContacts = new UsersService(this.realm, this.editStatusActivity, this.mApiService);
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(view)) EventBus.getDefault().register(view);
        mDisposable = new CompositeDisposable();
        this.mApiService = APIService.with(view);
        mUsersContacts = new UsersService(realm, view, this.mApiService);
        getStatusFromServer();
        getCurrentStatus();

    }


    private void getStatusFromServer() {
        mDisposable.addAll(mUsersContacts.getUserStatus().subscribe(view::ShowStatus, view::onErrorLoading))
        ;

    }

    public void getCurrentStatus() {
        try {
            mDisposable.addAll(mUsersContacts.getCurrentStatusFromLocal().subscribe(view::ShowCurrentStatus, throwable -> AppHelper.LogCat(" " + throwable.getMessage())))
            ;
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
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
        if (view != null) {
            EventBus.getDefault().unregister(view);
        }
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

    public void DeleteAllStatus() {
        AppHelper.showDialog(view, view.getString(R.string.delete_all_status_dialog));
        mDisposable.addAll(mUsersContacts.deleteAllStatus().subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                realm.executeTransaction(realm1 -> realm1.where(StatusModel.class).equalTo("userID", PreferenceManager.getID(view)).findAll().deleteAllFromRealm());
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                view.startActivity(view.getIntent());
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            }
        }, throwable -> AppHelper.LogCat("Delete Status Error StatusPresenter ")))
        ;
    }


    public void DeleteStatus(int statusID) {
        APIService mApiServiceDelete = APIService.with(viewDelete);
        UsersService mUsersContactsDelete = new UsersService(realm, viewDelete, mApiServiceDelete);
        AppHelper.showDialog(viewDelete, viewDelete.getString(R.string.deleting));
        mDisposable.addAll(mUsersContactsDelete.deleteStatus(statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_STATUS, statusID));
                viewDelete.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.LogCat("delete  status " + statusResponse.getMessage());
                AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("delete  status " + throwable.getMessage());
            AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
        }))
        ;
    }

    public void UpdateCurrentStatus(String status, int statusID) {
        AppHelper.showDialog(view, view.getString(R.string.updating_status));
        mDisposable.addAll(mUsersContacts.updateStatus(statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS));
                view.ShowCurrentStatus(status);


            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
        }))
        ;

    }


    public void EditCurrentStatus(String status, int statusID) {
        mDisposable.addAll(mUsersContacts.editStatus(status, statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity.getBaseContext(), editStatusActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_STATUS, status));
                editStatusActivity.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity.getBaseContext(), editStatusActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
        }))
        ;

    }

    public void onEventPush(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_DELETE_STATUS:
                int id = pusher.getStatusID();
                realm.executeTransactionAsync(realm1 -> {
                    realm1.where(StatusModel.class).equalTo("id", id).findFirst().deleteFromRealm();
                }, () -> {
                    view.deleteStatus(id);
                    AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.your_status_updated_successfully), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    getStatusFromServer();
                    getCurrentStatus();
                }, error -> {
                    AppHelper.LogCat(error.getMessage());
                    AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                });
                break;
            case AppConstants.EVENT_BUS_UPDATE_STATUS:
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS));
                view.ShowCurrentStatus(pusher.getData());
                getStatusFromServer();
                getCurrentStatus();
                break;
        }
    }
}