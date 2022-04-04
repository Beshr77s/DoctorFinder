package com.elmobarmij.chatting.utiles;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatApp";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_STATUS = "status";
    public static final String KEY_FCM_TOKEN = "fcmtoken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT ="chat";
    public static final String KEY_SENDER_ID ="senderId";
    public static final String KEY_RECEIVER_ID ="receiverId";
    public static final String KEY_MESSAGE ="message";
    public static final String KEY_TIMESTAMP ="time";
    public static final String KEY_COLLECTION_CONVERSION ="conversion";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LastMessage= "lastessage";


    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTH = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> REMOTE_MSG_HEADER = null;
    public static final HashMap<String,String> getRemoteMsgHeader(){
        if (REMOTE_MSG_HEADER == null){
            REMOTE_MSG_HEADER = new HashMap<>();
            REMOTE_MSG_HEADER.put(REMOTE_MSG_AUTH,"key=AAAAj4su0TM:APA91bHKq_gotnh3TCJqeJ6A3i54izQObrMhBjaRpDIXECkYoQtA19PHPpsGz3HHUKHN8XlXjkLH9jo_wqS3PSBWZnvvPN_y4Xt8Al2ZyO0i_3gMhdhdAK1Tm6QDkVVmZCYUrFxGtWzN");
        }
        REMOTE_MSG_HEADER.put(
                REMOTE_MSG_CONTENT_TYPE
                ,"application/json"
        );
    return REMOTE_MSG_HEADER;
    }

}
