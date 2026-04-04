package com.smartcontact.ds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TrieNode {
    final Map<Character, TrieNode> children = new HashMap<>();
    final Set<String> phoneNumbers = new HashSet<>();
    final Set<String> exactPhones = new HashSet<>();
}

