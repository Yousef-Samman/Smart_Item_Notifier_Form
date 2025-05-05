const form = document.getElementById("userForm");

form.addEventListener("submit", function(event) {
  event.preventDefault();

  const name = document.getElementById("name").value;
  const gender = document.getElementById("gender").value;
  const email = document.getElementById("email").value;
  const quantity = document.getElementById("quantity").value;
  const photoFile = document.getElementById("photo").files[0];

  const newSubmission = {
    name,
    gender,
    email,
    quantity,
    photoName: photoFile ? photoFile.name : "No Photo",
    timestamp: new Date().toISOString()
  };

  // Store in localStorage (optional)
  let submissions = JSON.parse(localStorage.getItem("userSubmissions")) || [];
  submissions.push(newSubmission);
  localStorage.setItem("userSubmissions", JSON.stringify(submissions));

  // ✅ Post data manually using fetch to show a success message
  const formData = new FormData(form);

  fetch("http://localhost:9091/submit", {
    method: "POST",
    body: formData
  })
  .then(response => {
    if (response.ok) {
      alert("✅ Submission successful!");
      form.reset(); // Clear form only on success
    } else {
      alert("❌ Submission failed. Please try again.");
    }
  })
  .catch(() => {
    alert("⚠️ Network error while submitting the form.");
  });
});
