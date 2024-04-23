package com.example.user.security.oAuth.provider;

import com.example.user.user.exception.UnKnownProviderException;

import java.util.HashMap;
import java.util.Map;

public abstract class ProviderList {

    private static final Map<String, Provider> providers = new HashMap<>();

    static {
        // Provider 등록
        registerProvider("kakao", new Kakao());
        registerProvider("google", new Google());
        registerProvider("git", new Git());
    }

    private static void registerProvider(String name, Provider provider) {
        providers.put(name, provider);
    }

    public static Provider findProvider(String provider) {
        if (providers.containsKey(provider)) {
            return providers.get(provider);
        } else {
            throw new UnKnownProviderException();
        }
    }
}
