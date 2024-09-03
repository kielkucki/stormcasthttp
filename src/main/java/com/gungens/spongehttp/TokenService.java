package com.gungens.spongehttp;

import java.lang.annotation.Documented;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TokenService extends TreeMap<String, String> {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    //username, token
    public TokenService() {
        super();
    }
    public boolean validateTokenAgainstUsername(String username, String token) {
        if (this.containsKey(username)) {
            String associatedToken = this.get(username);
            return token.equals(associatedToken);
        }
        return false;
    }
    public void addUser(String username, String token) {
        this.put(username,token);
    }
    public boolean hasUser(String username) {
        return this.containsKey(username);
    }
    public boolean hasToken(String token) {
        return this.containsValue(token);
    }
    public String getToken(String username) {
        return this.get(username);
    }
    public String getUserByToken(String token) {
       for (Map.Entry<String, String> entry : this.entrySet()) {
           if (Objects.equals(token,entry.getValue())) {
               return entry.getKey();
           }
       }
       return null;
    }

    public void removeUser(String username) {
        this.remove(username);
    }
    public static String generateRandomToken(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(randomIndex));
        }
        return builder.toString();
    }

}
