// app.js
// Central API layer + shared auth utilities

const API_BASE = "http://localhost:8080";

// ─── Token helpers ────────────────────────────────────────────────────────────

const Auth = {
  saveToken(token, expiresIn) {
    localStorage.setItem("token", token);
    localStorage.setItem("tokenExpiry", Date.now() + expiresIn);
  },

  getToken() {
    return localStorage.getItem("token");
  },

  isExpired() {
    const expiry = localStorage.getItem("tokenExpiry");
    if (!expiry) return true;
    return Date.now() > parseInt(expiry, 10);
  },

  isAuthenticated() {
    return !!this.getToken() && !this.isExpired();
  },

  clear() {
    localStorage.removeItem("token");
    localStorage.removeItem("tokenExpiry");
  },

  // Call on any protected page — redirects to login if not authenticated
  requireAuth() {
    if (!this.isAuthenticated()) {
      this.clear();
      window.location.href = "index.html";
    }
  },

  // Call on login/signup pages — redirects to home if already logged in
  redirectIfAuthenticated() {
    if (this.isAuthenticated()) {
      window.location.href = "home.html";
    }
  },
};

// ─── API helpers ──────────────────────────────────────────────────────────────

async function apiFetch(path, options = {}) {
  const resp = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!resp.ok) {
    let message;
    try {
      const err = await resp.json();
      // GlobalExceptionHandler returns { message, details }
      if (err.details) {
        // Validation errors — join field messages
        message = Object.entries(err.details)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join("\n");
      } else {
        message = err.message || "An unexpected error occurred";
      }
    } catch {
      message = (await resp.text()) || "An unexpected error occurred";
    }
    throw new Error(message);
  }

  // 200 with JSON body
  const contentType = resp.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    return resp.json();
  }
  return null;
}

async function apiFetchAuth(path, options = {}) {
  const token = Auth.getToken();
  return apiFetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
      ...(options.headers || {}),
    },
  });
}

// ─── API calls ────────────────────────────────────────────────────────────────

const API = {
  signup({ username, name, email, password }) {
    return apiFetch("/auth/signup", {
      method: "POST",
      body: JSON.stringify({ username, name, email, password }),
    });
  },

  login({ email, password }) {
    return apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  verify({ email, verificationCode }) {
    return apiFetch("/auth/verify", {
      method: "POST",
      body: JSON.stringify({ email, verificationCode }),
    });
  },

  resend(email) {
    return apiFetch("/auth/resend", {
      method: "POST",
      body: JSON.stringify({ email }),
    });
  },

  getMe() {
    return apiFetchAuth("/users/me", { method: "GET" });
  },
};
