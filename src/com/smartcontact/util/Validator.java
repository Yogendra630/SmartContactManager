package com.smartcontact.util;

public class Validator {
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[0-9+][0-9\\- ]{5,15}");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}

