# Smart Contact Manager (Java + DSA)

<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Playfair+Display&pause=800&color=D7B35E&center=true&vCenter=true&width=700&lines=Smart+Contact+Manager;HashMap+%2B+Trie+%3D+Lightning+Fast+Search;DSA+Project" alt="Typing SVG" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-DSA-1a2260?style=for-the-badge" />
  <img src="https://img.shields.io/badge/HashMap-O(1)-d7b35e?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Trie-O(L)-2c3690?style=for-the-badge" />
  <img src="https://img.shields.io/badge/REST-API-111744?style=for-the-badge" />
</p>

<p align="center">
  <b>Contact Manager</b> with ultra-fast search, clean architecture, and a lightweight Java REST API.
</p>

---

## Live Experience (Local)

1. Start API server (steps below)
2. Open: `http://localhost:8080`
3. live link-https://yogendra630.github.io/SmartContactManager/


---

## Highlights

- `HashMap` for **O(1)** lookup by phone number
- `Trie (Prefix Tree)` for **O(L)** prefix search
- Duplicate names handled efficiently
- CSV persistence (data saved after every write)
- Royal UI theme + responsive layout

---

## Features

- Add, delete, update contacts
- Search by full name (duplicates supported)
- Prefix search with auto-suggestions
- Display all contacts (sorted or unsorted)
- Recent search history (Deque)
- Favorites (PriorityQueue)
- Lightweight REST API + UI

---

## Folder Structure

```
SmartContactManager/
│
├── data/
│   └── contacts.csv
│
├── frontend/
│   ├── index.html
│   ├── styles.css
│   └── app.js
│
├── src/
│   └── com/
│       └── smartcontact/
│           ├── Main.java
│           │
│           ├── model/
│           │   └── Contact.java
│           │
│           ├── ds/
│           │   ├── Trie.java
│           │   └── TrieNode.java
│           │
│           ├── service/
│           │   ├── ContactManager.java
│           │   ├── SearchHistory.java
│           │   └── FavoritesManager.java
│           │
│           ├── persistence/
│           │   └── ContactStore.java
│           │
│           ├── server/
│           │   └── ContactApiServer.java
│           │
│           └── util/
│               └── Validator.java
│
├── README.md
└── .gitignore
```

---

## How to Run (API + Frontend)

1. Open terminal in this folder.
2. Compile:

```bash
javac -d out src/com/smartcontact/Main.java src/com/smartcontact/model/Contact.java src/com/smartcontact/ds/TrieNode.java src/com/smartcontact/ds/Trie.java src/com/smartcontact/service/ContactManager.java src/com/smartcontact/service/SearchHistory.java src/com/smartcontact/service/FavoritesManager.java src/com/smartcontact/persistence/ContactStore.java src/com/smartcontact/server/ContactApiServer.java src/com/smartcontact/util/Validator.java
```

3. Run API server:

```bash
java -cp out com.smartcontact.server.ContactApiServer
```

4. Open the UI in your browser:

```
http://localhost:8080


```

---

## API Endpoints

- `GET /api/contacts` -> list all contacts
- `GET /api/search?prefix=ro` -> prefix search
- `GET /api/search?full=Meera%20Nair` -> full name search
- `POST /api/contact` -> add contact
- `PUT /api/contact?phone=+91...` -> update
- `DELETE /api/contact?phone=+91...` -> delete

---

## Trie Design (Prefix Search)

Each Trie node stores:

- `children` map (next characters)
- `phoneNumbers` set of all contacts that share the node's prefix
- `exactPhones` set for exact-name matches

**Insert:** walk the name and add the phone to `phoneNumbers` in each node.

**Prefix Search:** move to the prefix node in `O(L)` and return `phoneNumbers` from that node.

**Delete:** remove the phone from all prefix sets, clean empty nodes safely.

---

## Complexity

- Add: `O(L)` for Trie + `O(1)` HashMap
- Delete: `O(L)` for Trie + `O(1)` HashMap
- Prefix Search: `O(L)` + output size
- Full Name Search: `O(1)` via HashMap

---

## Sample Test Cases

1. Add a contact
   - Name: Riya Jain
   - Phone: +91 98765 99999
   - Email: riya@example.com
   - Expected: Contact added

2. Duplicate name with different number
   - Add: Rohan Verma, +91 98765 40002
   - Add: Rohan Verma, +91 98765 40003
   - Search full name: Rohan Verma
   - Expected: Both contacts listed

3. Prefix search
   - Prefix: Ro
   - Expected: All names starting with Ro

4. Delete contact
   - Delete: +91 98765 40004
   - Search name: Meera Nair
   - Expected: No results

5. Update phone
   - Update: existing +91 98765 43210 -> new +91 90000 11111
   - Search by prefix Aar
   - Expected: Contact with updated number

---

## Notes

- Data persists to `data/contacts.csv` after every write.
- Designed to be extended into Spring Boot APIs or a full-stack UI.

---

## Next Upgrade Ideas

- REST API layer (Spring Boot)
- Web UI (React or HTML/CSS/JS)
- MySQL storage (JDBC)
- Unit tests (JUnit)
<p>
Contributions are welcome!  
Feel free to fork the repository and submit pull requests.
</p>

<hr>

<h2>📜 License</h2>

<p>
This project is licensed under the <b>MIT License</b>.
</p>

<hr>

<h2>👨‍💻 Author</h2>

<p>
<b>Yogendra Maurya</b><br>

</p>

<hr>

<p align="center">
⭐ If you like this project, please give it a star on GitHub!
</p>
