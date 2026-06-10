// script.js
// Handles login page (index.html)

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to home if already logged in
  Auth.redirectIfAuthenticated();

  // ── Login ──────────────────────────────────────────────────────────────────
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const email = document.getElementById("loginEmail").value.trim();
      const password = document.getElementById("loginPassword").value.trim();

      const loginButton = document.getElementById("loginButton");
      loginButton.disabled = true;
      loginButton.textContent = "Logging in...";

      try {
        const data = await API.login({ email, password });
        Auth.saveToken(data.token, data.expiresIn);
        window.location.href = "home.html";
      } catch (error) {
        alert("Login failed:\n" + error.message);
        console.error("Login error:", error);
      } finally {
        loginButton.disabled = false;
        loginButton.textContent = "Login";
      }
    });
  }

  // ── Register redirect ──────────────────────────────────────────────────────
  const registerButton = document.getElementById("registerButton");
  if (registerButton) {
    registerButton.addEventListener("click", () => {
      window.location.href = "signup.html";
    });
  }
});
