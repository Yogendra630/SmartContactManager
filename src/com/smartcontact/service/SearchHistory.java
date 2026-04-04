package com.smartcontact.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SearchHistory {
    private final Deque<String> history = new ArrayDeque<>();
    private final int maxSize;

    public SearchHistory(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
    }

    public void addQuery(String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        if (history.size() == maxSize) {
            history.removeLast();
        }
        history.addFirst(query);
    }

    public List<String> getRecent() {
        return new ArrayList<>(history);
    }
}

