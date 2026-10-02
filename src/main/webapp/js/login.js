const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const message = document.getElementById("message");

// Tabs: show one form, hide the other
document.querySelectorAll(".tab").forEach(tab => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach(t => t.classList.toggle("active", t === tab));
    loginForm.classList.toggle("hidden", tab.dataset.tab !== "login");
    registerForm.classList.toggle("hidden", tab.dataset.tab !== "register");
    message.innerText = "";
  });
});

// Send a form to the server with POST
async function submitForm(form, url) {
  const response = await fetch(url, {
    method: "POST",
    body: new URLSearchParams(new FormData(form))   // → email=a%40b.com&password=... (like a normal HTML form)
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error);                     // the message from send(resp, 401, error("..."))
  }
  return data;
}

function handle(form, url) {
  form.addEventListener("submit", async event => {
    event.preventDefault();                          // stop the browser's normal page reload
    message.innerText = "";
    try {
      await submitForm(form, url);
      location.href = "dashboard.html";              // logged in → go to the dashboard
    } catch (err) {
      message.innerText = err.message;
    }
  });
}

handle(loginForm, "api/auth/login");
handle(registerForm, "api/auth/register");