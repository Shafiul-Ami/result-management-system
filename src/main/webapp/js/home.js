const latest = document.getElementById("latest-notices");

fetch("api/public/notices")
  .then(response => {
    if (!response.ok) throw new Error("Could not load notices");
    return response.json();
  })
  .then(notices => {
    latest.innerHTML = notices.length
      ? notices.slice(0, 3).map(noticeCard).join("")       // only the newest 3
      : '<div class="card empty">No notices yet.</div>';
  })
  .catch(err => {
    latest.innerHTML = `<div class="card empty">${esc(err.message)}</div>`;
  });