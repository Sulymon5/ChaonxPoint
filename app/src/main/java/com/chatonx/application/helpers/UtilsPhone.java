package com.chatonx.application.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.chatonx.application.R;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.phoneFormat.PhoneFormat;
import com.chatonx.application.models.users.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Abderrahim El imame on 03/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UtilsPhone {
    private static volatile UtilsPhone Instance = null;
    private String[] projectionPhones = {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };
    private String[] projectionNames = {
            ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
    };
    public ConcurrentHashMap<Integer, Contact> contactsMap = new ConcurrentHashMap<Integer, Contact>();


    private ArrayList<ContactsModel> mListContacts = new ArrayList<ContactsModel>();
    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private static String name = null;


    public static UtilsPhone getInstance() {
        UtilsPhone localInstance = Instance;
        if (localInstance == null) {
            synchronized (UtilsPhone.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UtilsPhone();
                }
            }
        }
        return localInstance;
    }

    /**
     * method to retrieve all contacts from the book
     *
     * @return return value
     */
    public ArrayList<ContactsModel> GetPhoneContacts() {
        String country = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ChatonXApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                country = telephonyManager.getSimCountryIso().toUpperCase();
            } else {
                country = AppConstants.DEFAULT_COUNTRY_CODE;//"JO";
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        contactsMap = readContactsFromPhoneBook();
        for (Map.Entry entry : contactsMap.entrySet()) {

            Contact c = (Contact) entry.getValue();
            String phoneNumber = "";
            int cc = -1;
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber NumberProto = phoneUtil.parse(c.phones.get(0), country);
                if (String.valueOf(NumberProto.getNationalNumber()).length() < 9) {
                    continue;
                }
                phoneNumber = phoneUtil.format(NumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);


            } catch (NumberParseException e) {
                AppHelper.LogCat("NumberParseException was thrown: " + e.toString());
            }
//            AppHelper.LogCat(phoneNumber);

            //FileLog.e("contacts:",x +" - locale:"+country+" name:"+c.first_name +" "+c.last_name+" sPhone:"+phone);

 /*       ContentResolver contentResolver = ChatonXApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cur = contentResolver.query(ContactMobileNumbQuery.CONTENT_URI, ContactMobileNumbQuery.PROJECTION, ContactMobileNumbQuery.SELECTION, null, ContactMobileNumbQuery.SORT_ORDER);
        if (cur != null) {
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    ContactsModel contactsModel = new ContactsModel();
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                    String image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


                    //     AppHelper.LogCat("number phone --> " + phoneNumber);
                    if (name.contains("\\s+")) {
                        String[] nameArr = name.split("\\s+");
                        contactsModel.setUsername(nameArr[0] + nameArr[1]);
                        // AppHelper.LogCat("Fname --> " + nameArr[0]);
                        // AppHelper.LogCat("Lname --> " + nameArr[1]);
                    } else {
                        contactsModel.setUsername(name);
                        //AppHelper.LogCat("name" + name);
                    }*/
            if (phoneNumber != null) {

                String Regex = "[^\\d]";
          /*      String PhoneDigits = phoneNumber.replaceAll(Regex, "");
                boolean isValid = !(PhoneDigits.length() < 6 || PhoneDigits.length() > 13);
                String phNumberProto = PhoneDigits.replaceAll("-", "");
                String PhoneNo;
                if (PhoneDigits.length() != 10) {
                    PhoneNo = "+";
                    PhoneNo = PhoneNo.concat(phNumberProto);
                } else {
                    PhoneNo = phNumberProto;
                }*/


                // AppHelper.LogCat("phoneNumber --> " + phoneNumber);
                String phoneNumberTmpFinal;
                Phonenumber.PhoneNumber phoneNumberInter = getPhoneNumber(phoneNumber);
                if (phoneNumberInter != null) {
                    //  AppHelper.LogCat("phoneNumberInter --> " + phoneNumberInter.getNationalNumber());
                    phoneNumberTmpFinal = String.valueOf(phoneNumberInter.getNationalNumber());

//                    AppHelper.LogCat("phoneNumberInter " + phoneNumberTmpFinal);
              //      AppHelper.LogCat("PhoneNo " + phoneNumber);

                    // AppHelper.LogCat("phoneNumberTmpFinal --> " + phoneNumberTmpFinal);
                    //    if (isValid) {
                    ContactsModel contactsModel = new ContactsModel();
                    //    AppHelper.LogCat("PhoneNo --> " + PhoneNo);
                    contactsModel.setPhoneTmp(phoneNumberTmpFinal);
                    contactsModel.setPhone(phoneNumber.trim());
                    contactsModel.setUsername(c.first_name + " " + c.last_name);
                    contactsModel.setContactID(c.id);
                    contactsModel.setImage(c.user_image);

                    int flag = 0;
                    int arraySize = mListContacts.size();
                    if (arraySize == 0) {
                        if (!phoneNumber.equals(PreferenceManager.getPhone(ChatonXApplication.getInstance())))
                            mListContacts.add(contactsModel);
                    }
                    //remove duplicate numbers
                    for (int i = 0; i < arraySize; i++) {

                        if (!mListContacts.get(i).getPhone().trim().equals(phoneNumber.trim())) {
                            flag = 1;

                        } else {
                            flag = 0;
                            break;
                        }
                    }

                    if (flag == 1) {
                        if (!phoneNumber.equals(PreferenceManager.getPhone(ChatonXApplication.getInstance())))
                            mListContacts.add(contactsModel);
                    }


                   /* } else {
                        //   AppHelper.LogCat("invalid phone --> ");
                    }*/
                }
            }


        }
                /*}
                cur.close();
            }
        }*/
        AppHelper.LogCat("mListContacts " + mListContacts.size());
        return mListContacts;
    }

    public ConcurrentHashMap<Integer, Contact> readContactsFromPhoneBook() {
        ConcurrentHashMap<Integer, Contact> contactsMap = new ConcurrentHashMap<Integer, Contact>();
        try {
            ContentResolver cr = ChatonXApplication.getInstance().getContentResolver();

            HashMap<String, Contact> shortContacts = new HashMap<String, Contact>();
            String ids = "";
            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projectionPhones, null, null, null);
            if (pCur != null) {
                if (pCur.getCount() > 0) {
                    while (pCur.moveToNext()) {
                        String number = pCur.getString(1);
                        String image_uri = pCur.getString(4);
                        //  String image_uri = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                        if (number == null || number.length() == 0) {
                            continue;
                        }
                        number = PhoneFormat.stripExceptNumbers(number, true);
                        if (number.length() == 0) {
                            continue;
                        }

                        String shortNumber = number;

                        if (number.startsWith("+")) {
                            shortNumber = number.substring(1);
                        }

                        if (shortContacts.containsKey(shortNumber)) {
                            continue;
                        }

                        Integer id = pCur.getInt(0);
                        if (ids.length() != 0) {
                            ids += ",";
                        }
                        ids += id;

                        int type = pCur.getInt(2);
                        Contact contact = contactsMap.get(id);
                        if (contact == null) {
                            contact = new Contact();
                            contact.first_name = "";
                            contact.last_name = "";
                            contact.id = id;
                            contactsMap.put(id, contact);
                        }

                        contact.shortPhones.add(shortNumber);
                        contact.user_image = image_uri;
                        contact.phones.add(number);
                        contact.phoneDeleted.add(0);

                        if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                            contact.phoneTypes.add(pCur.getString(3));
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                            contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneHome));
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneMobile));
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                            contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneWork));
                        } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MAIN) {
                            contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneMain));
                        } else {
                            contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneOther));
                        }
                        shortContacts.put(shortNumber, contact);
                    }
                }
                pCur.close();
            }

            pCur = cr.query(ContactsContract.Data.CONTENT_URI, projectionNames, ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " IN (" + ids + ") AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'", null, null);
            if (pCur != null && pCur.getCount() > 0) {
                while (pCur.moveToNext()) {
                    int id = pCur.getInt(0);
                    String fname = pCur.getString(1);
                    String sname = pCur.getString(2);
                    String sname2 = pCur.getString(3);
                    String mname = pCur.getString(4);
                    Contact contact = contactsMap.get(id);
                    if (contact != null) {
                        contact.first_name = fname;
                        contact.last_name = sname;
                        if (contact.first_name == null) {
                            contact.first_name = "";
                        }
                        if (mname != null && mname.length() != 0) {
                            if (contact.first_name.length() != 0) {
                                contact.first_name += " " + mname;
                            } else {
                                contact.first_name = mname;
                            }
                        }
                        if (contact.last_name == null) {
                            contact.last_name = "";
                        }
                        if (contact.last_name.length() == 0 && contact.first_name.length() == 0 && sname2 != null && sname2.length() != 0) {
                            contact.first_name = sname2;
                        }
                    }
                }
                pCur.close();
            }

            try {
                pCur = cr.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{"display_name", ContactsContract.RawContacts.SYNC1, ContactsContract.RawContacts.CONTACT_ID}, ContactsContract.RawContacts.ACCOUNT_TYPE + " = " + "'com.whatsapp'", null, null);
                if (pCur != null) {
                    while ((pCur.moveToNext())) {
                        String phone = pCur.getString(1);
                        if (phone == null || phone.length() == 0) {
                            continue;
                        }
                        boolean withPlus = phone.startsWith("+");
                        phone = AppHelper.parseIntToString(phone);
                        if (phone == null || phone.length() == 0) {
                            continue;
                        }
                        String shortPhone = phone;
                        if (!withPlus) {
                            phone = "+" + phone;
                        }

                        if (shortContacts.containsKey(shortPhone)) {
                            continue;
                        }

                        String name = pCur.getString(0);
                        if (name == null || name.length() == 0) {
                            name = PhoneFormat.getInstance().format(phone);
                        }

                        String[] args = name.split(" ", 2);

                        Contact contact = new Contact();
                        if (args.length > 0) {
                            contact.first_name = args[0];
                        } else {
                            contact.first_name = "";
                        }
                        if (args.length > 1) {
                            contact.last_name = args[1];
                        } else {
                            contact.last_name = "";
                        }
                        contact.id = pCur.getInt(2);
                        contactsMap.put(contact.id, contact);

                        contact.phoneDeleted.add(0);
                        contact.shortPhones.add(shortPhone);
                        contact.phones.add(phone);
                        contact.phoneTypes.add(ChatonXApplication.getInstance().getString(R.string.PhoneMobile));
                        shortContacts.put(shortPhone, contact);
                    }
                    pCur.close();
                }
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
            contactsMap.clear();
        }
        return contactsMap;
    }

    public static class Contact {
        public int id;
        public ArrayList<String> phones = new ArrayList<String>();
        public ArrayList<String> phoneTypes = new ArrayList<String>();
        public ArrayList<String> shortPhones = new ArrayList<String>();
        public ArrayList<Integer> phoneDeleted = new ArrayList<Integer>();
        public String first_name;
        public String last_name;
        public String user_image;
    }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid(String phone) {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber(phone);
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber(String phone) {
        final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
        try {
            return mPhoneUtil.parse(phone, DEFAULT_COUNTRY);
        } catch (NumberParseException ignored) {
            return null;
        }
    }

    /**
     * method to get contact ID
     *
     * @param mContext this is the first parameter for getContactID  method
     * @param phone    this is the second parameter for getContactID  method
     * @return return value
     */
    public static long getContactID(Activity mContext, String phone) {
        if (PermissionHandler.checkPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read contact data permission already granted.");
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            long idPhone = 0;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        idPhone = Long.valueOf(mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                    }
                }
            } finally {
                mcursor.close();
            }
            return idPhone;
        } else {
            AppHelper.LogCat("Please request Read contact data permission.");
            PermissionHandler.requestPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
            return 0;
        }

    }


    /**
     * method to check for contact name
     *
     * @param phone this is the second parameter for getContactName  method
     * @return return value
     */
    @SuppressLint("StaticFieldLeak")
    public static String getContactName(String phone) {
        try {
            return new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... params) {
                    try {

                        // CONTENT_FILTER_URI allow to search contact by phone number
                        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(params[0]));
                        // This query will return NAME and ID of contact, associated with phone //number.
                        Cursor mcursor = ChatonXApplication.getInstance().getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
                        //Now retrieve _ID from query result
                        String name = null;
                        try {
                            if (mcursor != null) {
                                if (mcursor.moveToFirst()) {
                                    name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                }
                            }
                        } finally {
                            mcursor.close();
                        }
                        return name;
                    } catch (Exception e) {
                        return e.getMessage();
                    }
                }

                @Override
                protected void onPostExecute(String username) {
                    super.onPostExecute(username);
                    // String name = UtilsPhone.getContactName(mActivity, conversationsModel.getRecipientPhone());
//                    AppHelper.LogCat("name " + username);
                    name = username;
                }
            }.execute(phone).get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }

    }

    /**
     * method to check if user contact exist
     *
     * @param phone this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String phone) {
        try {
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            String name = null;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                mcursor.close();
            }

            return name != null;
        } catch (Exception e) {
            AppHelper.LogCat(e);
            return false;
        }
    }
}
