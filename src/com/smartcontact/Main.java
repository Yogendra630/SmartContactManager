package com.smartcontact;

import com.smartcontact.model.Contact;
import com.smartcontact.persistence.ContactStore;
import com.smartcontact.service.ContactManager;
import com.smartcontact.service.FavoritesManager;
import com.smartcontact.service.SearchHistory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Path DATA_PATH = Path.of("data", "contacts.csv");

    public static void main(String[] args) {
        ContactManager manager = new ContactManager();
        FavoritesManager favorites = new FavoritesManager();
        SearchHistory history = new SearchHistory(10);
        ContactStore store = new ContactStore();

        try {
            for (Contact contact : store.load(DATA_PATH)) {
                manager.addContact(contact);
            }
        } catch (IOException e) {
            System.out.println("Could not load contacts: " + e.getMessage());
        }

        if (manager.size() == 0) {
            seedData(manager);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addContact(manager, scanner);
                case "2" -> deleteContact(manager, scanner, favorites);
                case "3" -> updateContact(manager, scanner);
                case "4" -> searchFullName(manager, scanner, history);
                case "5" -> searchPrefix(manager, scanner, history);
                case "6" -> displayAll(manager);
                case "7" -> displaySorted(manager);
                case "8" -> manageFavorites(manager, favorites, scanner);
                case "9" -> showHistory(history);
                case "0" -> {
                    running = false;
                    saveData(manager, store);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n=== Smart Contact Manager ===");
        System.out.println("1. Add Contact");
        System.out.println("2. Delete Contact");
        System.out.println("3. Update Contact");
        System.out.println("4. Search by Full Name");
        System.out.println("5. Search by Prefix (Autocomplete)");
        System.out.println("6. Display All Contacts");
        System.out.println("7. Display All Contacts (Sorted)");
        System.out.println("8. Favorites");
        System.out.println("9. Recent Searches");
        System.out.println("0. Save & Exit");
        System.out.print("Choose an option: ");
    }

    private static void addContact(ContactManager manager, Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Email (optional): ");
        String email = scanner.nextLine();

        boolean added = manager.addContact(new Contact(name, phone, email));
        System.out.println(added ? "Contact added." : "Failed to add contact (duplicate or invalid)." );
    }

    private static void deleteContact(ContactManager manager, Scanner scanner, FavoritesManager favorites) {
        System.out.print("Phone of contact to delete: ");
        String phone = scanner.nextLine();
        boolean removed = manager.deleteContact(phone);
        if (removed) {
            favorites.removeFavorite(phone);
        }
        System.out.println(removed ? "Contact deleted." : "Contact not found.");
    }

    private static void updateContact(ContactManager manager, Scanner scanner) {
        System.out.print("Existing phone number: ");
        String phone = scanner.nextLine();
        System.out.print("New name (leave blank to keep): ");
        String newName = scanner.nextLine();
        System.out.print("New phone (leave blank to keep): ");
        String newPhone = scanner.nextLine();
        System.out.print("New email (leave blank to keep): ");
        String newEmail = scanner.nextLine();

        boolean updated = manager.updateContact(phone,
                newName.isBlank() ? null : newName,
                newEmail.isBlank() ? null : newEmail,
                newPhone.isBlank() ? null : newPhone);
        System.out.println(updated ? "Contact updated." : "Update failed.");
    }

    private static void searchFullName(ContactManager manager, Scanner scanner, SearchHistory history) {
        System.out.print("Full name to search: ");
        String name = scanner.nextLine();
        history.addQuery("FULL: " + name);
        List<Contact> results = manager.searchByFullName(name);
        printResults(results);
    }

    private static void searchPrefix(ContactManager manager, Scanner scanner, SearchHistory history) {
        System.out.print("Prefix to search: ");
        String prefix = scanner.nextLine();
        history.addQuery("PREFIX: " + prefix);
        List<Contact> results = manager.searchByPrefix(prefix);
        printResults(results);
    }

    private static void displayAll(ContactManager manager) {
        printResults(manager.getAllContacts());
    }

    private static void displaySorted(ContactManager manager) {
        printResults(manager.getAllContactsSorted());
    }

    private static void manageFavorites(ContactManager manager, FavoritesManager favorites, Scanner scanner) {
        System.out.println("\nFavorites Menu");
        System.out.println("1. Add Favorite");
        System.out.println("2. Remove Favorite");
        System.out.println("3. View Favorites");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> {
                System.out.print("Phone to favorite: ");
                String phone = scanner.nextLine();
                boolean added = favorites.addFavorite(phone, manager.getContactsByPhone());
                System.out.println(added ? "Added to favorites." : "Could not add favorite.");
            }
            case "2" -> {
                System.out.print("Phone to remove from favorites: ");
                String phone = scanner.nextLine();
                boolean removed = favorites.removeFavorite(phone);
                System.out.println(removed ? "Removed from favorites." : "Not a favorite.");
            }
            case "3" -> {
                List<Contact> favs = favorites.getFavoritesSorted(manager.getContactsByPhone());
                printResults(favs);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void showHistory(SearchHistory history) {
        List<String> recent = history.getRecent();
        if (recent.isEmpty()) {
            System.out.println("No recent searches.");
            return;
        }
        System.out.println("Recent Searches:");
        for (String q : recent) {
            System.out.println("- " + q);
        }
    }

    private static void printResults(List<Contact> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No contacts found.");
            return;
        }
        System.out.println("\nResults:");
        for (Contact c : results) {
            System.out.println("- " + c);
        }
    }

    private static void saveData(ContactManager manager, ContactStore store) {
        try {
            store.save(DATA_PATH, manager.getAllContactsSorted());
            System.out.println("Contacts saved to " + DATA_PATH);
        } catch (IOException e) {
            System.out.println("Failed to save contacts: " + e.getMessage());
        }
    }

    private static void seedData(ContactManager manager) {
        manager.addContact(new Contact("Aarav Sharma", "+91 98765 43210", "aarav@example.com"));
        manager.addContact(new Contact("Aanya Singh", "+91 98765 40001", "aanya@example.com"));
        manager.addContact(new Contact("Rohan Verma", "+91 98765 40002", "rohan@example.com"));
        manager.addContact(new Contact("Rohan Verma", "+91 98765 40003", "rohan.alt@example.com"));
        manager.addContact(new Contact("Meera Nair", "+91 98765 40004", "meera@example.com"));
    }
}

