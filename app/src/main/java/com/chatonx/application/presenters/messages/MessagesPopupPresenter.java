package com.chatonx.application.presenters.messages;


import android.os.Handler;

import com.chatonx.application.activities.messages.MessagesPopupActivity;
import com.chatonx.application.api.APIService;
import com.chatonx.application.api.apiServices.GroupsService;
import com.chatonx.application.api.apiServices.MessagesService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.helpers.notifications.NotificationsManager;
import com.chatonx.application.interfaces.Presenter;
import com.chatonx.application.models.messages.ConversationsModel;
import com.chatonx.application.models.messages.MessagesModel;
import com.chatonx.application.models.users.Pusher;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

import static com.chatonx.application.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.chatonx.application.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesPopupPresenter implements Presenter {
    private final MessagesPopupActivity view;
    private final Realm realm;
    private int RecipientID, ConversationID, GroupID;
    private Boolean isGroup;
    private MessagesService mMessagesService;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;

    public MessagesPopupPresenter(MessagesPopupActivity messagesPopupActivity) {
        this.view = messagesPopupActivity;
        this.realm = ChatonXApplication.getRealmDatabaseInstance();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(view)) EventBus.getDefault().register(view);

        mDisposable = new CompositeDisposable();
        if (view.getIntent().getExtras() != null) {
            if (view.getIntent().hasExtra("conversationID")) {
                ConversationID = view.getIntent().getExtras().getInt("conversationID");
            }
            if (view.getIntent().hasExtra("recipientID")) {
                RecipientID = view.getIntent().getExtras().getInt("recipientID");
            }

            if (view.getIntent().hasExtra("groupID")) {
                GroupID = view.getIntent().getExtras().getInt("groupID");
            }

            if (view.getIntent().hasExtra("isGroup")) {
                isGroup = view.getIntent().getExtras().getBoolean("isGroup");
            }

        }

        APIService mApiService = APIService.with(view.getApplicationContext());
        mMessagesService = new MessagesService(realm);
        mUsersContacts = new UsersService(realm, view.getApplicationContext(), mApiService);
        mDisposable.addAll(mUsersContacts.getContactInfo(PreferenceManager.getID(view)).subscribe(view::updateContact, view::onErrorLoading))
        ;
        if (isGroup) {
            GroupsService mGroupsService = new GroupsService(realm, view.getApplicationContext(), mApiService);
            mDisposable.addAll(
                    mGroupsService.getGroupInfo(GroupID).subscribe(view::updateGroupInfo, view::onErrorLoading),
                    mGroupsService.updateGroupMembers(GroupID).subscribe(view::ShowGroupMembers, view::onErrorLoading));
            loadLocalGroupData();
            new Handler().postDelayed(this::updateGroupConversationStatus, 500);
        } else {
            getRecipientInfo();
            loadLocalData();
            new Handler().postDelayed(this::updateConversationStatus, 500);
        }

    }

    private void getRecipientInfo() {

        mDisposable.addAll(
                mUsersContacts.getContactInfo(RecipientID).subscribe(contactsModel -> {
                    view.updateContactRecipient(contactsModel);
                    int ConversationID = getConversationId(contactsModel.getId(), PreferenceManager.getID(view), realm);
                    if (ConversationID != 0) {
                        realm.executeTransaction(realm1 -> {
                            ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                            assert conversationsModel != null;
                            conversationsModel.setRecipientImage(contactsModel.getImage());
                            realm1.copyToRealmOrUpdate(conversationsModel);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, ConversationID));
                        });
                    }
                }, view::onErrorLoading));

    }

    public void updateConversationStatus() {
        try {
            realm.executeTransaction(realm1 -> {
                ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).equalTo("RecipientID", RecipientID).findFirst();

                if (conversationsModel1 != null) {
                    conversationsModel1.setStatus(AppConstants.IS_SEEN);
                    conversationsModel1.setUnreadMessageCounter("0");
                    realm1.copyToRealmOrUpdate(conversationsModel1);

                    List<MessagesModel> messagesModel = realm1.where(MessagesModel.class).equalTo("conversationID", ConversationID).equalTo("senderID", RecipientID).findAll();
                    for (MessagesModel messagesModel1 : messagesModel) {
                        if (messagesModel1.getStatus() == AppConstants.IS_WAITING) {
                            messagesModel1.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel1);
                        }
                    }
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    NotificationsManager.SetupBadger(view);
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_READ, ConversationID));
                }
            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no conversation unRead MessagesPresenter ");
        }
    }

    public void updateGroupConversationStatus() {
        try {
            realm.executeTransaction(realm1 -> {
                ConversationsModel conversationsModel1 = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).equalTo("groupID", GroupID).findFirst();

                if (conversationsModel1 != null) {
                    conversationsModel1.setStatus(AppConstants.IS_SEEN);
                    conversationsModel1.setUnreadMessageCounter("0");
                    realm1.copyToRealmOrUpdate(conversationsModel1);

                    List<MessagesModel> messagesModel = realm1.where(MessagesModel.class).equalTo("conversationID", ConversationID).notEqualTo("senderID", PreferenceManager.getID(view)).findAll();
                    for (MessagesModel messagesModel1 : messagesModel) {
                        if (messagesModel1.getStatus() == AppConstants.IS_WAITING) {
                            messagesModel1.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel1);
                        }
                    }
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    NotificationsManager.SetupBadger(view);
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_READ, ConversationID));
                }
            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no conversation unRead MessagesPresenter ");
        }
    }

    private void loadLocalGroupData() {
        if (NotificationsManager.getManager())
            NotificationsManager.cancelNotification(GroupID);
        mDisposable.addAll(mMessagesService.getConversation(ConversationID).subscribe(view::ShowMessages, view::onErrorLoading, view::onHideLoading))
        ;
    }

    private void loadLocalData() {
        if (NotificationsManager.getManager())
            NotificationsManager.cancelNotification(RecipientID);
        try {
            mDisposable.addAll(mMessagesService.getConversation(ConversationID, RecipientID, PreferenceManager.getID(view)).subscribe(view::ShowMessages, view::onErrorLoading))
            ;
        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
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
        EventBus.getDefault().unregister(view);
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

    /**
     * method to get a conversation id
     *
     * @param recipientId this is the first parameter for getConversationId method
     * @param senderId    this is the second parameter for getConversationId method
     * @return conversation id
     */
    private int getConversationId(int recipientId, int senderId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .beginGroup()
                    .equalTo("RecipientID", recipientId)
                    .or()
                    .equalTo("RecipientID", senderId)
                    .endGroup().findAll().first();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception ContactFragment" + e.getMessage());
            return 0;
        }
    }
}
