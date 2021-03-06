package com.chatonx.application.adapters.recyclerView.groups;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chatonx.application.R;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.cache.ImageLoader;
import com.chatonx.application.helpers.Files.cache.MemoryCache;
import com.chatonx.application.helpers.UtilsPhone;
import com.chatonx.application.helpers.glide.GlideApp;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.ui.ColorGenerator;
import com.chatonx.application.ui.TextDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<ContactsModel> mContactsModels;
    private LayoutInflater mInflater;
    private MemoryCache memoryCache;

    public AddMembersToGroupSelectorAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        this.mContactsModels = new ArrayList<>();
        mInflater = LayoutInflater.from(mActivity);
        this.memoryCache = new MemoryCache();

    }

    public void setContacts(List<ContactsModel> mContactsModels) {
        this.mContactsModels = mContactsModels;
        notifyDataSetChanged();
    }

    public void remove(ContactsModel contactsModel) {
        mContactsModels.remove(contactsModel);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mContactsModels.remove(position);
        notifyDataSetChanged();
    }

    public void add(ContactsModel contactsModel) {
        mContactsModels.add(contactsModel);
        notifyItemInserted(mContactsModels.size() - 1);
    }

    public List<ContactsModel> getContacts() {
        return mContactsModels;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_add_members_header_view, parent, false);
        return new ContactsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final ContactsModel contactsModel = this.mContactsModels.get(position);

        try {

            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_enter);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    contactsViewHolder.itemView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            contactsViewHolder.itemView.startAnimation(animation);
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


            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.getId(), username);
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
        }


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mContactsModels != null) {
            return mContactsModels.size();
        } else {
            return 0;
        }
    }


    public class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.user_image)
        ImageView userImage;

        @BindView(R.id.username)
        TextView username;


        @BindView(R.id.remove_icon)
        LinearLayout removeIcon;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setTypeFaces();
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                username.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
            }
        }


        TextDrawable textDrawable(String name) {
            if (name == null) {
                name = mActivity.getString(R.string.app_name);
            }
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(name);
            String c = String.valueOf(name.toUpperCase().charAt(0));
            return TextDrawable.builder().buildRound(c, color);


        }

        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, int recipientId, String username) {
            TextDrawable drawable = textDrawable(username);
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, mActivity, recipientId, AppConstants.USER, AppConstants.ROW_PROFILE);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ImageLoader.SetBitmapImage(bitmap, userImage);
                    } else {

                        DrawableImageViewTarget target = new DrawableImageViewTarget(userImage) {

                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                super.onResourceReady(resource, transition);
                                userImage.setImageDrawable(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                userImage.setImageDrawable(errorDrawable);
                            }


                            @Override
                            public void onLoadStarted(Drawable placeHolderDrawable) {
                                super.onLoadStarted(placeHolderDrawable);
                                userImage.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        GlideApp.with(mActivity.getApplicationContext())
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

        void setUsername(String Username) {
            username.setText(Username);
        }

        @Override
        public void onClick(View view) {
            ContactsModel contactsModel = mContactsModels.get(getAdapterPosition());
            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_exit);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    remove(getAdapterPosition());
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CREATE_MEMBER, contactsModel));
                    itemView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            itemView.startAnimation(animation);

        }
    }


}

