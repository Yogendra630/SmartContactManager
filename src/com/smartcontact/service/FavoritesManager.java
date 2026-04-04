package com.smartcontact.service;

import com.smartcontact.model.Contact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class FavoritesManager {
    private final Set<String> favoritePhones = new HashSet<>();

    public boolean addFavorite(String phoneNumber, Map<String, Contact> contactsByPhone) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        if (!contactsByPhone.containsKey(phoneNumber)) {
            return false;
        }
        return favoritePhones.add(phoneNumber);
    }

    public boolean removeFavorite(String phoneNumber) {
        return favoritePhones.remove(phoneNumber);
    }

    public List<Contact> getFavoritesSorted(Map<String, Contact> contactsByPhone) {
        PriorityQueue<Contact> pq = new PriorityQueue<>(Comparator
                .comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Contact::getPhoneNumber));

        for (String phone : favoritePhones) {
            Contact contact = contactsByPhone.get(phone);
            if (contact != null) {
                pq.add(contact);
            }
        }

        List<Contact> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll());
        }
        return result;
    }

    public boolean isFavorite(String phoneNumber) {
        return favoritePhones.contains(phoneNumber);
    }
}

