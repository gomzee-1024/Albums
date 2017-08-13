package com.utils.gdkcorp.albums;

/**
 * Created by Gautam Kakadiya on 08-08-2017.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.utils.gdkcorp.albums.main";
        public static String STARTFOREGROUND_ACTION = "com.utils.gdkcorp.albums.startforeground";
        public static String STARTFOREGROUNG_JOIN_ACTION = "com.utils.gdkcorp.albums.jointrip";
        public static String STOPFOREGROUND_ACTION = "com.utils.gdkcorp.albums.stopforeground";
        public static final String STOPFOREGROUND_JOINED_ACTION = "com.utils.gdkcorp.albums.stopforegroundjoined";
    }

    public interface NOTIFICATION_ID {
        public static final int FOREGROUND_SERVICE = 101;
        public static final int JOIN_TRIP_NOTIFICATION_ID = 102 ;
    }

    public interface USER {
        public static final String USER_ID_PREFERENCE_KEY = "com.utils.gdkcorp.albums.USER_ID_KEY";
    }

    public interface TRIP {
        public static final String TRIP_ID_PREFERENCE_KEY = "com.utils.gdkcorp.albums.TRIP_ID_KEY";
        public static final String TRIP_NAME_PREFERENCE_KEY = "com.utils.gdkcorp.albums.TRIP_NAME_KEY";
        public static final String TRIP_LOCATION_PREFERENCE_KEY = "com.utils.gdkcorp.albums.TRIP_LOCATION_KEY";
    }

    public interface PREFERENCES {
        public static final String PREFERENCE_KEY = "com.utils.gdkcorp.albums.PREFERNCE_KEY";
    }

    public interface AVATAR {
        public static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/gallery-375e7.appspot.com/o/photos%2Fperson-flat.png?alt=media&token=ecd69438-f969-473e-b030-de2825ac8e55";
    }

    public interface SHARE_DATA_KEYS {
        public static final String SHARE_DATA_KEY = "data";
        public static final String TRIP_NAME_KEY = "trip_name";
        public static final String TRIP_LOCATION_KEY = "trip_location";
        public static final String TRIP_ID_KEY = "trip_id" ;
    }

    public interface FIREBASE_MESSAGING_SERVICE {
        public static final String AUTHORIZATION_KEY = "key=AAAAO1le_tk:APA91bG9FM5z92lfAFoAgaB2Wc0UnShRSYAKKAuiBvx9L8WfmRVK11tjKdJwj2RdXvRvzoCTLGEVo0S2poyTuDv5Zmm8MzXdwEPf78nj_uWIcNVpHtoTK3BMWTQD1JOSN9tIyEuiVJwW";
        public static final String BASE_URL_NOTIFICATION_API = "https://fcm.googleapis.com/";
    }

}
