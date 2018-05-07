package com.chatonx.application.activities.settings;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chatonx.application.R;
import com.chatonx.application.activities.profile.EditProfileActivity;
import com.chatonx.application.animations.AnimationsUtil;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.cache.ImageLoader;
import com.chatonx.application.helpers.Files.cache.MemoryCache;
import com.chatonx.application.helpers.RateHelper;
import com.chatonx.application.helpers.UtilsPhone;
import com.chatonx.application.helpers.UtilsString;
import com.chatonx.application.helpers.glide.GlideApp;
import com.chatonx.application.interfaces.NetworkListener;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.presenters.users.SettingsPresenter;
import com.chatonx.application.ui.ColorGenerator;
import com.chatonx.application.ui.TextDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SettingsActivity extends AppCompatActivity implements NetworkListener {


    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.user_status)
    EmojiconTextView userStatus;

    @BindView(R.id.settingsHead)
    AppCompatTextView settingsHead;

    @BindView(R.id.userName)
    TextView userName;

    @BindView(R.id.chats_settings_text)
    TextView chats;

    @BindView(R.id.account_settings_text)
    TextView account;

    @BindView(R.id.notifications_settings_text)
    TextView notifications;

    @BindView(R.id.about_help_settings_text)
    TextView aboutHelp;

    @BindView(R.id.mainSettings)
    NestedScrollView mView;
    private ContactsModel mContactsModel;
    private SettingsPresenter mSettingsPresenter;
    private MemoryCache memoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setupToolbar();
        setTypeFaces();
        memoryCache = new MemoryCache();
        mSettingsPresenter = new SettingsPresenter(this);
        mSettingsPresenter.onCreate();
        EventBus.getDefault().register(this);

    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            userName.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            userStatus.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            chats.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            account.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            notifications.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            aboutHelp.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.settingsHead)
    public void launchEditProfile(View v) {
        RateHelper.significantEvent(this);
        if (AppHelper.isAndroid5()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    new Pair<>(userAvatar, "userAvatar"),
                    new Pair<>(userName, "userName"),
                    new Pair<>(userStatus, "userStatus"),
                    new Pair<>(settingsHead, "settingsHead")
            );
            Intent mIntent = new Intent(this, EditProfileActivity.class);
            startActivity(mIntent, options.toBundle());
        } else {
            AppHelper.LaunchActivity(this, EditProfileActivity.class);
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.chats_settings)
    public void launchChatsSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, ChatsSettingsActivity.class);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.account_settings)
    public void launchAccountSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, AccountSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.notifications_settings)
    public void launchNotificationSettings() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, NotificationsSettingsActivity.class);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.about_help_settings)
    public void launchAboutSettings() {
        AppHelper.LaunchActivity(this, AboutHelpActivity.class);
    }

    @SuppressLint("StaticFieldLeak")
    public void ShowContact(ContactsModel contactsModels) {
        mContactsModel = contactsModels;
        try {

            String finalName = null;

            if (mContactsModel.getStatus() != null) {
                String status = UtilsString.unescapeJava(mContactsModel.getStatus());
                userStatus.setText(status);
            } else {
                userStatus.setText(getString(R.string.no_status));
            }
            if (mContactsModel.getUsername() != null) {
                userName.setText(mContactsModel.getUsername());
                finalName = mContactsModel.getUsername();
            } else {
                userName.setText(getString(R.string.no_username));
                String name = UtilsPhone.getContactName(mContactsModel.getPhone());
                if (name != null) {
                    finalName = name;
                } else {
                    finalName = mContactsModel.getPhone();
                }
            }
            TextDrawable drawable = textDrawable(finalName);

            String ImageUrl = mContactsModel.getImage();
            int recipientId = mContactsModel.getId();
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, SettingsActivity.this, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ImageLoader.SetBitmapImage(bitmap, userAvatar);
                    } else {


                        DrawableImageViewTarget target = new DrawableImageViewTarget(userAvatar) {

                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                super.onResourceReady(resource, transition);
                                userAvatar.setImageDrawable(resource);
                                ImageLoader.DownloadImage(memoryCache, EndPoints.SETTINGS_IMAGE_URL + ImageUrl, ImageUrl, SettingsActivity.this, recipientId, AppConstants.USER, AppConstants.SETTINGS_PROFILE);
                                ImageLoader.DownloadImage(memoryCache, EndPoints.EDIT_PROFILE_IMAGE_URL + ImageUrl, ImageUrl, SettingsActivity.this, recipientId, AppConstants.USER, AppConstants.EDIT_PROFILE);

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                userAvatar.setImageDrawable(errorDrawable);
                            }


                            @Override
                            public void onLoadStarted(Drawable placeHolderDrawable) {
                                super.onLoadStarted(placeHolderDrawable);
                                userAvatar.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        GlideApp.with(SettingsActivity.this)
                                .load(EndPoints.ROWS_IMAGE_URL + ImageUrl)

                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(drawable)
                                .error(drawable)
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .into(target);
                    }
                }
            }.execute();

        } catch (Exception e) {
            AppHelper.LogCat("" + e);
        }

    }


    private TextDrawable textDrawable(String name) {
        if (name == null) {
            name = getApplicationContext().getString(R.string.app_name);
        }
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(name);
        String c = String.valueOf(name.toUpperCase().charAt(0));
        return TextDrawable.builder().buildRound(c, color);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setSlideInAnimation(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSettingsPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED:
            case AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS:
            case AppConstants.EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED:
                mSettingsPresenter.loadData();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatonXApplication.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected) {
        if (!isConnecting && !isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_not_available), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
        } else if (isConnecting && isConnected) {
            AppHelper.Snackbar(this, mView, getString(R.string.connection_is_available), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
        } else {
            AppHelper.Snackbar(this, mView, getString(R.string.waiting_for_network), AppConstants.MESSAGE_COLOR_WARNING, AppConstants.TEXT_COLOR);
        }
    }
}
