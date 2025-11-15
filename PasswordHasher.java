package com.slginventory.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hash(String passwordPlain) {
        return BCrypt.hashpw(passwordPlain, BCrypt.gensalt(12));
    }

    public static boolean verify(String passwordPlain, String hash) {
        return BCrypt.checkpw(passwordPlain, hash);
    }
}
