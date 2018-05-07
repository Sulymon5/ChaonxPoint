package com.chatonx.application.presenters.groups;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.chatonx.application.R;
import com.chatonx.application.activities.groups.EditGroupActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.fragments.bottomSheets.BottomSheetEditGroupImage;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.FilesManager;
import com.chatonx.application.helpers.PermissionHandler;
import com.chatonx.application.interfaces.Presenter;
import com.chatonx.application.models.groups.GroupsModel;
import com.chatonx.application.models.messages.ConversationsModel;
import com.chatonx.application.models.users.Pusher;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditGroupPresenter implements Presenter {
    private EditGroupActivity view;
    private BottomSheetEditGroupImage bottomSheetEditGroupImage;
    private Realm realm;
    private UsersService mUsersContacts;
    private APIService mApiService;
    private Socket mSocket;
    private CompositeDisposable mDisposable;

    public EditGroupPresenter(EditGroupActivity editGroupActivity) {
        this.view = editGroupActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }

    public EditGroupPresenter(BottomSheetEditGroupImage bottomSheetEditGroupImage) {
        this.bottomSheetEditGroupImage = bottomSheetEditGroupImage;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();

    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {

        mDisposable = new CompositeDisposable();
        if (view != null) {

            this.mApiService = APIService.with(view);
            this.mUsersContacts = new UsersService(this.realm, view, this.mApiService);
            connectToChatServer();
        } else if (bottomSheetEditGroupImage != null) {
            this.mApiService = APIService.with(bottomSheetEditGroupImage.getActivity());
            this.mUsersContacts = new UsersService(this.realm, bottomSheetEditGroupImage.getActivity(), this.mApiService);
        }
    }


    /**
     * method to connect to the chat sever by socket
     */
    private void connectToChatServer() {

        ChatonXApplication app = (ChatonXApplication) view.getApplication();
        mSocket = app.getSocket();

        if (mSocket == null) {
            ChatonXApplication.connectSocket();
            mSocket = app.getSocket();
        }
        if (!mSocket.connected())
            mSocket.connect();


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
        if (mDisposable != null)
            mDisposable.dispose();
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (PermissionHandler.checkPermission(bottomSheetEditGroupImage.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read storage data permission already granted.");
                switch (requestCode) {
                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        break;
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = bottomSheetEditGroupImage.getActivity().getContentResolver()
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
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_PATH_GROUP, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
                PermissionHandler.requestPermission(bottomSheetEditGroupImage.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }

    }


    public void EditCurrentName(String name, int groupID) {
        mDisposable.addAll(mUsersContacts.editGroupName(name, groupID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                realm.executeTransactionAsync(realm1 -> {
                            GroupsModel groupsModel = realm1.where(GroupsModel.class).equalTo("id", groupID).findFirst();
                            groupsModel.setGroupName(name);
                            realm1.copyToRealmOrUpdate(groupsModel);
                        }, () -> realm.executeTransactionAsync(realm1 -> {
                            ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("groupID", groupID).findFirst();
                            conversationsModel.setRecipientUsername(name);
                            realm1.copyToRealmOrUpdate(conversationsModel);
                        }, () -> {
                            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CREATE_GROUP));

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("groupId", groupID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (mSocket != null)
                                mSocket.emit(AppConstants.SOCKET_IMAGE_GROUP_UPDATED, jsonObject);
                            view.finish();
                        }, error -> AppHelper.LogCat("error update group name in conversation model " + error.getMessage())),
                        error -> AppHelper.LogCat("error update group name in group model  " + error.getMessage()));
            } else {
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatusEdit), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, AppHelper::LogCat))
        ;

    }

}