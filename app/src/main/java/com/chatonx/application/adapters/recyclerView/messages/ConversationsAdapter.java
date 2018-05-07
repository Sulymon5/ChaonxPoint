package com.chatonx.application.adapters.recyclerView.messages;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chatonx.application.R;
import com.chatonx.application.activities.messages.MessagesActivity;
import com.chatonx.application.activities.profile.ProfilePreviewActivity;
import com.chatonx.application.api.APIHelper;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.cache.ImageLoader;
import com.chatonx.application.helpers.Files.cache.MemoryCache;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.helpers.RateHelper;
import com.chatonx.application.helpers.UtilsPhone;
import com.chatonx.application.helpers.UtilsString;
import com.chatonx.application.helpers.UtilsTime;
import com.chatonx.application.helpers.glide.GlideApp;
import com.chatonx.application.helpers.images.ImageCompressionAsyncTask;
import com.chatonx.application.models.groups.GroupsModel;
import com.chatonx.application.models.groups.MembersGroupModel;
import com.chatonx.application.models.messages.ConversationsModel;
import com.chatonx.application.models.messages.MessagesModel;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.ui.ColorGenerator;
import com.chatonx.application.ui.TextDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.chatonx.application.app.AppConstants.EVENT_BUS_ITEM_IS_ACTIVATED;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
@SuppressLint("StaticFieldLeak")
public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // protected final Activity context;
    private RealmList<ConversationsModel> mConversations;
    private Realm realm;
    private String SearchQuery;
    private SparseBooleanArray selectedItems;
    private boolean isActivated = false;
    private RecyclerView conversationList;
    private MemoryCache memoryCache;

    public ConversationsAdapter() {
        this.mConversations = new RealmList<>();
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
        this.selectedItems = new SparseBooleanArray();
        this.memoryCache = new MemoryCache();
    }

    public ConversationsAdapter(RecyclerView conversationList) {
        this.mConversations = new RealmList<>();
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
        this.selectedItems = new SparseBooleanArray();
        this.conversationList = conversationList;
        this.memoryCache = new MemoryCache();
    }


    public void setConversations(RealmList<ConversationsModel> conversationsModelList) {
        this.mConversations = conversationsModelList;
        notifyDataSetChanged();
    }

    /**
     * method to connect to the chat sever by socket
     */

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<ConversationsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ConversationsModel> newModels) {
        int arraySize = mConversations.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final ConversationsModel model = mConversations.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ConversationsModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final ConversationsModel model = newModels.get(i);
            if (!mConversations.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ConversationsModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final ConversationsModel model = newModels.get(toPosition);
            final int fromPosition = mConversations.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private ConversationsModel removeItem(int position) {
        final ConversationsModel model = mConversations.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, ConversationsModel model) {
        mConversations.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final ConversationsModel model = mConversations.remove(fromPosition);
        mConversations.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new ConversationViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ConversationViewHolder conversationViewHolder = (ConversationViewHolder) holder;
        Activity mActivity = (Activity) conversationViewHolder.itemView.getContext();
        ConversationsModel conversationsModel = mConversations.get(position);
        MessagesModel messagesModel = conversationsModel.getMessages().last();

        if (conversationsModel.isGroup()) {

            if (conversationsModel.getRecipientUsername() != null) {
                String groupName = UtilsString.unescapeJava(conversationsModel.getRecipientUsername());
                conversationViewHolder.setUsername(groupName);
                SpannableString recipientUsername = SpannableString.valueOf(groupName);
                if (SearchQuery == null) {
                    conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
                } else {
                    int index = TextUtils.indexOf(groupName.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
                }
            }


            conversationViewHolder.isOffline();

            if (!conversationsModel.getCreatedOnline()) {
                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorGray2));

            } else {
                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
            }
            if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                conversationViewHolder.lastMessage.setVisibility(View.GONE);
                conversationViewHolder.setTypeFile("image");
            } else if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                conversationViewHolder.lastMessage.setVisibility(View.GONE);
                conversationViewHolder.setTypeFile("video");
            } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                conversationViewHolder.lastMessage.setVisibility(View.GONE);
                conversationViewHolder.setTypeFile("audio");
            } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                conversationViewHolder.lastMessage.setVisibility(View.GONE);
                conversationViewHolder.setTypeFile("document");
            } else {

                conversationViewHolder.isFile.setVisibility(View.GONE);
                conversationViewHolder.FileContent.setVisibility(View.GONE);
                conversationViewHolder.lastMessage.setVisibility(View.VISIBLE);
                switch (messagesModel.getMessage()) {
                    case AppConstants.CREATE_GROUP:
                        if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                            if (!conversationsModel.getCreatedOnline()) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.tap_to_create_group));
                            } else {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_created_this_group));
                            }

                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getPhone());
                            if (name != null) {
                                conversationViewHolder.setLastMessage("" + name + mActivity.getString(R.string.he_created_this_group));
                            } else {
                                conversationViewHolder.setLastMessage("" + messagesModel.getPhone() + mActivity.getString(R.string.he_created_this_group));
                            }
                        }


                        break;
                    case AppConstants.LEFT_GROUP:
                        if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                            conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_left));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getPhone());
                            if (name != null) {
                                conversationViewHolder.setLastMessage("" + name + mActivity.getString(R.string.he_left));
                            } else {
                                conversationViewHolder.setLastMessage("" + messagesModel.getPhone() + mActivity.getString(R.string.he_left));
                            }


                        }

                        break;
                    default:

                        conversationViewHolder.isFile.setVisibility(View.GONE);
                        conversationViewHolder.FileContent.setVisibility(View.GONE);
                        conversationViewHolder.lastMessage.setVisibility(View.VISIBLE);
                        if (conversationsModel.getLastMessage() != null)
                            conversationViewHolder.setLastMessage(conversationsModel.getLastMessage());
                        else
                            conversationViewHolder.setLastMessage(messagesModel.getMessage());
                        break;
                }
            }


            if (messagesModel.getDate() != null) {
                conversationViewHolder.setMessageDate(messagesModel.getDate());
            }

            if (conversationsModel.getCreatedOnline()) {
                conversationViewHolder.setGroupImage(conversationsModel.getRecipientImage(), conversationsModel.getGroupID(), conversationsModel.getRecipientUsername());
            } else {
                conversationViewHolder.setGroupImageOffline(conversationsModel.getRecipientImage(), conversationsModel.getRecipientUsername());
            }

            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                conversationViewHolder.showSent(messagesModel.getStatus());
            } else {
                conversationViewHolder.hideSent();
            }
            if (conversationsModel.getStatus() == AppConstants.IS_WAITING && !conversationsModel.getUnreadMessageCounter().equals("0")) {
                conversationViewHolder.ChangeStatusUnread();
                conversationViewHolder.showCounter();
                conversationViewHolder.setCounter(conversationsModel.getUnreadMessageCounter());

            } else {
                conversationViewHolder.ChangeStatusRead();
                conversationViewHolder.hideCounter();

            }

            if (!conversationsModel.getCreatedOnline()) {

                try {

                    ContactsModel membersGroupModel1 = realm.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(mActivity)).findFirst();
                    MembersGroupModel membersGroupModel = new MembersGroupModel();
                    String role = "admin";
                    membersGroupModel.setUserId(membersGroupModel1.getId());
                    membersGroupModel.setGroupID(conversationsModel.getId());
                    membersGroupModel.setUsername(membersGroupModel1.getUsername());
                    membersGroupModel.setPhone(membersGroupModel1.getPhone());
                    membersGroupModel.setStatus(membersGroupModel1.getStatus());
                    membersGroupModel.setStatus_date(membersGroupModel1.getStatus_date());
                    membersGroupModel.setImage(membersGroupModel1.getImage());
                    membersGroupModel.setRole(role);
                    PreferenceManager.addMember(mActivity, membersGroupModel);
                    StringBuilder ids = new StringBuilder();
                    int arraySize = PreferenceManager.getMembers(mActivity).size();
                    for (int x = 0; x <= arraySize - 1; x++) {
                        ids.append(PreferenceManager.getMembers(mActivity).get(x).getUserId());
                        ids.append(",");
                    }
                    String id = UtilsString.removelastString(ids.toString());
                    AppHelper.LogCat("ids " + id);
                    // create RequestBody instance from file
                    RequestBody requestIds = RequestBody.create(MediaType.parse("multipart/form-data"), id);
                    conversationViewHolder.getProgressBarGroup();
                    ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
                        @Override
                        protected void onPostExecute(byte[] imageBytes) {
                            // image here is compressed & ready to be sent to the server
                            // create RequestBody instance from file
                            RequestBody requestFile;
                            if (imageBytes == null)
                                requestFile = null;
                            else
                                requestFile = RequestBody.create(MediaType.parse("image*//*"), imageBytes);
                            // create RequestBody instance from file
                            RequestBody requestName = RequestBody.create(MediaType.parse("multipart/form-data"), conversationsModel.getRecipientUsername());

                            APIHelper.initializeApiGroups().createGroup(PreferenceManager.getID(mActivity), requestName, requestFile, requestIds, conversationsModel.getMessageDate()).subscribe(groupResponse -> {
                                if (groupResponse.isSuccess()) {
                                    conversationViewHolder.setProgressBarGroup();
                                    Realm realm = ChatonXApplication.getRealmDatabaseInstance();
                                    realm.executeTransaction(realm1 -> {
                                        ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", conversationsModel.getId()).findFirst();
                                        conversationsModel1.setCreatedOnline(true);
                                        conversationsModel1.setGroupID(groupResponse.getGroupID());
                                        conversationsModel1.setRecipientImage(groupResponse.getGroupImage());
                                        realm1.copyToRealmOrUpdate(conversationsModel1);


                                        MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", conversationsModel.getId()).findFirst();
                                        messagesModel1.setGroup(true);
                                        messagesModel1.setGroupID(groupResponse.getGroupID());
                                        realm1.copyToRealmOrUpdate(messagesModel1);

                                        GroupsModel groupsModel = new GroupsModel();
                                        groupsModel.setId(groupResponse.getGroupID());
                                        groupsModel.setMembers(groupResponse.getMembersGroupModels());
                                        if (groupResponse.getGroupImage() != null)
                                            groupsModel.setGroupImage(groupResponse.getGroupImage());
                                        else
                                            groupsModel.setGroupImage("null");
                                        groupsModel.setGroupName(conversationsModel.getRecipientUsername());
                                        groupsModel.setCreatorID(PreferenceManager.getID(mActivity));
                                        realm1.copyToRealmOrUpdate(groupsModel);
                                    });
                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationsModel.getId()));
                                    AppHelper.LogCat("group id created 2 e " + groupResponse.getGroupID());
                                    new Handler().postDelayed(() -> {
                                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CREATE_NEW_GROUP, groupResponse.getGroupID(), conversationsModel.getId()));
                                    }, 1000);
                                    PreferenceManager.clearMembers(mActivity);
                                    realm.close();
                                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                                    AppHelper.CustomToast(mActivity, groupResponse.getMessage());
                                } else {
                                    conversationViewHolder.setProgressBarGroup();
                                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                                    AppHelper.CustomToast(mActivity, groupResponse.getMessage());
                                }
                            },throwable -> {
                                AppHelper.LogCat(throwable.getMessage());

                                conversationViewHolder.setProgressBarGroup();
                                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity), mActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
                            });
                        }
                    };
                    imageCompression.execute(conversationsModel.getRecipientImage());
                } catch (Exception e) {
                    AppHelper.LogCat("execption  ids " + e.getMessage());
                }
            }
        } else {
            String username;
            if (conversationsModel.getRecipientUsername() != null && !conversationsModel.getRecipientUsername().equals("null")) {
                username = conversationsModel.getRecipientUsername();
            } else {
                String name = UtilsPhone.getContactName(conversationsModel.getRecipientPhone());
                if (name != null) {
                    username = name;
                } else {
                    username = conversationsModel.getRecipientPhone();
                }

            }
            conversationViewHolder.setUserImage(conversationsModel.getRecipientImage(), conversationsModel.getRecipientID(), username);
            SpannableString recipientUsername = SpannableString.valueOf(username);
            if (SearchQuery == null) {
                conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
            } else {
                int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
            }


            if (!conversationsModel.getCreatedOnline()) {
                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
            } else {
                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
                if (messagesModel.getImageFile() != null && !messagesModel.getImageFile().equals("null")) {
                    conversationViewHolder.lastMessage.setVisibility(View.GONE);
                    conversationViewHolder.setTypeFile("image");
                } else if (messagesModel.getVideoFile() != null && !messagesModel.getVideoFile().equals("null")) {
                    conversationViewHolder.lastMessage.setVisibility(View.GONE);
                    conversationViewHolder.setTypeFile("video");
                } else if (messagesModel.getAudioFile() != null && !messagesModel.getAudioFile().equals("null")) {
                    conversationViewHolder.lastMessage.setVisibility(View.GONE);
                    conversationViewHolder.setTypeFile("audio");
                } else if (messagesModel.getDocumentFile() != null && !messagesModel.getDocumentFile().equals("null")) {
                    conversationViewHolder.lastMessage.setVisibility(View.GONE);
                    conversationViewHolder.setTypeFile("document");
                } else {
                    conversationViewHolder.isFile.setVisibility(View.GONE);
                    conversationViewHolder.FileContent.setVisibility(View.GONE);
                    if (conversationsModel.getLastMessage() != null)
                        conversationViewHolder.setLastMessage(conversationsModel.getLastMessage());
                    else
                        conversationViewHolder.setLastMessage(messagesModel.getMessage());
                }

                if (messagesModel.getDate() != null) {
                    conversationViewHolder.setMessageDate(messagesModel.getDate());
                } else {
                    conversationViewHolder.setMessageDate(conversationsModel.getMessageDate());
                }
            }


            if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
                conversationViewHolder.showSent(messagesModel.getStatus());
            } else {
                conversationViewHolder.hideSent();
            }


            if (conversationsModel.getStatus() == AppConstants.IS_WAITING && !conversationsModel.getUnreadMessageCounter().equals("0")) {
                conversationViewHolder.ChangeStatusUnread();
                conversationViewHolder.showCounter();
                conversationViewHolder.setCounter(conversationsModel.getUnreadMessageCounter());

            } else {
                conversationViewHolder.ChangeStatusRead();
                conversationViewHolder.hideCounter();

            }


        }

        conversationViewHolder.setOnClickListener(view -> {
            if (!isActivated) {
                if (conversationsModel.isValid())
                    if (conversationsModel.isGroup()) {
                        if (!conversationsModel.getCreatedOnline()) {

                            try {

                                ContactsModel membersGroupModel1 = realm.where(ContactsModel.class).equalTo("id", PreferenceManager.getID(mActivity)).findFirst();
                                MembersGroupModel membersGroupModel = new MembersGroupModel();
                                String role = "admin";
                                membersGroupModel.setUserId(membersGroupModel1.getId());
                                membersGroupModel.setGroupID(conversationsModel.getId());
                                membersGroupModel.setUsername(membersGroupModel1.getUsername());
                                membersGroupModel.setPhone(membersGroupModel1.getPhone());
                                membersGroupModel.setStatus(membersGroupModel1.getStatus());
                                membersGroupModel.setStatus_date(membersGroupModel1.getStatus_date());
                                membersGroupModel.setImage(membersGroupModel1.getImage());
                                membersGroupModel.setRole(role);
                                PreferenceManager.addMember(mActivity, membersGroupModel);
                                StringBuilder ids = new StringBuilder();
                                int arraySize = PreferenceManager.getMembers(mActivity).size();
                                for (int x = 0; x <= arraySize - 1; x++) {
                                    ids.append(PreferenceManager.getMembers(mActivity).get(x).getUserId());
                                    ids.append(",");
                                }
                                String id = UtilsString.removelastString(ids.toString());
                                // create RequestBody instance from file
                                RequestBody requestIds =
                                        RequestBody.create(MediaType.parse("multipart/form-data"), id);
                                conversationViewHolder.getProgressBarGroup();

                                ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
                                    @Override
                                    protected void onPostExecute(byte[] imageBytes) {
                                        // image here is compressed & ready to be sent to the server
                                        // create RequestBody instance from file
                                        RequestBody requestFile;
                                        if (imageBytes == null)
                                            requestFile = null;
                                        else
                                            requestFile = RequestBody.create(MediaType.parse("image*//*"), imageBytes);


                                        RequestBody requestName = RequestBody.create(MediaType.parse("multipart/form-data"), conversationsModel.getRecipientUsername());

                                        APIHelper.initializeApiGroups().createGroup(PreferenceManager.getID(mActivity), requestName, requestFile, requestIds, conversationsModel.getMessageDate()).subscribe(groupResponse -> {
                                            if (groupResponse.isSuccess()) {
                                                conversationViewHolder.setProgressBarGroup();
                                                Realm realm = ChatonXApplication.getRealmDatabaseInstance();
                                                realm.executeTransaction(realm1 -> {
                                                    ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", conversationsModel.getId()).findFirst();
                                                    conversationsModel1.setCreatedOnline(true);
                                                    conversationsModel1.setGroupID(groupResponse.getGroupID());
                                                    conversationsModel1.setRecipientImage(groupResponse.getGroupImage());
                                                    realm1.copyToRealmOrUpdate(conversationsModel1);


                                                    MessagesModel messagesModel1 = realm1.where(MessagesModel.class).equalTo("conversationID", conversationsModel.getId()).findFirst();
                                                    messagesModel1.setGroup(true);
                                                    messagesModel1.setGroupID(groupResponse.getGroupID());
                                                    realm1.copyToRealmOrUpdate(messagesModel1);

                                                    GroupsModel groupsModel = new GroupsModel();
                                                    groupsModel.setId(groupResponse.getGroupID());
                                                    groupsModel.setMembers(groupResponse.getMembersGroupModels());
                                                    if (groupResponse.getGroupImage() != null)
                                                        groupsModel.setGroupImage(groupResponse.getGroupImage());
                                                    else
                                                        groupsModel.setGroupImage("null");
                                                    groupsModel.setGroupName(conversationsModel.getRecipientUsername());
                                                    groupsModel.setCreatorID(PreferenceManager.getID(mActivity));
                                                    realm1.copyToRealmOrUpdate(groupsModel);
                                                });
                                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationsModel.getId()));
                                                AppHelper.LogCat("group id created 2 e " + groupResponse.getGroupID());
                                                new Handler().postDelayed(() -> {
                                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CREATE_NEW_GROUP, groupResponse.getGroupID(), conversationsModel.getId()));
                                                }, 1000);
                                                PreferenceManager.clearMembers(mActivity);
                                                realm.close();
                                                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                                                AppHelper.CustomToast(mActivity, groupResponse.getMessage());
                                            } else {
                                                conversationViewHolder.setProgressBarGroup();
                                                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                                                AppHelper.CustomToast(mActivity, groupResponse.getMessage());
                                            }
                                        }, throwable -> {
                                            AppHelper.LogCat(throwable.getMessage());

                                            conversationViewHolder.setProgressBarGroup();
                                            AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.main_activity),  mActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                                            AppHelper.CustomToast(mActivity,  mActivity.getString(R.string.oops_something));
                                        });
                                    }
                                };
                                imageCompression.execute(conversationsModel.getRecipientImage());
                            } catch (Exception e) {
                                AppHelper.LogCat("execption  ids " + e.getMessage());
                            }
                        } else {
                            if (view.getId() == R.id.user_image) {
                                if (AppHelper.isAndroid5()) {
                                    Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                    mIntent.putExtra("conversationID", conversationsModel.getId());
                                    mIntent.putExtra("groupID", conversationsModel.getGroupID());
                                    mIntent.putExtra("isGroup", conversationsModel.isGroup());
                                    mIntent.putExtra("userId", conversationsModel.getRecipientID());
                                    mActivity.startActivity(mIntent);
                                } else {
                                    Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                    mIntent.putExtra("conversationID", conversationsModel.getId());
                                    mIntent.putExtra("groupID", conversationsModel.getGroupID());
                                    mIntent.putExtra("isGroup", conversationsModel.isGroup());
                                    mIntent.putExtra("userId", conversationsModel.getRecipientID());
                                    mActivity.startActivity(mIntent);
                                    mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                                }
                            } else {

                                RateHelper.significantEvent(mActivity);
                                Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                                messagingIntent.putExtra("conversationID", conversationsModel.getId());
                                messagingIntent.putExtra("groupID", conversationsModel.getGroupID());
                                messagingIntent.putExtra("isGroup", true);
                                messagingIntent.putExtra("recipientID", conversationsModel.getRecipientID());
                                mActivity.startActivity(messagingIntent);

                            }
                        }

                    } else {
                        if (view.getId() == R.id.user_image) {

                            if (AppHelper.isAndroid5()) {
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", conversationsModel.getRecipientID());
                                mIntent.putExtra("isGroup", false);
                                mActivity.startActivity(mIntent);
                            } else {
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", conversationsModel.getRecipientID());
                                mIntent.putExtra("isGroup", false);
                                mActivity.startActivity(mIntent);
                                mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                            }
                        } else {
                            RateHelper.significantEvent(mActivity);
                            Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                            messagingIntent.putExtra("conversationID", conversationsModel.getId());
                            messagingIntent.putExtra("recipientID", conversationsModel.getRecipientID());
                            messagingIntent.putExtra("isGroup", false);
                            mActivity.startActivity(messagingIntent);

                        }
                    }
            } else {
                if (conversationsModel.isGroup()) {
                    AppHelper.LogCat("This is a group you cannot delete this conversation now");
                } else {
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_ITEM_IS_ACTIVATED, view));
                }

            }


        });

        holder.itemView.setActivated(selectedItems.get(position, false));

        if (holder.itemView.isActivated()) {

            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_enter);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    conversationViewHolder.selectIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            conversationViewHolder.selectIcon.startAnimation(animation);
        } else {


            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_exit);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    conversationViewHolder.selectIcon.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            conversationViewHolder.selectIcon.startAnimation(animation);
        }


    }

    @Override
    public int getItemCount() {
        if (mConversations != null) return mConversations.size();
        return 0;
    }


    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {

            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
            if (!isActivated)
                isActivated = true;

        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        if (isActivated)
            isActivated = false;
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public ConversationsModel getItem(int position) {
        return mConversations.get(position);
    }


    /**
     * method to check if a  conversation exist
     *
     * @param conversationId this is the first parameter for  checkIfGroupConversationExist method
     * @param realm          this is the second parameter for  checkIfGroupConversationExist  method
     * @return return value
     */
    private boolean checkIfConversationExist(int conversationId, Realm realm) {
        RealmQuery<ConversationsModel> query = realm.where(ConversationsModel.class).equalTo("id", conversationId);
        return query.count() != 0;

    }

    public void addConversationItem(int conversationId) {
        try {
            Realm realm = ChatonXApplication.getRealmDatabaseInstance();
            ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("id", conversationId).findFirst();
            if (!isConversationExistInList(conversationsModel.getId())) {
                addConversationItem(0, conversationsModel);
            } else {
                return;
            }
            realm.close();

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private boolean isConversationExistInList(int conversationId) {
        int arraySize = mConversations.size();
        boolean conversationExist = false;
        for (int i = 0; i < arraySize; i++) {
            ConversationsModel model = mConversations.get(i);
            if (conversationId == model.getId()) {
                conversationExist = true;
                break;
            }
        }
        return conversationExist;
    }

    private void addConversationItem(int position, ConversationsModel conversationsModel) {
        // if (position != 0) {
        try {
            this.mConversations.add(position, conversationsModel);
            notifyItemInserted(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        // }
    }

    public void removeConversationItem(int position) {
        //if (position != 0) {
        try {
            mConversations.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        //  }
    }

    public void DeleteConversationItem(int ConversationID) {
        try {
            int arraySize = mConversations.size();
            for (int i = 0; i < arraySize; i++) {
                ConversationsModel model = mConversations.get(i);
                if (model.isValid()) {
                    if (ConversationID == model.getId()) {
                        removeConversationItem(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    public void updateStatusConversationItem(int ConversationID) {
        try {
            Realm realm = ChatonXApplication.getRealmDatabaseInstance();
            int arraySize = mConversations.size();
            for (int i = 0; i < arraySize; i++) {
                ConversationsModel model = mConversations.get(i);
                try {
                    if (ConversationID == model.getId()) {
                        ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                        changeItemAtPosition(i, conversationsModel);
                        break;
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }


            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    public void updateConversationItem(int ConversationID) {
        try {
            Realm realm = ChatonXApplication.getRealmDatabaseInstance();
            int arraySize = mConversations.size();
            for (int i = 0; i < arraySize; i++) {
                ConversationsModel model = mConversations.get(i);
                if (ConversationID == model.getId()) {
                    ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                    changeItemAtPosition(i, conversationsModel);
                    if (i != 0)
                        MoveItemToPosition(i, 0);
                    break;
                }

            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, ConversationsModel conversationsModel) {
        mConversations.set(position, conversationsModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        ConversationsModel model = mConversations.remove(fromPosition);
        mConversations.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        conversationList.scrollToPosition(fromPosition);
    }

    public static int getConversationId(int recipientId, int senderId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .beginGroup()
                    .equalTo("RecipientID", recipientId)
                    .or()
                    .equalTo("RecipientID", senderId)
                    .endGroup().findAll().first();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception MainService" + e.getMessage());
            return 0;
        }
    }

    public void updateItem(int userId, boolean isOnline) {
        try {
            Realm realm = ChatonXApplication.getRealmDatabaseInstance();
            int conversationId = getConversationId(userId, PreferenceManager.getID(ChatonXApplication.getInstance()), realm);

            int arraySize = mConversations.size();
            for (int i = 0; i < arraySize; i++) {
                ConversationsModel model = mConversations.get(i);
                if (conversationId == model.getId()) {
                    ConversationsModel conversationsModel = realm.where(ConversationsModel.class).equalTo("id", conversationId).findFirst();
                    conversationsModel.setOnline(isOnline);
                    changeItemAtPosition(i, conversationsModel);
                    break;
                }

            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }


    class ConversationViewHolder extends RecyclerView.ViewHolder {

        Context context;
        @BindView(R.id.user_image)
        ImageView userImage;

        @BindView(R.id.online_indicator)
        View onlineIndicator;

        @BindView(R.id.username)
        EmojiconTextView username;

        @BindView(R.id.last_message)
        EmojiconTextView lastMessage;

        @BindView(R.id.counter)
        TextView counter;

        @BindView(R.id.date_message)
        TextView messageDate;

        @BindView(R.id.status_messages)
        ImageView status_messages;
        @BindView(R.id.file_types)
        ImageView isFile;
        @BindView(R.id.file_types_text)
        TextView FileContent;

        @BindView(R.id.create_group_pro_bar)
        ProgressBar progressBarGroup;

        @BindView(R.id.conversation_row)
        LinearLayout ConversationRow;


        @BindView(R.id.select_icon)
        LinearLayout selectIcon;

        ConversationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            username.setSelected(true);
            context = itemView.getContext();
            setTypeFaces();
        }

        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                username.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                lastMessage.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                counter.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                messageDate.setTypeface(AppHelper.setTypeFace(context, "Futura"));
                FileContent.setTypeface(AppHelper.setTypeFace(context, "Futura"));

            }
        }

        void getProgressBarGroup() {
            progressBarGroup.setVisibility(View.VISIBLE);
            ConversationRow.setEnabled(false);
        }

        void setProgressBarGroup() {
            progressBarGroup.setVisibility(View.GONE);
            ConversationRow.setEnabled(true);
        }

        @SuppressLint("SetTextI18n")
        void setTypeFile(String type) {
            isFile.setVisibility(View.VISIBLE);
            FileContent.setVisibility(View.VISIBLE);
            switch (type) {
                case "image":
                    isFile.setImageResource(R.drawable.ic_photo_camera_gray_24dp);
                    FileContent.setText("Image");
                    break;
                case "video":
                    isFile.setImageResource(R.drawable.ic_videocam_gray_24dp);
                    FileContent.setText("Video");
                    break;
                case "audio":
                    isFile.setImageResource(R.drawable.ic_headset_gray_24dp);
                    FileContent.setText("Audio");
                    break;
                case "document":
                    isFile.setImageResource(R.drawable.ic_document_file_gray_24dp);
                    FileContent.setText("Document");
                    break;
            }

        }

        void isOnline() {
            onlineIndicator.setVisibility(View.VISIBLE);
        }

        void isOffline() {
            onlineIndicator.setVisibility(View.GONE);
        }

        void setGroupImageOffline(String ImageUrl, String name) {
            TextDrawable drawable = textDrawable(name);
            GlideApp.with(context.getApplicationContext())
                    .asBitmap()
                    .load(ImageUrl)
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(userImage);
        }

        void setGroupImage(String ImageUrl, int groupId, String name) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ImageLoader.GetCachedBitmapImage(memoryCache, ImageUrl, context, groupId, AppConstants.GROUP, AppConstants.ROW_PROFILE);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        ImageLoader.SetBitmapImage(bitmap, userImage);
                    } else {
                        TextDrawable drawable = textDrawable(name);

                        if (ImageUrl != null) {
                            BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {


                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    super.onResourceReady(resource, transition);
                                    userImage.setImageBitmap(resource);
                                    ImageLoader.DownloadImage(memoryCache, EndPoints.ROWS_IMAGE_URL + ImageUrl, ImageUrl, context, groupId, AppConstants.GROUP, AppConstants.ROW_PROFILE);

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
                            GlideApp.with(context.getApplicationContext())
                                    .asBitmap()
                                    .load(EndPoints.ROWS_IMAGE_URL + ImageUrl)

                                    .centerCrop()
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(drawable)
                                    .error(drawable)
                                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                    .into(target);
                        } else {
                            userImage.setImageDrawable(drawable);
                        }
                    }
                }
            }.execute();


        }

        TextDrawable textDrawable(String name) {
            if (name == null) {
                name = context.getApplicationContext().getString(R.string.app_name);
            }
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(name);
            String c = String.valueOf(name.toUpperCase().charAt(0));
            return TextDrawable.builder().buildRound(c, color);


        }

        void setUserImage(String ImageUrl, int recipientId, String name) {

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
                        TextDrawable drawable = textDrawable(name);

                        if (ImageUrl != null) {
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
                                    userImage.setImageDrawable(errorDrawable);
                                }

                                @Override
                                public void onLoadStarted(Drawable placeHolderDrawable) {
                                    super.onLoadStarted(placeHolderDrawable);
                                    userImage.setImageDrawable(placeHolderDrawable);
                                }
                            };
                            GlideApp.with(context.getApplicationContext())
                                    .asBitmap()
                                    .load(EndPoints.ROWS_IMAGE_URL + ImageUrl)

                                    .centerCrop()
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(drawable)
                                    .error(drawable)
                                    // .signature(new StringSignature(System.currentTimeMillis()+""))
                                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                    .into(target);
                        } else {
                            userImage.setImageDrawable(drawable);
                        }
                    }
                }
            }.execute();
        }

        void setUsername(String user) {
            username.setText(user);

        }

        void setLastMessage(String LastMessage) {
            lastMessage.setVisibility(View.VISIBLE);
            lastMessage.setTextColor(AppHelper.getColor(context, R.color.colorGray2));
            String last = UtilsString.unescapeJava(LastMessage);
            if (last.length() > 18)
                lastMessage.setText(String.format("%s... ", last.substring(0, 18)));
            else
                lastMessage.setText(last);

        }

        void setMessageDate(String MessageDate) {
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... params) {
                    return UtilsTime.convertDateToString(context, UtilsTime.getCorrectDate(params[0]));
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    messageDate.setText(result);
                }
            }.execute(MessageDate);

        }

        void hideSent() {
            status_messages.setVisibility(View.GONE);
        }

        void showSent(int status) {
            status_messages.setVisibility(View.VISIBLE);
            switch (status) {
                case AppConstants.IS_WAITING:
                    status_messages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                    break;
                case AppConstants.IS_SENT:
                    status_messages.setImageResource(R.drawable.ic_done_gray_24dp);
                    break;
                case AppConstants.IS_DELIVERED:
                    status_messages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                    break;
                case AppConstants.IS_SEEN:
                    status_messages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                    break;

            }

        }

        void setCounter(String Counter) {
            counter.setText(Counter.toUpperCase());
        }

        void hideCounter() {
            counter.setVisibility(View.GONE);
        }


        void showCounter() {
            counter.setVisibility(View.VISIBLE);
        }

        void ChangeStatusUnread() {
            messageDate.setTypeface(null, Typeface.BOLD);
            username.setTypeface(null, Typeface.BOLD);
            if (AppConstants.ENABLE_FONTS_TYPES)
                username.setTypeface(AppHelper.setTypeFace(context, "Futura"));
            messageDate.setTextColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
        }

        void ChangeStatusRead() {
            messageDate.setTypeface(null, Typeface.NORMAL);
            username.setTypeface(null, Typeface.BOLD);
            if (AppConstants.ENABLE_FONTS_TYPES)
                username.setTypeface(AppHelper.setTypeFace(context, "Futura"));
            messageDate.setTextColor(ContextCompat.getColor(context, R.color.colorGray2));
            username.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        }

        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }

}
