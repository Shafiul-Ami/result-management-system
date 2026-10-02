// Notice board page. esc(), fmtDate() and noticeCard() come from common.js (loaded first).

const box = document.getElementById("notices");

fetch("api/public/notices")
  .then(response => {
    if (!response.ok) {                       // status 500 from the servlet's catch block
      throw new Error("Could not load notices (" + response.status + ")");
    }
    return response.json();                   // text → JavaScript array of objects
  })
  .then(notices => {
    if (notices.length === 0) {
      box.innerHTML = "<p>No notices yet.</p>";
      return;
    }
    box.innerHTML = notices.map(noticeCard).join("");   // array of HTML strings → one string
  })
  .catch(err => {
    box.innerHTML = `<p style="color:red">${esc(err.message)}</p>`;
  });
