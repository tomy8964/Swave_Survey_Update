package com.example.user.security.oAuth.profile;

import com.example.user.user.exception.UnKnownProviderException;

import java.util.HashMap;
import java.util.Map;

public abstract class ProfileList {

    private static final Map<String, Profile> profiles = new HashMap<>();

    static {
        registerProfile("kakao", new KakaoProfile());
        registerProfile("google", new GoogleProfile());
        registerProfile("git", new GitProfile());
    }

    private static void registerProfile(String name, Profile profile) {
        profiles.put(name, profile);
    }

    public static Profile findProfile(String profile) {
        if (profiles.containsKey(profile)) {
            return profiles.get(profile);
        } else {
            throw new UnKnownProviderException();
        }
    }
}
