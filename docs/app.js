const API_BASE = window.API_BASE || "http://localhost:8080";

const resultsEl = document.getElementById("results");
const chipsEl = document.getElementById("chips");
const searchInput = document.getElementById("search-input");
const searchBtn = document.getElementById("search-btn");
const form = document.getElementById("contact-form");
const statusEl = document.getElementById("status");
const statTotal = document.getElementById("stat-total");

async function fetchJson(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || "Request failed");
  }
  return res.json();
}

function renderContacts(contacts) {
  resultsEl.innerHTML = "";
  if (!contacts || contacts.length === 0) {
    resultsEl.innerHTML = `<div class="contact-card"><div class="info"><h3>No results</h3><p>Try a different prefix or name.</p></div></div>`;
    return;
  }

  contacts.forEach((c) => {
    const initials = c.name
      .split(" ")
      .map((p) => p[0])
      .slice(0, 2)
      .join("")
      .toUpperCase();

    const card = document.createElement("div");
    card.className = "contact-card";
    card.innerHTML = `
      <div class="avatar">${initials}</div>
      <div class="info">
        <h3>${c.name}</h3>
        <p>${c.phone}${c.email ? ` ? ${c.email}` : ""}</p>
      </div>
      <button class="icon">?</button>
    `;
    resultsEl.appendChild(card);
  });
}

function setStatus(msg, isError = false) {
  statusEl.textContent = msg;
  statusEl.style.color = isError ? "#ffb3b3" : "#c7d2ff";
}

async function loadAll() {
  try {
    const data = await fetchJson("/api/contacts");
    renderContacts(data);
    statTotal.textContent = data.length;
  } catch (e) {
    renderContacts([]);
  }
}

searchBtn.addEventListener("click", async () => {
  const query = searchInput.value.trim();
  chipsEl.innerHTML = "";
  if (!query) {
    loadAll();
    return;
  }
  chipsEl.innerHTML = `<span>Prefix: "${query}"</span>`;
  try {
    const data = await fetchJson(`/api/search?prefix=${encodeURIComponent(query)}`);
    renderContacts(data);
  } catch (e) {
    renderContacts([]);
  }
});

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("name-input").value.trim();
  const phone = document.getElementById("phone-input").value.trim();
  const email = document.getElementById("email-input").value.trim();

  try {
    await fetchJson("/api/contact", {
      method: "POST",
      body: JSON.stringify({ name, phone, email }),
    });
    setStatus("Contact saved.");
    form.reset();
    loadAll();
  } catch (e) {
    setStatus("Failed to save. Check values or duplicates.", true);
  }
});

const refreshBtn = document.getElementById("refresh");
refreshBtn.addEventListener("click", loadAll);

const resetFormBtn = document.getElementById("reset-form");
resetFormBtn.addEventListener("click", () => {
  form.reset();
  setStatus("");
});

loadAll();
