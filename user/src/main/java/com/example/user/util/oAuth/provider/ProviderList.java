package com.example.user.util.oAuth.provider;

import com.example.user.user.exception.UnKnownProviderException;

public abstract class ProviderList {

    public static Provider findProvider(String provider) {
        if (provider.equals("kakao")) {
            return new Kakao();
        } else if (provider.equals("google")) {
            return new Google();
        } else if (provider.equals("git")) {
            return new Git();
        }

        throw new UnKnownProviderException();
    }
}
