package com.crazyhands.dictionary;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.net.Authenticator;

/**
 * Created by crazyhands on 19/08/2017.
 */

public class AuthenticatorService extends Service {
    // Instance field that stores the authenticator object

    private authenticator mAuthenticator;


    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new authenticator(this);
    }
    /*
         * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
            */



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
