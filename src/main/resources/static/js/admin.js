const API_BASE='/api';
let currentUser=null;

// logout() and authFetch() are provided by api.js


function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-GB', {day:'2-digit', month:'short', year:'numeric'});
}

function showToast(msg, type='') {
    let t=document.getElementById('toast');
    t.textContent=msg;
    t.className='toast'+(type ? ' toast-'+type : '');
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3000);
}

function switchAdminTab(tab) {
    document.querySelectorAll('.admin-tab').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.admin-panel').forEach(el => el.classList.remove('active'));
    document.getElementById('tab-'+tab).classList.add('active');
    document.getElementById('panel-'+tab).classList.add('active');
}

async function loadUsers() {
    let container=document.getElementById('users-table-container');
    try {
        let res=await authFetch(API_BASE+'/admin/users');
        if (res.status===403) { logout(); return; }
        if (!res.ok) {
            container.innerHTML=`<div class="empty-state"><p>Failed to load users (status ${res.status}).</p></div>`;
            return;
        }
        let users=await res.json();

        if (users.length===0) {
            container.innerHTML='<div class="empty-state"><p>No users found.</p></div>';
            return;
        }

        let html=`
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Joined</th><th>Action</th>
                </tr>
            </thead>
            <tbody>`;

        users.forEach(u => {
            let roleBadge=u.role==='ADMIN'
                ? '<span class="role-badge role-admin">ADMIN</span>'
                : '<span class="role-badge role-user">USER</span>';
            let isSelf=currentUser && currentUser.id===u.id;

            html+=`<tr>
                <td>${u.id}</td>
                <td>${escHtml(u.username)}</td>
                <td>${escHtml(u.email)}</td>
                <td>${roleBadge}</td>
                <td>${formatDate(u.createdAt)}</td>
                <td>${isSelf ? '<span style="color:var(--text-muted);font-size:0.8rem;">You</span>' : `<button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id})">Delete</button>`}</td>
            </tr>`;
        });

        html+='</tbody></table>';
        container.innerHTML=html;
    } catch (e) {
        container.innerHTML='<div class="empty-state"><p>Failed to load users. Check the console for details.</p></div>';
        console.error('loadUsers error:', e);
    }
}

async function deleteUser(id) {
    if (!confirm('Delete this user and all their data?')) return;
    try {
        let res=await authFetch(API_BASE+'/admin/users/'+id, {
            method: 'DELETE'
        });
        if (res.ok) {
            showToast('User deleted', 'success');
            loadUsers();
        } else {
            showToast('Delete failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

async function loadResources() {
    let container=document.getElementById('resources-table-container');
    try {
        let res=await authFetch(API_BASE+'/resources');
        if (res.status===403) { logout(); return; }
        if (!res.ok) {
            container.innerHTML=`<div class="empty-state"><p>Failed to load resources (status ${res.status}).</p></div>`;
            return;
        }
        let resources=await res.json();

        if (resources.length===0) {
            container.innerHTML='<div class="empty-state"><p>No resources found.</p></div>';
            return;
        }

        let html=`
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th><th>Title</th><th>Category</th><th>Added By</th><th>Date</th><th>Comments</th><th>Action</th>
                </tr>
            </thead>
            <tbody>`;

        resources.forEach(r => {
            html+=`<tr>
                <td>${r.id}</td>
                <td><a href="/resource.html?id=${r.id}" style="color:var(--accent)">${escHtml(r.title)}</a></td>
                <td>${r.category}</td>
                <td>${escHtml(r.addedByUsername)}</td>
                <td>${formatDate(r.createdAt)}</td>
                <td>${r.commentCount}</td>
                <td><button class="btn btn-danger btn-sm" onclick="deleteResource(${r.id})">Delete</button></td>
            </tr>`;
        });

        html+='</tbody></table>';
        container.innerHTML=html;
    } catch (e) {
        container.innerHTML='<div class="empty-state"><p>Failed to load resources. Check the console for details.</p></div>';
        console.error('loadResources error:', e);
    }
}

async function deleteResource(id) {
    if (!confirm('Delete this resource and all its comments?')) return;
    try {
        let res=await authFetch(API_BASE+'/admin/resources/'+id, {
            method: 'DELETE'
        });
        if (res.ok) {
            showToast('Resource deleted', 'success');
            loadResources();
        } else {
            showToast('Delete failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

// Init
(function() {
    let token=localStorage.getItem('osls_token');
    if (!token) { window.location.href='/index.html'; return; }

    let userData=localStorage.getItem('osls_user');
    if (!userData) { window.location.href='/index.html'; return; }

    currentUser=JSON.parse(userData);
    if (currentUser.role!=='ADMIN') {
        window.location.href='/dashboard.html';
        return;
    }

    document.getElementById('nav-username').textContent=currentUser.username;

    loadUsers();
    loadResources();
})();
