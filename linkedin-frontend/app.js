// ===== Base API URL =====
const API = "https://linkedin-backend-5k8n.onrender.com/api";

// ================= AUTH SECTION =================

// Signup new user
async function signup() {
  const name = document.getElementById("signupName").value;
  const email = document.getElementById("signupEmail").value;
  const password = document.getElementById("signupPassword").value;

  try {
    const res = await fetch(`${API}/auth/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email, password }),
      credentials: "include"
    });

    console.log("Signup response:", res);

    const text = await res.text();
    console.log("Signup raw text:", text);

    let data;
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }

    alert(data.message || data.error || "Signup completed");
  } catch (err) {
    console.error("Signup error:", err);
    alert("Signup failed. Please check console for details.");
  }
}

// Login user
async function login() {
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  try {
    const res = await fetch(`${API}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
      credentials: "include"
    });

    if (res.ok) {
      window.location.href = "feed.html";
    } else {
      const data = await res.json();
      alert(data.error || "Login failed");
    }
  } catch (err) {
    console.error("Login error:", err);
    alert("Login failed. Please try again.");
  }
}

// Logout
async function logout() {
  await fetch(`${API}/auth/logout`, { method: "POST", credentials: "include" });
  window.location.href = "index.html";
}

// ================= FEED SECTION =================

// Load logged-in user info
async function loadUser() {
  const res = await fetch(`${API}/auth/me`, { credentials: "include" });
  if (res.ok) {
    const data = await res.json();
    document.getElementById("user-info").innerText = data.name;
    loadPosts();
  } else {
    window.location.href = "index.html";
  }
}

// Create a new post
async function createPost() {
  const content = document.getElementById("postContent").value;
  if (!content.trim()) return alert("Post cannot be empty");

  const res = await fetch(`${API}/posts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content }),
    credentials: "include"
  });

  const data = await res.json();
  if (res.ok) {
    document.getElementById("postContent").value = "";
    loadPosts();
  } else {
    alert(data.error);
  }
}

// Load posts feed
async function loadPosts() {
  const res = await fetch(`${API}/posts`, { credentials: "include" });
  const posts = await res.json();

  const container = document.getElementById("posts");
  container.innerHTML = "";

  posts.forEach(p => {
    const div = document.createElement("div");
    div.className = "post";
    div.innerHTML = `
      <div class="meta">
        <b>${p.userName}</b> • ${new Date(p.createdAt).toLocaleString()}
      </div>
      <div class="content">${p.content}</div>
      <div class="post-actions">
        <button onclick="likePost(${p.id})">👍 Like</button>
        <button onclick="openCommentModal(${p.id})">💬 Comment</button>
        <button onclick="editPost(${p.id}, '${p.content.replace(/'/g, "\\'")}')">✏️ Edit</button>
        <button onclick="deletePost(${p.id})">🗑️ Delete</button>
      </div>
    `;
    container.appendChild(div);
  });
}

// ================= LIKE SECTION =================

// Like or Unlike a post
async function likePost(postId) {
  const res = await fetch(`${API}/likes/${postId}`, {
    method: "POST",
    credentials: "include"
  });
  const data = await res.json();
  alert(data.message);
  loadPosts();
}

// ================= COMMENT SECTION =================

let currentPostId = null;

function openCommentModal(postId) {
  currentPostId = postId;
  document.getElementById("commentModal").style.display = "block";
  loadComments(postId);
}

function closeCommentModal() {
  document.getElementById("commentModal").style.display = "none";
}

// Submit a new comment
async function submitComment() {
  const content = document.getElementById("commentContent").value;
  if (!content.trim()) return alert("Comment cannot be empty");

  await fetch(`${API}/comments/${currentPostId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content }),
    credentials: "include"
  });

  document.getElementById("commentContent").value = "";
  loadComments(currentPostId);
}

// Load all comments for a post
async function loadComments(postId) {
  const res = await fetch(`${API}/comments/${postId}`, { credentials: "include" });
  const comments = await res.json();
  const div = document.getElementById("commentList");
  div.innerHTML = "<h4>Comments:</h4>";

  comments.forEach(c => {
    const p = document.createElement("p");
    p.textContent = `${c.userName}: ${c.content}`;
    div.appendChild(p);
  });
}

// ================= POST EDIT / DELETE =================

// Edit existing post
async function editPost(postId, oldContent) {
  const newContent = prompt("Edit your post:", oldContent);
  if (newContent === null || newContent.trim() === "") return;

  await fetch(`${API}/posts/${postId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content: newContent }),
    credentials: "include"
  });

  alert("Post updated!");
  loadPosts();
}

// Delete a post
async function deletePost(postId) {
  if (!confirm("Are you sure you want to delete this post?")) return;

  try {
    const res = await fetch(`${API}/posts/${postId}`, {
      method: "DELETE",
      credentials: "include"
    });

    console.log("Delete response status:", res.status);

    if (res.ok) {
      alert("Post deleted!");
      loadPosts();
    } else {
      // Try to read response body if it exists
      let errorMsg = "Failed to delete post";
      try {
        const text = await res.text();
        if (text) {
          console.error("Delete failed:", res.status, text);
          try {
            const err = JSON.parse(text);
            errorMsg = err.error || err.message || errorMsg;
          } catch {
            errorMsg = text;
          }
        }
      } catch {
        // Response body might be empty or unreadable
        errorMsg = `Failed to delete post (Status: ${res.status})`;
      }

      alert(errorMsg);
    }
  } catch (e) {
    console.error("Network or fetch error deleting post:", e);
    alert("Delete failed — network error. See console.");
  }
}

// ================= PROFILE SECTION =================

// Go to profile page
function openProfile() {
  window.location.href = "profile.html";
}

// Load profile info
async function loadProfile() {
  const res = await fetch(`${API}/auth/me`, { credentials: "include" });
  if (!res.ok) {
    window.location.href = "index.html";
    return;
  }

  const data = await res.json();
  document.getElementById("profileName").innerText = data.name;
  document.getElementById("profileEmail").innerText = data.email;
  document.getElementById("profileCreated").innerText = new Date().toLocaleDateString();
}

// Go back from profile to feed
function goBack() {
  window.location.href = "feed.html";
}

// ================= AUTO INIT =================

// When on feed page, load posts and user
if (window.location.pathname.endsWith("feed.html")) {
  loadUser();
}

// When on profile page, load profile
if (window.location.pathname.endsWith("profile.html")) {
  loadProfile();
}