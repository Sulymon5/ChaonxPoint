package com.chatonx.application.helpers.Files.backup;


import com.chatonx.application.helpers.AppHelper;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Abderrahim El imame .
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class RealmMigrations implements RealmMigration {


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RealmMigrations);
    }


    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        AppHelper.LogCat("onUpgrade() migrating oldVersion = " + oldVersion + "; newVersion = " + newVersion);
        if (oldVersion == 1) {//old database version
            setupDBVersion2andAbove(schema);
            oldVersion++;
        }

        if (oldVersion == 2) {//old database version
            setupDBVersion3andAbove(schema);
            oldVersion++;
        }
    }

    private void setupDBVersion2andAbove(RealmSchema schema) {
        RealmObjectSchema userSchema = schema.get("UsersModel");
        //userSchema.addField("follow_owner", Integer.class);
    }

    private void setupDBVersion3andAbove(RealmSchema schema) {
        //final RealmObjectSchema userSchema = schema.get("MessagesModel");
        // userSchema.addField("type", String.class);
        //

    }
}