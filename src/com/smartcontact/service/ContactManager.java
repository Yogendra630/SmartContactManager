package com.smartcontact.service;

import com.smartcontact.ds.Trie;
import com.smartcontact.model.Contact;
import com.smartcontact.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactManager {
    private final Map<String, Contact> contactsByPhone = new HashMap<>();
    private final Map<String, Set<String>> phonesByNameKey = new HashMap<>();
    private final Trie trie = new Trie();

    public boolean addContact(Contact contact) {
        if (contact == null || !Validator.isValidName(contact.getName()) || !Validator.isValidPhone(contact.getPhoneNumber())) {
            return false;
        }
        if (!Validator.isValidEmail(contact.getEmail())) {
            return false;
        }
        String phone = contact.getPhoneNumber();
        if (contactsByPhone.containsKey(phone)) {
            return false;
        }
        contactsByPhone.put(phone, contact);
        String nameKey = normalizeName(contact.getName());
        phonesByNameKey.computeIfAbsent(nameKey, k -> new HashSet<>()).add(phone);
        trie.insert(nameKey, phone);
        return true;
    }

    public boolean deleteContact(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        Contact removed = contactsByPhone.remove(phoneNumber);
        if (removed == null) {
            return false;
        }
        String nameKey = normalizeName(removed.getName());
        Set<String> phones = phonesByNameKey.get(nameKey);
        if (phones != null) {
            phones.remove(phoneNumber);
            if (phones.isEmpty()) {
                phonesByNameKey.remove(nameKey);
            }
        }
        trie.delete(nameKey, phoneNumber);
        return true;
    }

    public boolean updateContact(String existingPhone, String newName, String newEmail, String newPhone) {
        Contact existing = contactsByPhone.get(existingPhone);
        if (existing == null) {
            return false;
        }

        String finalName = (newName == null || newName.isBlank()) ? existing.getName() : newName;
        String finalEmail = (newEmail == null) ? existing.getEmail() : newEmail;
        String finalPhone = (newPhone == null || newPhone.isBlank()) ? existing.getPhoneNumber() : newPhone;

        if (!Validator.isValidName(finalName) || !Validator.isValidPhone(finalPhone) || !Validator.isValidEmail(finalEmail)) {
            return false;
        }

        if (!existingPhone.equals(finalPhone) && contactsByPhone.containsKey(finalPhone)) {
            return false;
        }

        if (!existingPhone.equals(finalPhone)) {
            deleteContact(existingPhone);
            return addContact(new Contact(finalName, finalPhone, finalEmail));
        }

        String oldNameKey = normalizeName(existing.getName());
        String newNameKey = normalizeName(finalName);
        if (!oldNameKey.equals(newNameKey)) {
            Set<String> oldSet = phonesByNameKey.get(oldNameKey);
            if (oldSet != null) {
                oldSet.remove(existingPhone);
                if (oldSet.isEmpty()) {
                    phonesByNameKey.remove(oldNameKey);
                }
            }
            phonesByNameKey.computeIfAbsent(newNameKey, k -> new HashSet<>()).add(existingPhone);
            trie.delete(oldNameKey, existingPhone);
            trie.insert(newNameKey, existingPhone);
        }

        existing.setName(finalName);
        existing.setEmail(finalEmail);
        return true;
    }

    public Contact searchByPhone(String phoneNumber) {
        return contactsByPhone.get(phoneNumber);
    }

    public List<Contact> searchByFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return List.of();
        }
        String key = normalizeName(fullName);
        Set<String> phones = phonesByNameKey.getOrDefault(key, Set.of());
        List<Contact> result = new ArrayList<>();
        for (String phone : phones) {
            Contact contact = contactsByPhone.get(phone);
            if (contact != null) {
                result.add(contact);
            }
        }
        result.sort(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Contact::getPhoneNumber));
        return result;
    }

    public List<Contact> searchByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        String key = normalizeName(prefix);
        List<String> phones = trie.searchByPrefix(key);
        List<Contact> result = new ArrayList<>();
        for (String phone : phones) {
            Contact contact = contactsByPhone.get(phone);
            if (contact != null) {
                result.add(contact);
            }
        }
        result.sort(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Contact::getPhoneNumber));
        return result;
    }

    public List<Contact> getAllContacts() {
        return new ArrayList<>(contactsByPhone.values());
    }

    public List<Contact> getAllContactsSorted() {
        List<Contact> list = getAllContacts();
        list.sort(Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Contact::getPhoneNumber));
        return list;
    }

    public Map<String, Contact> getContactsByPhone() {
        return contactsByPhone;
    }

    public int size() {
        return contactsByPhone.size();
    }

    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}

