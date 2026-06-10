// homeScript.js
// Handles home page (home.html)

document.addEventListener("DOMContentLoaded", async () => {
  // Guard — kick out if not authenticated
  Auth.requireAuth();

  const userProfile = document.getElementById("userProfile");
  const logoutButton = document.getElementById("logoucleartButton");

  // ── Load current user ──────────────────────────────────────────────────────
  try {
    const user = await API.getMe();
    userProfile.innerHTML = `
      <p><strong>Name:</strong> ${user.name ?? "—"}</p>
      <p><strong>Username:</strong> ${user.username}</p>
      <p><strong>Email:</strong> ${user.email}</p>
      <p><strong>Member since:</strong> ${new Date(user.createdAt).toLocaleDateString()}</p>
    `;
  } catch (error) {
    console.error("Failed to load user:", error);
    // Token likely expired or invalid
    Auth.clear();
    window.location.href = "index.html";
  }

  // ── Logout ─────────────────────────────────────────────────────────────────
  logoutButton.addEventListener("click", () => {
    Auth.clear();
    window.location.href = "index.html";
  });
});
