package com.chatonx.application.activities.main.welcome;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chatonx.application.R;
import com.chatonx.application.activities.main.MainActivity;
import com.chatonx.application.animations.AnimationsUtil;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.fragments.bottomSheets.BottomSheetEditProfile;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.helpers.glide.GlideApp;
import com.chatonx.application.helpers.images.ImageUtils;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.contacts.ProfileResponse;
import com.chatonx.application.presenters.users.EditProfilePresenter;
import com.chatonx.application.services.MainService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.chatonx.application.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH;

/**
 * Created by Abderrahim El imame on 4/1/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CompleteRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.username_input)
    AppCompatEditText usernameInput;

    @BindView(R.id.userAvatar)
    ImageView userAvatar;

    @BindView(R.id.addAvatar)
    FloatingActionButton addAvatar;

    @BindView(R.id.progress_bar_edit_profile)
    ProgressBar progressBar;

    @BindView(R.id.completeRegistration)
    TextView completeRegistration;


    @BindView(R.id.registerBtn)
    TextView registerBtn;

    @BindView(R.id.completeRegistrationLayout)
    NestedScrollView mView;
    private CompositeDisposable mDisposable;

    private String PicturePath;
    private EditProfilePresenter mEditProfilePresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_registration_activity);
        ButterKnife.bind(this);
        mDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);
        setTypeFaces();
        mEditProfilePresenter = new EditProfilePresenter(this);
        mEditProfilePresenter.onCreate();
        registerBtn.setOnClickListener(this);
        addAvatar.setOnClickListener(v -> {
            BottomSheetEditProfile bottomSheetEditProfile = new BottomSheetEditProfile();
            bottomSheetEditProfile.show(getSupportFragmentManager(), bottomSheetEditProfile.getTag());
        });

    }


    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            completeRegistration.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            registerBtn.setTypeface(AppHelper.setTypeFace(this, "Futura"));
            usernameInput.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registerBtn:
                complete();
                break;

        }
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case EVENT_BUS_IMAGE_PROFILE_PATH:
                progressBar.setVisibility(View.VISIBLE);
                PicturePath = String.valueOf(pusher.getData());
                if (PicturePath != null) {
                    try {
                        new UploadFileToServer().execute();
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                        AppHelper.CustomToast(this, getString(R.string.oops_something));
                    }

                }
                break;

        }

    }

    private void complete() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            PreferenceManager.setIsNeedInfo(this, false);
            if (!AppHelper.isServiceRunning(this, MainService.class)
                    && PreferenceManager.getToken(this) != null
                    && !PreferenceManager.isNeedProvideInfo(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(this, MainService.class));
                } else {
                    startService(new Intent(this, MainService.class));
                }
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            this.finish();
            AnimationsUtil.setSlideInAnimation(this);
        } else {
            mEditProfilePresenter.EditCurrentName(username, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditProfilePresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mDisposable != null) mDisposable.dispose();
    }

    private void setImage(String ImageUrl) {
        DrawableImageViewTarget target = new DrawableImageViewTarget(userAvatar) {


            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                userAvatar.setImageDrawable(resource);

            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                userAvatar.setImageDrawable(errorDrawable);
            }


            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                userAvatar.setImageDrawable(placeholder);
            }
        };

        GlideApp.with(this)
                .load(EndPoints.EDIT_PROFILE_IMAGE_URL + ImageUrl)

                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.image_holder_ur_circle)
                .error(R.drawable.image_holder_ur_circle)
                .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                .into(target);
    }

    /**
     * Uploading the image  to server
     */
    private class UploadFileToServer extends AsyncTask<Void, Integer, ProfileResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AppHelper.LogCat("onPreExecute  image ");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            AppHelper.LogCat("progress image " + (int) (progress[0]));
        }

        @Override
        protected ProfileResponse doInBackground(Void... params) {
            return uploadFile();
        }


        private ProfileResponse uploadFile() {

            RequestBody requestFile;
            final ProfileResponse profileResponse = null;
            if (PicturePath != null) {
                byte[] imageByte = ImageUtils.compressImage(PicturePath);
                // create RequestBody instance from file
                requestFile = RequestBody.create(MediaType.parse("image/*"), imageByte);
            } else {
                requestFile = null;
            }
            mDisposable.add(APIHelper.initialApiUsersContacts().uploadImage(requestFile).subscribe(response -> {
                if (response.isSuccess()) {

                    if (PicturePath != null) {
                        File file = new File(PicturePath);
                        file.delete();
                    }


                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        AppHelper.CustomToast(CompleteRegistrationActivity.this, response.getMessage());
                        setImage(response.getUserImage());
                    });
                } else {
                    AppHelper.CustomToast(CompleteRegistrationActivity.this, response.getMessage());
                }
            }, throwable -> {
                AppHelper.CustomToast(CompleteRegistrationActivity.this, getString(R.string.failed_upload_image));
                AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }))
            ;

            return profileResponse;
        }

        @Override
        protected void onPostExecute(ProfileResponse response) {
            super.onPostExecute(response);
            // AppHelper.LogCat("Response from server: " + response);

        }


    }

}
