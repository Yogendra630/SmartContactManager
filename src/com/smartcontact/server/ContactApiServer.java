package com.smartcontact.server;

import com.smartcontact.model.Contact;
import com.smartcontact.persistence.ContactStore;
import com.smartcontact.service.ContactManager;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactApiServer {
    private static final int PORT = 8080;
    private static final Path DATA_PATH = Path.of("data", "contacts.csv");
    private static final Path FRONTEND_DIR = Path.of("frontend");

    private final ContactManager manager = new ContactManager();
    private final ContactStore store = new ContactStore();

    public static void main(String[] args) throws Exception {
        new ContactApiServer().start();
    }

    private void start() throws Exception {
        try {
            for (Contact contact : store.load(DATA_PATH)) {
                manager.addContact(contact);
            }
        } catch (IOException e) {
            System.out.println("Could not load contacts: " + e.getMessage());
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/contacts", new ContactsHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/contact", new ContactHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Smart Contact API running on http://localhost:" + PORT);
        System.out.println("Open http://localhost:" + PORT + " in your browser.");
    }

    private class ContactsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, jsonError("Method not allowed"));
                return;
            }
            List<Contact> contacts = manager.getAllContactsSorted();
            send(exchange, 200, contactsToJson(contacts));
        }
    }

    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, jsonError("Method not allowed"));
                return;
            }
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String prefix = params.getOrDefault("prefix", "");
            String full = params.getOrDefault("full", "");

            List<Contact> results;
            if (!full.isBlank()) {
                results = manager.searchByFullName(full);
            } else if (!prefix.isBlank()) {
                results = manager.searchByPrefix(prefix);
            } else {
                results = List.of();
            }
            send(exchange, 200, contactsToJson(results));
        }
    }

    private class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;
            String method = exchange.getRequestMethod().toUpperCase();
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String phone = params.getOrDefault("phone", "");

            switch (method) {
                case "GET" -> {
                    if (phone.isBlank()) {
                        send(exchange, 400, jsonError("Phone is required"));
                        return;
                    }
                    Contact contact = manager.searchByPhone(phone);
                    if (contact == null) {
                        send(exchange, 404, jsonError("Not found"));
                        return;
                    }
                    send(exchange, 200, contactToJson(contact));
                }
                case "POST" -> {
                    String body = readBody(exchange);
                    Map<String, String> data = parseJson(body);
                    Contact contact = new Contact(
                            data.getOrDefault("name", ""),
                            data.getOrDefault("phone", ""),
                            data.getOrDefault("email", "")
                    );
                    boolean added = manager.addContact(contact);
                    if (!added) {
                        send(exchange, 400, jsonError("Invalid or duplicate contact"));
                        return;
                    }
                    persist();
                    send(exchange, 201, contactToJson(contact));
                }
                case "PUT" -> {
                    if (phone.isBlank()) {
                        send(exchange, 400, jsonError("Phone is required"));
                        return;
                    }
                    String body = readBody(exchange);
                    Map<String, String> data = parseJson(body);
                    boolean updated = manager.updateContact(
                            phone,
                            data.get("name"),
                            data.get("email"),
                            data.get("phone")
                    );
                    if (!updated) {
                        send(exchange, 400, jsonError("Update failed"));
                        return;
                    }
                    persist();
                    Contact updatedContact = manager.searchByPhone(data.getOrDefault("phone", phone));
                    send(exchange, 200, contactToJson(updatedContact));
                }
                case "DELETE" -> {
                    if (phone.isBlank()) {
                        send(exchange, 400, jsonError("Phone is required"));
                        return;
                    }
                    boolean deleted = manager.deleteContact(phone);
                    if (!deleted) {
                        send(exchange, 404, jsonError("Not found"));
                        return;
                    }
                    persist();
                    send(exchange, 200, "{\"status\":\"deleted\"}");
                }
                default -> send(exchange, 405, jsonError("Method not allowed"));
            }
        }
    }

    private class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            Path file = FRONTEND_DIR.resolve(path.substring(1));
            if (!Files.exists(file)) {
                send(exchange, 404, "Not found", "text/plain");
                return;
            }
            String contentType = guessContentType(file);
            byte[] bytes = Files.readAllBytes(file);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            headers.set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void persist() {
        try {
            store.save(DATA_PATH, manager.getAllContactsSorted());
        } catch (IOException e) {
            System.out.println("Failed to save contacts: " + e.getMessage());
        }
    }

    private boolean handlePreflight(HttpExchange exchange) throws IOException {
        if (!"OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            return false;
        }
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
        return true;
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        send(exchange, status, body, "application/json");
    }

    private void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType + "; charset=UTF-8");
        headers.set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx >= 0) {
                String key = decode(pair.substring(0, idx));
                String value = decode(pair.substring(idx + 1));
                params.put(key, value);
            }
        }
        return params;
    }

    private String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String jsonError(String message) {
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    private String contactsToJson(List<Contact> contacts) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < contacts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(contactToJson(contacts.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String contactToJson(Contact c) {
        if (c == null) {
            return "null";
        }
        return "{\"name\":\"" + escapeJson(c.getName()) +
                "\",\"phone\":\"" + escapeJson(c.getPhoneNumber()) +
                "\",\"email\":\"" + escapeJson(c.getEmail() == null ? "" : c.getEmail()) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) {
            return map;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] pairs = trimmed.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            String key = stripQuotes(kv[0].trim());
            String val = stripQuotes(kv[1].trim());
            map.put(key, val);
        }
        return map;
    }

    private String stripQuotes(String value) {
        String v = value;
        if (v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        return v.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }

    private String guessContentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".html")) return "text/html";
        return "text/plain";
    }
}
