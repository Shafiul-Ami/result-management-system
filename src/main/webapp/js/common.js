// =====================================================================
// common.js — shared by every PUBLIC page (index, notices, result, login).
// Load it BEFORE the page's own script:
//   <script src="js/common.js"></script>
//   <script src="js/notices.js"></script>
// =====================================================================

// ---------- Helpers ----------
function esc(text) {
  return String(text ?? "")
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function fmtDate(iso) {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d).toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}

// ---------- Notice card (used by the home page and notices.html) ----------
function noticeBadge(n) {
  if (n.resultDate === null) return '<span class="badge badge-muted">General</span>';
  if (n.released) return '<span class="badge badge-success">Result Released</span>';
  return `<span class="badge badge-warning">Result on ${fmtDate(n.resultDate)}</span>`;
}

function noticeCard(n) {
  return `
    <article class="notice ${n.resultDate ? "result" : ""}">
      <div class="notice-head">
        <h3>${esc(n.title)}</h3>
        ${noticeBadge(n)}
      </div>
      <p>${esc(n.message)}</p>
      <div class="notice-meta">
        <span>Posted ${fmtDate(n.createdAt)}</span>
        ${n.resultDate ? `<span>Class: ${esc(n.className || "All classes")}</span>` : ""}
      </div>
    </article>`;
}

// ---------- Navbar: written ONCE, shown on every page ----------
const NAV_LINKS = [
  { href: "index.html", text: "Home" },
  { href: "notices.html", text: "Notices" },
  { href: "result.html", text: "Check Result" }
];

function renderNavbar() {
  const box = document.getElementById("navbar");
  if (!box) return;                                         // page has no navbar placeholder

  // Which page are we on? ".../ResultManagementSystem/notices.html" → "notices.html"
  const page = location.pathname.split("/").pop() || "index.html";

  box.innerHTML = `
    <header class="navbar no-print">
      <div class="container nav-inner">
        <a href="index.html" class="brand"><span class="brand-logo">E</span>Edu<b>Result</b></a>
        <button class="nav-toggle" aria-label="Open menu"><span></span><span></span><span></span></button>
        <nav class="nav-links">
          ${NAV_LINKS.map(link =>
            `<a href="${link.href}" class="${link.href === page ? "active" : ""}">${link.text}</a>`
          ).join("")}
          <a href="login.html" class="btn btn-primary btn-sm">Teacher Login</a>
        </nav>
      </div>
    </header>`;

  // Mobile: the ☰ button opens/closes the menu
  const toggle = box.querySelector(".nav-toggle");
  const links = box.querySelector(".nav-links");
  toggle.addEventListener("click", () => links.classList.toggle("open"));
}

// ---------- Footer ----------
function renderFooter() {
  const box = document.getElementById("footer");
  if (!box) return;
  box.innerHTML = `
    <footer class="footer no-print">
      <div class="container">
        <span>&copy; ${new Date().getFullYear()} EduResult · College Result Management</span>
        <span>Built with HTML, CSS, JavaScript &amp; Java Servlets</span>
      </div>
    </footer>`;
}

renderNavbar();
renderFooter();