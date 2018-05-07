package com.chatonx.application.helpers.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.chatonx.application.R;
import com.chatonx.application.activities.main.MainActivity;
import com.chatonx.application.activities.messages.MessagesActivity;
import com.chatonx.application.activities.messages.MessagesPopupActivity;
import com.chatonx.application.activities.settings.PreferenceSettingsManager;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.EndPoints;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.Files.cache.ImageLoader;
import com.chatonx.application.helpers.Files.cache.MemoryCache;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.helpers.UtilsPhone;
import com.chatonx.application.helpers.UtilsString;
import com.chatonx.application.models.messages.ConversationsModel;
import com.chatonx.application.models.messages.MessagesModel;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.ui.ColorGenerator;
import com.chatonx.application.ui.CropSquareTransformation;
import com.chatonx.application.ui.TextDrawable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.chatonx.application.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;

/**
 * Created by Abderrahim El imame on 6/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NotificationsManager {


    private static NotificationManager mNotificationManager;
    // private int numMessages = 0;
    private static MemoryCache memoryCache;

    public NotificationsManager() {
    }

    @SuppressLint("StaticFieldLeak")
    public static void showUserNotification(Context mContext, int conversationID, String phone, String message, int userId, String Avatar) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //

        memoryCache = new MemoryCache();
        //  String text = UtilsString.unescapeJava(message);
        Intent messagingIntent = new Intent(mContext, MainActivity.class);
        messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent messagingPopupIntent = new Intent(mContext, MainActivity.class);
        messagingPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        int counterUnreadConversation = getUnreadConversationsCounter();

        int counterUnreadMessages = getUnreadMessagesCounter(mContext);
        RealmList<MessagesModel> msgs = getNotificationMessages(userId);


        if (counterUnreadConversation == 1) {
            /**
             * this for default activity
             */
            messagingIntent = new Intent(mContext, MessagesActivity.class);
            messagingIntent.putExtra("conversationID", conversationID);
            messagingIntent.putExtra("recipientID", userId);
            messagingIntent.putExtra("isGroup", false);
            /**
             * this for popup activity
             */
            messagingPopupIntent = new Intent(mContext, MessagesPopupActivity.class);
            messagingPopupIntent.putExtra("conversationID", conversationID);
            messagingPopupIntent.putExtra("recipientID", userId);
            messagingPopupIntent.putExtra("isGroup", false);

        }


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(messagingIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        TaskStackBuilder stackPopupBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack

        stackPopupBuilder.addParentStack(MessagesPopupActivity.class);
        // Adds the Intent to the top of the stack
        stackPopupBuilder.addNextIntent(messagingPopupIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultMessagingPopupIntent = stackPopupBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder mNotifyBuilder;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String username = phone;
        try {

            if (msgs.isLoaded()) {
                if (msgs.size() != 0) {
                    String name = UtilsPhone.getContactName(phone);
                    if (name != null) {
                        username = name;
                    } else {
                        username = phone;
                    }
                } else {
                    username = phone;
                }
            }

        } catch (Exception e) {
            AppHelper.LogCat(" " + e.getMessage());
            username = phone;
        }
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = username;// The id of the channel.
            CharSequence name = ChatonXApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setChannelId(CHANNEL_ID);

            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = username;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext,CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH);

        }
        if (msgs.isLoaded())
            if (msgs.size() != 0) {
                if (counterUnreadConversation == 1) {
                    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultMessagingPopupIntent).build();
                    mNotifyBuilder.addAction(action);

                    inboxStyle.setBigContentTitle(username);

                    mNotifyBuilder.setContentTitle(username);
                    if (msgs.isValid())
                        mNotifyBuilder.setContentText(UtilsString.unescapeJava(msgs.get(msgs.size() - 1).getMessage()));
                    inboxStyle.setSummaryText(counterUnreadMessages + " " + mContext.getString(R.string.new_messages_notify));
                    for (MessagesModel m : msgs) {
                        inboxStyle.addLine(UtilsString.unescapeJava(m.getMessage()));
                    }

                } else {
                    inboxStyle.setBigContentTitle(mContext.getResources().getString(R.string.app_name));

                    mNotifyBuilder.setContentTitle(username);
                    if (msgs.isValid())
                        mNotifyBuilder.setContentText(UtilsString.unescapeJava(msgs.get(msgs.size() - 1).getMessage()));
                    inboxStyle.setSummaryText(counterUnreadMessages + " " + mContext.getString(R.string.messages_from_notify) + " " + counterUnreadConversation + " " + mContext.getString(R.string.chats_notify));
                    for (MessagesModel m : msgs) {

                        if (m.getUsername() != null)
                            inboxStyle.addLine("".concat(m.getUsername()).concat(" : ").concat(UtilsString.unescapeJava(m.getMessage())));
                        else
                            inboxStyle.addLine("".concat(m.getPhone()).concat(" : ").concat(UtilsString.unescapeJava(m.getMessage())));

                    }
                }
            } else {
                AppHelper.LogCat("size is 0 ");
            }
        mNotifyBuilder.setStyle(inboxStyle);
        TextDrawable drawable = textDrawable(mContext, username);
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, Avatar, mContext, userId, AppConstants.USER, AppConstants.ROW_PROFILE);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE, false);
                    Bitmap circleBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    BitmapShader shader = new BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);
                    paint.setAntiAlias(true);
                    Canvas c = new Canvas(circleBitmap);
                    c.drawCircle(scaledBitmap.getWidth() / 2, scaledBitmap.getHeight() / 2, scaledBitmap.getWidth() / 2, paint);
                    return circleBitmap;
                } else {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    mNotifyBuilder.setLargeIcon(bitmap);
                } else {
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }
                    };
                    Picasso.with(mContext)
                            .load(EndPoints.ROWS_IMAGE_URL + Avatar)
                            .transform(new CropSquareTransformation())
                            .resize(AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE)
                            .into(target);
                }
            }
        }.execute();


        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_notifications_settings_vibrate(mContext)) {
            long[] vibrate = new long[]{2000, 2000, 2000, 2000, 2000};
            mNotifyBuilder.setVibrate(vibrate);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_notifications_settings_light(mContext);
        if (colorLight != null) {
            mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            mNotifyBuilder.setDefaults(defaults);
        }


        mNotifyBuilder.setAutoCancel(true);

        mNotificationManager.notify(userId, mNotifyBuilder.build());

        SetupBadger(mContext);
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));

    }

    private static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private static TextDrawable textDrawable(Context context, String name) {
        if (name == null) {
            name = context.getString(R.string.app_name);
        }
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(name);
        String c = String.valueOf(name.toUpperCase().charAt(0));
        return TextDrawable.builder().buildRound(c, color);


    }

    private static int getUnreadMessagesCounter(Context mContext) {
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        List<MessagesModel> messagesModels = realm.where(MessagesModel.class)
                .notEqualTo("id", 0)
                .equalTo("status", AppConstants.IS_WAITING)
                .notEqualTo("senderID", PreferenceManager.getID(mContext))
                .findAll();
        // ConversationsModel conversationsModel1 = realm.where(ConversationsModel.class).findFirst();
        //  int unRead = Integer.parseInt(conversationsModel1 != null ? conversationsModel1.getUnreadMessageCounter() : "0");
        if (!realm.isClosed()) realm.close();
        return messagesModels.size();
    }

    private static int getUnreadConversationsCounter() {
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        List<ConversationsModel> conversationsModel1 = realm.where(ConversationsModel.class)
                .notEqualTo("UnreadMessageCounter", "0")
                .findAll();
        int counter = conversationsModel1.size();
        if (!realm.isClosed()) realm.close();
        return counter;
    }

    private static RealmList<MessagesModel> getNotificationMessages(int userId) {
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        RealmList<MessagesModel> messagesModels = new RealmList<>();
        RealmResults<MessagesModel> messagesModel = realm.where(MessagesModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("senderID", userId).findAll();
        messagesModels.addAll(messagesModel);
       // if (!realm.isClosed()) realm.close();
        return messagesModels;
    }


    @SuppressLint("StaticFieldLeak")
    public static void showGroupNotification(Context mContext, Intent resultIntent, Intent messagingGroupPopupIntent, String groupName, String message, int groupId, String Avatar) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //


        memoryCache = new MemoryCache();

        String text = UtilsString.unescapeJava(message);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        TaskStackBuilder stackGroupPopupBuilder = TaskStackBuilder.create(mContext);
        stackGroupPopupBuilder.addParentStack(MessagesPopupActivity.class);
        // Adds the Intent to the top of the stack
        stackGroupPopupBuilder.addNextIntent(messagingGroupPopupIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultMessagingGroupPopupIntent = stackGroupPopupBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        final NotificationCompat.Builder mNotifyBuilder;


        //   ++numMessages;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultMessagingGroupPopupIntent).build();
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = groupName;// The id of the channel.
            CharSequence name = ChatonXApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(action)
                    .setContentTitle(groupName)
                    .setContentText(text)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setChannelId(CHANNEL_ID);

            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            mNotifyBuilder = new NotificationCompat.Builder(mContext)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(action)
                    .setContentTitle(groupName)
                    .setContentText(text)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    //  .setNumber(numMessages)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        TextDrawable drawable = textDrawable(mContext, groupName);
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {

                Bitmap bitmap = ImageLoader.GetCachedBitmapImage(memoryCache, Avatar, mContext, groupId, AppConstants.USER, AppConstants.ROW_PROFILE);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE, false);
                    Bitmap circleBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    BitmapShader shader = new BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);
                    paint.setAntiAlias(true);
                    Canvas c = new Canvas(circleBitmap);
                    c.drawCircle(scaledBitmap.getWidth() / 2, scaledBitmap.getHeight() / 2, scaledBitmap.getWidth() / 2, paint);
                    return circleBitmap;
                } else {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    mNotifyBuilder.setLargeIcon(bitmap);
                } else {
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }
                    };
                    Picasso.with(mContext)
                            .load(EndPoints.ROWS_IMAGE_URL + Avatar)
                            .transform(new CropSquareTransformation())
                            .resize(AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE)
                            .into(target);
                }
            }
        }.execute();
        mNotifyBuilder.setAutoCancel(true);


        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_group_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_group_notifications_settings_vibrate(mContext)) {
            long[] vibrate = new long[]{2000, 2000, 2000, 2000, 2000};
            mNotifyBuilder.setVibrate(vibrate);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_group_notifications_settings_light(mContext);
        if (colorLight != null) {
            mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            mNotifyBuilder.setDefaults(defaults);
        }


        mNotificationManager.notify(groupId, mNotifyBuilder.build());

    }

    /**
     * method to get manager for notification
     */
    public static boolean getManager() {
        if (mNotificationManager != null) {
            return true;
        } else {
            return false;
        }

    }

    /***
     * method to cancel a specific notification
     *
     * @param index
     */
    public static void cancelNotification(int index) {
        //    numMessages = 0;
        mNotificationManager.cancel(index);
    }

    /**
     * method to set badger counter for the app
     */
    public static void SetupBadger(Context mContext) {

        int messageBadgeCounter = 0;
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        String DeviceName = android.os.Build.MANUFACTURER;
        String[] DevicesName = {
                "Sony",
                "Samsung",
                "LG",
                "HTC",
                "Xiaomi",
                "ASUS",
                "ADW",
                "NOVA",
                "Huawei",
                "ZUK",
                "APEX",
                "OPPO",
                "ZTE",
                "EverythingMe"
        };

        for (String device : DevicesName) {
            if (DeviceName.equals(device.toLowerCase())) {
                try {
                    List<MessagesModel> messagesModels = realm.where(MessagesModel.class)
                            .notEqualTo("id", 0)
                            .equalTo("status", AppConstants.IS_WAITING)
                            .notEqualTo("senderID", PreferenceManager.getID(mContext))
                            .findAll();

                    if (messagesModels.size() != 0) {
                        messageBadgeCounter = messagesModels.size();
                    }
                    try {
                        ShortcutBadger.applyCount(mContext.getApplicationContext(), messageBadgeCounter);
                    } catch (Exception e) {
                        AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                }
                break;
            }
        }
        if (!realm.isClosed())
            realm.close();

    }
}
