package com.smartcontact.persistence;

import com.smartcontact.model.Contact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ContactStore {
    private static final String HEADER = "name,phone,email";

    public List<Contact> load(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (i == 0 && line.equalsIgnoreCase(HEADER)) {
                continue;
            }
            List<String> parts = parseCsvLine(line);
            String name = parts.size() > 0 ? parts.get(0) : "";
            String phone = parts.size() > 1 ? parts.get(1) : "";
            String email = parts.size() > 2 ? parts.get(2) : "";
            contacts.add(new Contact(name, phone, email));
        }
        return contacts;
    }

    public void save(Path path, List<Contact> contacts) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Contact contact : contacts) {
            String line = String.join(",",
                    escape(contact.getName()),
                    escape(contact.getPhoneNumber()),
                    escape(contact.getEmail() == null ? "" : contact.getEmail()));
            lines.add(line);
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }
}

