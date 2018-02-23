package main.utils;

public class MyStringUtils {
    public static final String USERNAME_PATTERN = "[a-zA-Z0-9]{1,10}";

    public static boolean validateUsername(String username) {
        return username != null && username.matches(USERNAME_PATTERN);
    }
}
