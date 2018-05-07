package com.chatonx.application.api;

import com.chatonx.application.api.apiServices.AuthService;
import com.chatonx.application.api.apiServices.ConversationsService;
import com.chatonx.application.api.apiServices.GroupsService;
import com.chatonx.application.api.apiServices.MessagesService;
import com.chatonx.application.api.apiServices.UsersService;
import com.chatonx.application.app.ChatonXApplication;

/**
 * Created by Abderrahim El imame on 4/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class APIHelper {

    public static UsersService initialApiUsersContacts() {
        APIService mApiService = APIService.with(ChatonXApplication.getInstance());
        return new UsersService(ChatonXApplication.getRealmDatabaseInstance(), ChatonXApplication.getInstance(), mApiService);
    }


    public static GroupsService initializeApiGroups() {
        APIService mApiService = APIService.with(ChatonXApplication.getInstance());
        return new GroupsService(ChatonXApplication.getRealmDatabaseInstance(), ChatonXApplication.getInstance(), mApiService);
    }

    public static ConversationsService initializeConversationsService() {
        return new ConversationsService(ChatonXApplication.getRealmDatabaseInstance());
    }

    public static MessagesService initializeMessagesService() {
        return new MessagesService(ChatonXApplication.getRealmDatabaseInstance());
    }

    public static AuthService initializeAuthService() {
        APIService mApiService = APIService.with(ChatonXApplication.getInstance());
        return new AuthService(ChatonXApplication.getInstance(), mApiService);
    }
}
