package com.example.tam.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by TAM on 1/8/2017.
 */

public class MoviesAuthenticatorService extends Service {

    private MoviesAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create new authenticator object
        mAuthenticator = new MoviesAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
