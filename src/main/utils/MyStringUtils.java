package main.utils;

public class MyStringUtils {
    public static boolean validateUsername(String username) {
        if(username == null || username.equals("") || username.length() > 10)
            return false;
        return true;
    }
}
