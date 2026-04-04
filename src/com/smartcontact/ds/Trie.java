package com.smartcontact.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String key, String phoneNumber) {
        if (key == null || key.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }
        TrieNode current = root;
        for (char ch : key.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
            current.phoneNumbers.add(phoneNumber);
        }
        current.exactPhones.add(phoneNumber);
    }

    public void delete(String key, String phoneNumber) {
        if (key == null || key.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }
        deleteRecursive(root, key, 0, phoneNumber);
    }

    private boolean deleteRecursive(TrieNode node, String key, int index, String phoneNumber) {
        if (index == key.length()) {
            node.exactPhones.remove(phoneNumber);
        } else {
            char ch = key.charAt(index);
            TrieNode child = node.children.get(ch);
            if (child == null) {
                return false;
            }
            boolean shouldDeleteChild = deleteRecursive(child, key, index + 1, phoneNumber);
            if (shouldDeleteChild) {
                node.children.remove(ch);
            }
        }

        if (index > 0) {
            node.phoneNumbers.remove(phoneNumber);
        }

        boolean noContacts = node.phoneNumbers.isEmpty() && node.exactPhones.isEmpty();
        boolean noChildren = node.children.isEmpty();
        return noContacts && noChildren;
    }

    public List<String> searchByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }
        TrieNode current = root;
        for (char ch : prefix.toCharArray()) {
            TrieNode child = current.children.get(ch);
            if (child == null) {
                return Collections.emptyList();
            }
            current = child;
        }
        return new ArrayList<>(current.phoneNumbers);
    }

    public List<String> searchExact(String key) {
        if (key == null || key.isBlank()) {
            return Collections.emptyList();
        }
        TrieNode current = root;
        for (char ch : key.toCharArray()) {
            TrieNode child = current.children.get(ch);
            if (child == null) {
                return Collections.emptyList();
            }
            current = child;
        }
        return new ArrayList<>(current.exactPhones);
    }
}

