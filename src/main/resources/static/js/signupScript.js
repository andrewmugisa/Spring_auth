// signupScript.js
// Handles signup page (signup.html) including inline verify and resend

document.addEventListener("DOMContentLoaded", () => {
  // Redirect to home if already logged in
  Auth.redirectIfAuthenticated();

  const formContainer = document.getElementById("signupFormContainer");
  const signupButton = document.getElementById("signupButton");
  const loginRedirectButton = document.getElementById("loginRedirectButton");

  let pendingEmail = "";
  let pendingPassword = "";

  // ── Signup ─────────────────────────────────────────────────────────────────
  signupButton.addEventListener("click", async () => {
    const name = document.getElementById("nameInput").value.trim();
    const email = document.getElementById("emailInput").value.trim();
    const password = document.getElementById("passwordInput").value.trim();

    if (!name || !email || !password) {
      alert("All fields are required.");
      return;
    }

    if (password.length < 6) {
      alert("Password must be at least 6 characters.");
      return;
    }

    signupButton.disabled = true;
    signupButton.textContent = "Signing up...";

    try {
      await API.signup({ name, email, password });
      pendingEmail = email;
      pendingPassword = password;
      showVerifyStep(email);
    } catch (error) {
      alert("Signup failed:\n" + error.message);
      console.error("Signup error:", error);
    } finally {
      signupButton.disabled = false;
      signupButton.textContent = "Sign Up";
    }
  });

  // ── Redirect back to login ─────────────────────────────────────────────────
  loginRedirectButton.addEventListener("click", () => {
    window.location.href = "index.html";
  });

  // ── Verify step (replaces form after signup) ───────────────────────────────
  function showVerifyStep(email) {
    formContainer.innerHTML = `
      <h2>Verify Your Email</h2>
      <p>A verification code was sent to <strong>${email}</strong>.</p>
      <div class="form-group">
        <label for="verificationCode">Verification Code:</label>
        <input type="text" id="verificationCode" placeholder="Enter 6-digit code" maxlength="6" />
      </div>
      <div id="buttons">
        <button type="button" id="verifyButton">Verify</button>
        <button type="button" id="resendButton">Resend Code</button>
        <button type="button" id="backToLoginButton">Back to Login</button>
      </div>
      <p id="verifyMessage" style="color: red; display: none;"></p>
    `;

    document
      .getElementById("verifyButton")
      .addEventListener("click", handleVerify);
    document
      .getElementById("resendButton")
      .addEventListener("click", handleResend);
    document
      .getElementById("backToLoginButton")
      .addEventListener("click", () => {
        window.location.href = "index.html";
      });
  }

  async function handleVerify() {
    const code = document.getElementById("verificationCode").value.trim();
    const verifyButton = document.getElementById("verifyButton");

    if (!code) {
      showVerifyMessage("Verification code is required.", "red");
      return;
    }

    verifyButton.disabled = true;
    verifyButton.textContent = "Verifying...";

    try {
      await API.verify({ email: pendingEmail, verificationCode: code });
      showVerifyMessage("Verified! Logging you in...", "green");

      // Auto-login after verification
      const data = await API.login({
        email: pendingEmail,
        password: pendingPassword,
      });
      Auth.saveToken(data.token, data.expiresIn);
      setTimeout(() => {
        window.location.href = "home.html";
      }, 1000);
    } catch (error) {
      showVerifyMessage("Failed: " + error.message, "red");
      console.error("Verify error:", error);
      verifyButton.disabled = false;
      verifyButton.textContent = "Verify";
    }
  }

  async function handleResend() {
    const resendButton = document.getElementById("resendButton");

    resendButton.disabled = true;
    resendButton.textContent = "Sending...";

    try {
      await API.resend(pendingEmail);
      showVerifyMessage("Code resent successfully. Check your email.", "green");
    } catch (error) {
      showVerifyMessage("Resend failed: " + error.message, "red");
      console.error("Resend error:", error);
    } finally {
      resendButton.disabled = false;
      resendButton.textContent = "Resend Code";
    }
  }

  function showVerifyMessage(text, color) {
    const el = document.getElementById("verifyMessage");
    if (!el) return;
    el.textContent = text;
    el.style.color = color;
    el.style.display = "block";
  }
});
