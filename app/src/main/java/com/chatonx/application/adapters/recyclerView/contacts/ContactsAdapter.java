package com.chatonx.application.adapters.recyclerView.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chatonx.application.R;
import com.chatonx.application.activities.messages.MessagesActivity;
import com.chatonx.application.activities.profile.ProfilePreviewActivity;
import com.chatonx.application.animations.AnimationsUtil;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.cache.ImageLoader;
import com.chatonx.application.helpers.Files.cache.MemoryCache;
import com.chatonx.application.helpers.RateHelper;
import com.chatonx.application.helpers.UtilsPhone;
import com.chatonx.application.helpers.UtilsString;
import com.chatonx.application.helpers.glide.GlideApp;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.ui.ColorGenerator;
import com.chatonx.application.ui.RecyclerViewFastScroller;
import com.chatonx.application.ui.TextDrawable;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private RealmList<ContactsModel> mContactsModel;
    private String SearchQuery;
    private MemoryCache memoryCache;

    public ContactsAdapter(RealmList<ContactsModel> mContactsModel) {
        this.mContactsModel = mContactsModel;
        memoryCache = new MemoryCache();

    }

    public ContactsAdapter() {
        memoryCache = new MemoryCache();
        this.mContactsModel = new RealmList<>();
    }


    public void setContacts(RealmList<ContactsModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<ContactsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ContactsModel> newModels) {
        int arraySize = mContactsModel.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final ContactsModel model = mContactsModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ContactsModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final ContactsModel model = newModels.get(i);
            if (!mContactsModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ContactsModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final ContactsModel model = newModels.get(toPosition);
            final int fromPosition = mContactsModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private ContactsModel removeItem(int position) {
        final ContactsModel model = mContactsModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, ContactsModel model) {
        mContactsModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final ContactsModel model = mContactsModel.remove(fromPosition);
        mContactsModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof ContactsViewHolder) {
            ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            Activity context = (Activity) holder.itemView.getContext();

            try {
                ContactsModel contactsModel = this.mContactsModel.get(position);
                String username;
                if (contactsModel.getUsername() != null) {
                    username = contactsModel.getUsername();
                } else {
                    String name = UtilsPhone.getContactName(contactsModel.getPhone());
                    if (name != null) {
                        username = name;
                    } else {
                        username = contactsModel.getPhone();
                    }

                }
                contactsViewHolder.setUsername(username);


                SpannableString recipientUsername = SpannableString.valueOf(username);
                if (SearchQuery == null) {
                    contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
                } else {
                    int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(context, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
                }
                if (contactsModel.getStatus() != null) {
                    String status = UtilsString.unescapeJava(contactsModel.getStatus());
                    contactsViewHolder.setStatus(status);

                } else {
                    contactsViewHolder.setStatus(contactsModel.getPhone());
                }

                if (contactsModel.isLinked() && contactsModel.isActivate()) {
                    contactsViewHolder.hideInviteButton();
                } else {
                    contactsViewHolder.showInviteButton();
                }

                if (contactsModel.isOnline()) {
                    contactsViewHolder.isOnline();
                } else {
                    contactsViewHolder.isOffline();
                }
                contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.getId(), username);


                contactsViewHolder.setOnClickListener(view -> {
                    if (view.getId() == R.id.user_image) {
                        RateHelper.significantEvent(context);
                        if (!contactsModel.isValid()) return;
                        if (AppHelper.isAndroid5()) {
                            if (contactsModel.isLinked()) {
                                Intent mIntent = new Intent(context, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", contactsModel.getId());
                                mIntent.putExtra("isGroup", false);
                                context.startActivity(mIntent);
                            }
                        } else {
                            if (contactsModel.isLinked()) {
                                Intent mIntent = new Intent(context, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", contactsModel.getId());
                                mIntent.putExtra("isGroup", false);
                                context.startActivity(mIntent);
                                context.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                            }
                        }

                    } else {
                        RateHelper.significantEvent(context);
                        if (!contactsModel.isValid()) return;
                        if (contactsModel.isLinked() && contactsModel.isActivate()) {
                            Intent messagingIntent = new Intent(context, MessagesActivity.class);
                            messagingIntent.putExtra("conversationID", 0);
                            messagingIntent.putExtra("recipientID", contactsModel.getId());
                            messagingIntent.putExtra("isGroup", false);
                            context.startActivity(messagingIntent);
                            context.finish();
                            AnimationsUtil.setSlideInAnimation(context);
                        } else {
                            String number = contactsModel.getPhone();
                            contactsViewHolder.setShareApp(context.getString(R.string.invitation_from) + " " + number);
                        }
                    }

                });
            } catch (Exception e) {
                AppHelper.LogCat("Contacts adapters Exception " + e.getMessage());
            }

        }


    }

    public void updateItem(int userId, boolean isOnline) {
        try {
            Realm realm = ChatonXApplication.getRealmDatabaseInstance();
            int arraySize = mContactsModel.size();
            for (int i = 0; i < arraySize; i++) {
                ContactsModel model = mContactsModel.get(i);
                if (userId == model.getId()) {
                    ContactsModel contactsModel = realm.where(ContactsModel.class).equalTo("id", userId).findFirst();
                    contactsModel.setOnline(isOnline);
                    changeItemAtPosition(i, contactsModel);
                    break;
                }

            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, ContactsModel contactsModel) {
        mContactsModel.set(position, contactsModel);
        notifyItemChanged(position);
    }


    @Override
    public int getItemCount() {
        return mContactsModel.size() > 0 ? mContactsModel.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (mContactsModel.size() > pos) {
                if (mContactsModel.get(pos).getUsername() != null) {
                    return Character.toString(mContactsModel.get(pos).getUsername().charAt(0));
                } else {
                    String name = UtilsPhone.getContactName(mContactsModel.get(pos).getPhone());
                    if (name != null) {
                        return Character.toString(name.charAt(0));
                    } else {
                        return Character.toString(mContactsModel.get(pos).getPhone().charAt(0));
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public ContactsModel getItem(int position) {
        return mContactsModel.get(position);
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        Context context;
        @BindView(R.id.user_image)
        ImageView userImage;


        @BindView(R.id.online_indicator)
        View onlineIndicator;

        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.status)
        EmojiconTextView status;
        @BindView(R.id.invite)
        TextView invite;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);
            context = itemView.getContext();
            setTypeFaces();
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                status.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                invite.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                username.setTypeface(AppHelper.setTypeFace(context, "Futura"));
            }
        }

        void setShareApp(String subject) {

            //   Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/mipmap/" + "ic_launcher");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.INVITE_MESSAGE_SMS + String.format(context.getString(R.string.rate_helper_google_play_url), context.getPackageName()));
            // shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("text/*");
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.shareItem)));
        }


        TextDrawable textDrawable(String name) {
            if (name == null) {
                name = context.getString(R.string.app_name);
            }
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(name);
            String c = String.valueOf(name.toUpperCase().charAt(0));
            return TextDrawable.builder().buildRound(c, color);


        }

        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, int recipientId, String name) {

            TextDrawable drawable = textDrawable(name);
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, context, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ImageLoader.SetBitmapImage(bitmap, userImage);
                    } else {

                        BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                super.onResourceReady(resource, transition);
                                userImage.setImageBitmap(resource);
                                ImageLoader.DownloadImage(memoryCache, EndPoints.ROWS_IMAGE_URL + ImageUrl, ImageUrl, context, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                if (ImageUrl != null) {
                                    Bitmap bitmap = null;
                                    try {
                                        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(ImageUrl));
                                    } catch (IOException ex) {
                                        // AppHelper.LogCat(e.getMessage());
                                    }
                                    if (bitmap != null) {
                                        ImageLoader.SetBitmapImage(bitmap, userImage);
                                    } else {
                                        userImage.setImageDrawable(errorDrawable);
                                    }
                                } else {
                                    userImage.setImageDrawable(errorDrawable);
                                }
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                userImage.setImageDrawable(placeholder);
                            }
                        };
                        GlideApp.with(context.getApplicationContext())
                                .asBitmap()
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

        }


        void hideInviteButton() {
            invite.setVisibility(View.GONE);
        }

        void showInviteButton() {
            invite.setVisibility(View.VISIBLE);
        }

        void isOnline() {
            onlineIndicator.setVisibility(View.VISIBLE);
        }

        void isOffline() {
            onlineIndicator.setVisibility(View.GONE);
        }

        void setUsername(String phone) {
            username.setText(phone);
        }

        void setStatus(String Status) {
            status.setText(Status);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }


}
