const form = document.getElementById("result-form");
const output = document.getElementById("output");

// esc() and fmtDate() come from common.js (loaded first)

// ===== Submit: ask the server =====
form.addEventListener("submit", async event => {
  event.preventDefault();
  const button = form.querySelector("button");
  button.disabled = true;                                  // stop double-clicks
  output.innerHTML = '<div class="loader"></div>';

  try {
    const response = await fetch("api/public/result", {
      method: "POST",
      body: new URLSearchParams(new FormData(form))
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error);                         // 400 / 404 / 500
    }
    output.innerHTML = data.published ? marksheetHtml(data) : waitingHtml(data);
  } catch (err) {
    output.innerHTML = messageBox("Result not found", err.message, true);
  } finally {
    button.disabled = false;                               // runs after success OR error
  }
});

// ===== "Not yet" box =====
function waitingHtml(data) {
  return `
    <div class="card state-box" style="max-width:820px;margin:0 auto">
      <div class="big-icon">⏳</div>
      <h3>Hello ${esc(data.name)}, your result is not available yet</h3>
      <p class="muted">${esc(data.message)}</p>
      ${data.resultDate ? `<div class="date-pill">${fmtDate(data.resultDate)}</div>` : ""}
    </div>`;
}

// ===== Error box =====
function messageBox(title, text, isError) {
  return `
    <div class="card state-box" style="max-width:820px;margin:0 auto">
      <div class="big-icon" style="${isError ? "background:var(--danger-bg);color:var(--danger)" : ""}">!</div>
      <h3>${esc(title)}</h3>
      <p class="muted">${esc(text)}</p>
    </div>`;
}

// ===== The marksheet =====
function marksheetHtml(data) {
  const s = data.student;

  // One table row per subject — the subject list comes from the server (ResultCalc.SUBJECTS)
  const rows = data.subjects.map((subject, i) => {
    const mark = s.marks[i];
    const pass = mark >= data.passMarks;
    return `
      <tr>
        <td>${i + 1}</td>
        <td>${esc(subject)}</td>
        <td class="num">${data.maxMarks}</td>
        <td class="num">${data.passMarks}</td>
        <td class="num ${pass ? "" : "low"}">${mark}</td>
        <td class="num">${pass
          ? '<span class="badge badge-success">Pass</span>'
          : '<span class="badge badge-danger">Fail</span>'}</td>
      </tr>`;
  }).join("");

  const passed = s.status === "PASS";

  return `
    <div class="card marksheet">
      <div class="ms-head">
        <div>
          <h2>Statement of Marks</h2>
          <div style="opacity:.9;font-size:.9rem">Result declared on ${fmtDate(data.resultDate)}</div>
        </div>
        <span class="badge ${passed ? "badge-success" : "badge-danger"}" style="font-size:1rem">${s.status}</span>
      </div>

      <div class="ms-body">
        <div class="ms-info">
          <div><span>Student Name</span><strong>${esc(s.name)}</strong></div>
          <div><span>Roll Number</span><strong>${esc(s.rollNo)}</strong></div>
          <div><span>Class</span><strong>${esc(s.className)}</strong></div>
          <div><span>Date of Birth</span><strong>${fmtDate(s.dob)}</strong></div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr><th>#</th><th>Subject</th><th class="num">Max</th><th class="num">Pass</th>
                  <th class="num">Obtained</th><th class="num">Status</th></tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
        </div>

        <div class="ms-summary">
          <div><span>Total</span><strong>${s.total} / ${s.maxTotal}</strong></div>
          <div><span>Percentage</span><strong>${s.percentage}%</strong></div>
          <div><span>Grade</span><strong>${esc(s.grade)}</strong></div>
          <div><span>Result</span><strong style="color:${passed ? "var(--success)" : "var(--danger)"}">${s.status}</strong></div>
        </div>

        <div class="form-actions no-print" style="justify-content:center">
          <button class="btn btn-primary" onclick="window.print()">Print / Save as PDF</button>
        </div>
      </div>
    </div>`;
}