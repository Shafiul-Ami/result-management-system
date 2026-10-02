// ===== Settings (★ NEW) — must match ResultCalc.java =====
const SUBJECTS = ["Mathematics", "Physics", "Chemistry", "English", "Computer Science"];
const PASS_MARKS = 40;

// ===== Elements =====
const nameSpan = document.getElementById("teacher-name");
const logoutBtn = document.getElementById("logout");
const form = document.getElementById("student-form");
const formTitle = document.getElementById("form-title");
const submitBtn = document.getElementById("submit-btn");
const cancelBtn = document.getElementById("cancel-btn");
const formMessage = document.getElementById("form-message");
const rows = document.getElementById("student-rows");
// ★ NEW
const marksForm = document.getElementById("marks-form");
const marksSelect = document.getElementById("marks-student");
const marksInputs = document.getElementById("marks-inputs");
const marksMessage = document.getElementById("marks-message");

let students = [];

// ===== Helpers =====
function esc(text) {
  return String(text ?? "")
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function fmtDate(iso) {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d).toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}

function showMessage(el, text, isError = false) {        // ★ now works for both forms
  el.style.color = isError ? "red" : "green";
  el.innerText = text;
}

function statusBadge(status) {                              // ★ NEW
  return status === "PASS"
    ? '<span class="badge badge-success">PASS</span>'
    : '<span class="badge badge-danger">FAIL</span>';
}

async function api(url, options = {}) {
  const response = await fetch(url, options);
  let data;
  try {
    data = await response.json();
  } catch {
    // Not JSON (e.g. Tomcat's HTML 404 page when a servlet is missing) → clear message instead of a parse error
    throw new Error(`Server error ${response.status} for ${url}`);
  }
  if (response.status === 401) {
    location.href = "login.html";
    throw new Error("Please log in again.");
  }
  if (!response.ok) {
    throw new Error(data.error);
  }
  return data;
}

// ===== READ =====
async function loadStudents() {
  students = await api("api/teacher/students");
  renderTable();
  renderMarksSelect();        
  renderClassList();                             // ★ keep the dropdown up to date
}

function renderTable() {
  if (students.length === 0) {
    rows.innerHTML = '<tr><td colspan="6" class="empty">No students yet. Add one above.</td></tr>';
    return;
  }
  rows.innerHTML = students.map(s => `
    <tr>
      <td><strong>${esc(s.rollNo)}</strong></td>
      <td>${esc(s.name)}</td>
      <td>${esc(s.className)}</td>
      <td>${fmtDate(s.dob)}</td>
      <td>${s.marks
            ? `${s.total}/${s.maxTotal} · ${s.percentage}% · ${esc(s.grade)} ${statusBadge(s.status)}`
            : '<span class="badge badge-warning">Marks pending</span>'}</td>
      <td class="actions">
        <button class="btn btn-outline btn-sm" data-marks="${s.id}">Marks</button>
        <button class="btn btn-ghost btn-sm" data-edit="${s.id}">Edit</button>
        <button class="btn btn-danger btn-sm" data-delete="${s.id}">Delete</button>
      </td>
    </tr>`).join("");
}

// ===== CREATE / UPDATE student =====
form.addEventListener("submit", async event => {
  event.preventDefault();
  try {
    const data = await api("api/teacher/students", {
      method: "POST",
      body: new URLSearchParams(new FormData(form))
    });
    showMessage(formMessage, data.message);
    resetForm();
    await loadStudents();
  } catch (err) {
    showMessage(formMessage, err.message, true);
  }
});

function startEdit(id) {
  const s = students.find(student => student.id === id);
  form.elements.id.value = s.id;
  form.elements.rollNo.value = s.rollNo;
  form.elements.name.value = s.name;
  form.elements.className.value = s.className;
  form.elements.dob.value = s.dob;
  formTitle.innerText = "Edit Student – " + s.name;
  submitBtn.innerText = "Update Student";
  cancelBtn.classList.remove("hidden");
  formMessage.innerText = "";
  form.scrollIntoView({ behavior: "smooth" });
}

function resetForm() {
  form.reset();
  form.elements.id.value = "";
  formTitle.innerText = "Add New Student";
  submitBtn.innerText = "Add Student";
  cancelBtn.classList.add("hidden");
}

cancelBtn.addEventListener("click", () => {
  resetForm();
  formMessage.innerText = "";
});

// ===== DELETE student =====
async function deleteStudent(id) {
  const s = students.find(student => student.id === id);
  if (!confirm(`Delete ${s.name} (${s.rollNo})? Their marks will also be deleted.`)) return;
  try {
    const data = await api("api/teacher/students?id=" + id, { method: "DELETE" });
    showMessage(formMessage, data.message);
    await loadStudents();
  } catch (err) {
    showMessage(formMessage, err.message, true);
  }
}

// =====================================================================
// ★ NEW: MARKS
// =====================================================================

// 1. Create the 5 number inputs from the SUBJECTS array (no copy-pasting HTML 5 times)
marksInputs.innerHTML = SUBJECTS.map((subject, i) => `
  <div class="form-group">
    <label>${subject}</label>
    <input type="number" name="sub${i + 1}" min="0" max="100" step="1" required>
  </div>`).join("");

// 2. Fill the dropdown with students (✓ = marks already entered)
function renderMarksSelect() {
  const selected = marksSelect.value;                      // remember the choice while redrawing
  marksSelect.innerHTML = '<option value="">-- Choose a student --</option>' +
    students.map(s => `<option value="${s.id}">${esc(s.rollNo)} – ${esc(s.name)}${s.marks ? " ✓" : ""}</option>`).join("");
  marksSelect.value = selected;
}

// 3. When a student is chosen: show their existing marks (or empty boxes)
function fillMarks() {
  const s = students.find(student => student.id === Number(marksSelect.value));
  SUBJECTS.forEach((_, i) => {
    marksForm.elements["sub" + (i + 1)].value = s && s.marks ? s.marks[i] : "";
  });
  updatePreview();
}
marksSelect.addEventListener("change", fillMarks);

// 4. Live preview while typing — same rules as ResultCalc.java
function updatePreview() {
  const values = SUBJECTS.map((_, i) => marksForm.elements["sub" + (i + 1)].value);
  if (values.some(v => v === "")) {                        // not all 5 filled yet
    ["pv-total", "pv-percent", "pv-grade", "pv-status"].forEach(id => document.getElementById(id).innerText = "–");
    return;
  }
  const marks = values.map(Number);
  const total = marks.reduce((sum, m) => sum + m, 0);
  const pct = Math.round(total * 100 / (SUBJECTS.length * 100) * 100) / 100;
  const failed = marks.some(m => m < PASS_MARKS);
  const grade = failed ? "F" : pct >= 90 ? "A+" : pct >= 80 ? "A" : pct >= 70 ? "B+"
              : pct >= 60 ? "B" : pct >= 50 ? "C" : "D";

  document.getElementById("pv-total").innerText = `${total} / ${SUBJECTS.length * 100}`;
  document.getElementById("pv-percent").innerText = pct + "%";
  document.getElementById("pv-grade").innerText = grade;
  document.getElementById("pv-status").innerHTML = statusBadge(failed ? "FAIL" : "PASS");
}
marksForm.addEventListener("input", updatePreview);     // fires when any input inside the form changes

// 5. Save marks
marksForm.addEventListener("submit", async event => {
  event.preventDefault();
  try {
    const data = await api("api/teacher/marks", {
      method: "POST",
      body: new URLSearchParams(new FormData(marksForm))  // studentId, sub1..sub5
    });
    showMessage(marksMessage, data.message);
    await loadStudents();                                 // table shows the new grade
  } catch (err) {
    showMessage(marksMessage, err.message, true);
  }
});

// 6. "Marks" button in the table → choose that student in the marks form
function openMarks(id) {
  marksSelect.value = id;
  fillMarks();
  marksMessage.innerText = "";
  document.getElementById("marks-card").scrollIntoView({ behavior: "smooth" });
}

// ===== Table buttons (event delegation) =====
rows.addEventListener("click", event => {
  const button = event.target.closest("button");
  if (!button) return;
  if (button.dataset.marks) openMarks(Number(button.dataset.marks));      // ★ NEW
  if (button.dataset.edit) startEdit(Number(button.dataset.edit));
  if (button.dataset.delete) deleteStudent(Number(button.dataset.delete));
});

// ===== Logout =====
logoutBtn.addEventListener("click", async () => {
  await fetch("api/auth/logout", { method: "POST" });
  location.href = "login.html";
});
// =====================================================================
// ★ STEP 9: NOTICES
// =====================================================================
const noticeForm = document.getElementById("notice-form");
const isResult = document.getElementById("is-result");
const resultFields = document.getElementById("result-fields");
const noticeMessage = document.getElementById("notice-message");
const noticeList = document.getElementById("notice-list");
const classList = document.getElementById("class-list");

let notices = [];

// Class name suggestions: unique, sorted class names of all students
function renderClassList() {
  const classes = [...new Set(students.map(s => s.className))].sort();
  classList.innerHTML = classes.map(c => `<option value="${esc(c)}">`).join("");
}

// Show the date + class fields only for result notices
isResult.addEventListener("change", () => {
  resultFields.classList.toggle("hidden", !isResult.checked);
  noticeForm.elements.resultDate.required = isResult.checked;   // date is required only when ticked
});

// Same badge rules as notices.js (Step 5)
function noticeBadge(n) {
  if (n.resultDate === null) return '<span class="badge badge-muted">General</span>';
  if (n.released) return '<span class="badge badge-success">Result Released</span>';
  return `<span class="badge badge-warning">Result on ${fmtDate(n.resultDate)}</span>`;
}

// READ — reuse the public API from Step 5
async function loadNotices() {
  notices = await api("api/public/notices");
  if (notices.length === 0) {
    noticeList.innerHTML = '<p class="empty">No notices published yet.</p>';
    return;
  }
  noticeList.innerHTML = notices.map(n => `
    <article class="notice ${n.resultDate ? "result" : ""}">
      <div class="notice-head">
        <h3>${esc(n.title)}</h3>
        ${noticeBadge(n)}
      </div>
      <p>${esc(n.message)}</p>
      <div class="notice-meta">
        <span>Posted ${fmtDate(n.createdAt)}</span>
        ${n.resultDate ? `<span>Class: ${esc(n.className || "All classes")}</span>` : ""}
        <button class="btn btn-danger btn-sm" data-delete-notice="${n.id}">Delete</button>
      </div>
    </article>`).join("");
}

// CREATE
noticeForm.addEventListener("submit", async event => {
  event.preventDefault();
  const data = new FormData(noticeForm);
  if (!isResult.checked) {                 // general notice → don't send date or class at all
    data.delete("resultDate");
    data.delete("className");
  }
  try {
    const result = await api("api/teacher/notices", {
      method: "POST",
      body: new URLSearchParams(data)
    });
    showMessage(noticeMessage, result.message);
    noticeForm.reset();
    isResult.dispatchEvent(new Event("change"));   // hide the result fields again
    await loadNotices();
  } catch (err) {
    showMessage(noticeMessage, err.message, true);
  }
});

// DELETE — one listener for all Delete buttons in the list (event delegation)
noticeList.addEventListener("click", async event => {
  const button = event.target.closest("button[data-delete-notice]");
  if (!button) return;
  if (!confirm("Delete this notice? Students will no longer see it.")) return;
  try {
    const result = await api("api/teacher/notices?id=" + button.dataset.deleteNotice, { method: "DELETE" });
    showMessage(noticeMessage, result.message);
    await loadNotices();
  } catch (err) {
    showMessage(noticeMessage, err.message, true);
  }
});
// ===== Start =====
async function start() {
  const teacher = await api("api/auth/me");
  nameSpan.innerText = teacher.name;
  await loadStudents();
  await loadNotices();
}
start().catch(err => showMessage(formMessage, err.message, true));