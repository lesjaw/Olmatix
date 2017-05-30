package com.olmatix.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by android on 5/11/2017.
 */

public class SessionManager {

        // Shared Preferences
        SharedPreferences pref;

        // Editor for Shared preferences
        SharedPreferences.Editor editor;

        // Context
        Context _context;

        // Shared pref mode
        int PRIVATE_MODE = 0;

        // Sharedpref file name
        private static final String PREF_NAME = "OlmatixPref";


        public static final String KEY_NAME = "name";


        // Constructor
        public SessionManager(Context context){
            this._context = context;
            pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = pref.edit();
        }


        public void createNodeSession(String name){

            // Storing name in pref
            editor.putString(KEY_NAME, name);


            // commit changes
            editor.commit();
        }

        /**
         * Get stored session data
         * */
        public  String getNodeDetails(){

            // return user
            return  pref.getString(KEY_NAME, null);
        }

}
