package com.chatonx.application.presenters.users;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.chatonx.application.R;
import com.chatonx.application.activities.main.MainActivity;
import com.chatonx.application.activities.main.welcome.CompleteRegistrationActivity;
import com.chatonx.application.activities.profile.EditProfileActivity;
import com.chatonx.application.activities.profile.EditUsernameActivity;
import com.chatonx.application.animations.AnimationsUtil;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.FilesManager;
import com.chatonx.application.helpers.PermissionHandler;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.Presenter;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.services.MainService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditProfilePresenter implements Presenter {
    private EditProfileActivity view;
    private EditUsernameActivity editUsernameActivity;
    private CompositeDisposable mDisposable;
    private CompleteRegistrationActivity completeRegistrationActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private boolean isEditUsername = false;
    private APIService mApiService;

    public APIService getmApiService() {
        return mApiService;
    }

    public EditProfilePresenter(CompleteRegistrationActivity completeRegistrationActivity) {
        this.completeRegistrationActivity = completeRegistrationActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    public EditProfilePresenter(EditProfileActivity editProfileActivity) {
        this.view = editProfileActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }


    public EditProfilePresenter() {
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }

    public EditProfilePresenter(EditUsernameActivity editUsernameActivity, boolean b) {
        this.isEditUsername = b;
        this.editUsernameActivity = editUsernameActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        mDisposable = new CompositeDisposable();
        if (!isEditUsername) {
            if (completeRegistrationActivity != null) {
                APIService mApiService = APIService.with(completeRegistrationActivity);
                mUsersContacts = new UsersService(realm, completeRegistrationActivity, mApiService);
            } else {
                mApiService = APIService.with(view);
                mUsersContacts = new UsersService(realm, view, mApiService);
                loadData();
            }
        } else {
            mApiService = APIService.with(editUsernameActivity);
            this.mUsersContacts = new UsersService(realm, editUsernameActivity, mApiService);

        }

    }

    public void loadData() {
        mDisposable.addAll(mUsersContacts.getContactInfo(PreferenceManager.getID(view)).subscribe(contactsModel -> {
            view.ShowContact(contactsModel);
        }, throwable -> {
            view.onErrorLoading(throwable);
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
        if (mDisposable!=null) mDisposable.dispose();
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


    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (PermissionHandler.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read contact data permission already granted.");

                switch (requestCode) {
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(activity, data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(activity, data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = activity.getApplicationContext().getContentResolver()
                                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String imageLocation = cursor.getString(1);
                                    cursor.close();
                                    File imageFile = new File(imageLocation);
                                    if (imageFile.exists()) {
                                        imagePath = imageFile.getPath();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("error" + e);
                            }
                        }
                        break;
                }


                if (imagePath != null) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
                PermissionHandler.requestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

    }


    public void EditCurrentName(String name, boolean forComplete) {
        mDisposable.addAll(mUsersContacts.editUsername(name).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                if (forComplete) {
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    PreferenceManager.setIsNeedInfo(completeRegistrationActivity, false);

                    if (!AppHelper.isServiceRunning(completeRegistrationActivity, MainService.class)
                            && PreferenceManager.getToken(completeRegistrationActivity) != null
                            && !PreferenceManager.isNeedProvideInfo(completeRegistrationActivity))
                        completeRegistrationActivity.startService(new Intent(completeRegistrationActivity, MainService.class));
                    Intent intent = new Intent(completeRegistrationActivity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    completeRegistrationActivity.startActivity(intent);
                    completeRegistrationActivity.finish();
                    AnimationsUtil.setSlideInAnimation(completeRegistrationActivity);
                } else {
                    AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED));
                    editUsernameActivity.finish();
                }
            } else {
                if (!forComplete) {
                    AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                } else {
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), completeRegistrationActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }
            }
        }, AppHelper::LogCat))
        ;

    }

}