const API_BASE='/api';
let currentUser=null;
let editingCommentId=null;
let resourceId=null;

// logout() and authFetch() are provided by api.js


function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-GB', {day:'2-digit', month:'short', year:'numeric'});
}

function badgeClass(cat) {
    let map={BOOK:'badge-book',WHITEPAPER:'badge-whitepaper',STANDARD:'badge-standard',
             LEGAL:'badge-legal',TOOL:'badge-tool',WEBSITE:'badge-website',OTHER:'badge-other'};
    return map[cat] || 'badge-other';
}

function showToast(msg, type='') {
    let t=document.getElementById('toast');
    t.textContent=msg;
    t.className='toast'+(type ? ' toast-'+type : '');
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3000);
}

function showError(msg) {
    let el=document.getElementById('form-error');
    el.textContent=msg;
    el.classList.add('show');
}

// ---- ADD / EDIT MODE ----
async function saveResource() {
    let title=document.getElementById('r-title').value.trim();
    let url=document.getElementById('r-url').value.trim();
    let category=document.getElementById('r-category').value;
    let description=document.getElementById('r-description').value.trim();

    if (!title || !url || !category) {
        showError('Title, URL and category are required.');
        return;
    }

    let btn=document.getElementById('btn-save');
    btn.disabled=true;
    btn.innerHTML='<span class="spinner"></span> Saving...';

    let payload={ title, url, category, description };
    let method=resourceId ? 'PUT' : 'POST';
    let endpoint=resourceId ? API_BASE+'/resources/'+resourceId : API_BASE+'/resources';

    try {
        let res=await authFetch(endpoint, {
            method,
            body: JSON.stringify(payload)
        });

        let data=await res.json();
        if (!res.ok) {
            showError(data.message || 'Save failed');
            return;
        }

        window.location.href='/resource.html?id='+data.id;
    } catch (e) {
        showError('Network error. Try again.');
    } finally {
        btn.disabled=false;
        btn.textContent='Save Resource';
    }
}

// ---- DETAIL VIEW ----
async function loadResourceDetail(id) {
    let box=document.getElementById('resource-info-box');

    try {
        let res=await authFetch(API_BASE+'/resources/'+id);
        if (!res.ok) { box.innerHTML='<p style="color:var(--danger)">Resource not found.</p>'; return; }

        let r=await res.json();
        let isOwner=currentUser && currentUser.id===r.addedById;
        let isAdmin=currentUser && currentUser.role==='ADMIN';
        let canEdit=isOwner||isAdmin;

        box.innerHTML=`
            <div style="display:flex;align-items:center;gap:0.7rem;margin-bottom:0.8rem;flex-wrap:wrap;">
                <span class="category-badge ${badgeClass(r.category)}">${r.category}</span>
            </div>
            <h2>${escHtml(r.title)}</h2>
            <div class="r-meta">
                <span>Added by <strong style="color:var(--accent)">${escHtml(r.addedByUsername)}</strong></span>
                <span>${formatDate(r.createdAt)}</span>
                ${r.updatedAt!==r.createdAt ? '<span>Updated '+formatDate(r.updatedAt)+'</span>' : ''}
            </div>
            <a class="r-url" href="${escHtml(r.url)}" target="_blank" rel="noopener">
                &#128279; ${escHtml(r.url)}
            </a>
            <div class="r-desc">${escHtml(r.description || 'No description provided.')}</div>
            ${canEdit ? `
            <div class="resource-actions">
                <button class="btn btn-outline btn-sm" onclick="switchToEdit(${r.id})">&#9998; Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteResource(${r.id})">&#128465; Delete</button>
            </div>` : ''}
        `;

        await loadComments(id);
    } catch (e) {
        box.innerHTML='<p style="color:var(--danger)">Failed to load resource.</p>';
    }
}

async function switchToEdit(id) {
    let res=await authFetch(API_BASE+'/resources/'+id);
    let r=await res.json();

    document.getElementById('detail-section').style.display='none';
    document.getElementById('add-edit-section').style.display='';
    document.getElementById('form-title').textContent='Edit Resource';
    document.getElementById('resource-id').value=id;
    document.getElementById('r-title').value=r.title;
    document.getElementById('r-url').value=r.url;
    document.getElementById('r-category').value=r.category;
    document.getElementById('r-description').value=r.description||'';
    resourceId=id;
}

async function deleteResource(id) {
    if (!confirm('Delete this resource? This cannot be undone.')) return;
    try {
        let res=await authFetch(API_BASE+'/resources/'+id, {
            method: 'DELETE'
        });
        if (res.ok) {
            window.location.href='/dashboard.html';
        } else {
            showToast('Delete failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

// ---- COMMENTS ----
async function loadComments(id) {
    let list=document.getElementById('comments-list');
    list.innerHTML='<div class="loading-overlay"><div class="spinner"></div></div>';

    try {
        let res=await authFetch(API_BASE+'/resources/'+id+'/comments');
        let comments=await res.json();

        document.getElementById('comment-count').textContent='('+comments.length+')';

        if (comments.length===0) {
            list.innerHTML='<p style="color:var(--text-muted);font-size:0.85rem;padding:1rem 0;">No comments yet. Be the first!</p>';
            return;
        }

        list.innerHTML=comments.map(c => {
            let isOwner=currentUser && currentUser.id===c.userId;
            let isAdmin=currentUser && currentUser.role==='ADMIN';
            let canEdit=isOwner; // Admins can delete others' comments but not edit them
            let canDelete=isOwner||isAdmin;
            return `
            <div class="comment-item" id="comment-${c.id}">
                <div class="comment-header">
                    <span class="author">${escHtml(c.username)}</span>
                    <span class="date">${formatDate(c.createdAt)}</span>
                </div>
                <div class="comment-body">${escHtml(c.content)}</div>
                ${canEdit||canDelete ? `
                <div class="comment-actions">
                    ${canEdit ? `<button class="btn btn-ghost btn-sm" onclick="openEditComment(${c.id},'${escHtml(c.content).replace(/'/g,"\\'")}')">Edit</button>` : ''}
                    ${canDelete ? `<button class="btn btn-danger btn-sm" onclick="deleteComment(${c.id})">Delete</button>` : ''}
                </div>` : ''}
            </div>`;
        }).join('');
    } catch (e) {
        list.innerHTML='<p style="color:var(--danger);font-size:0.85rem;">Failed to load comments.</p>';
    }
}

async function postComment() {
    let input=document.getElementById('comment-input');
    let content=input.value.trim();
    if (!content) return;

    try {
        let res=await authFetch(API_BASE+'/resources/'+resourceId+'/comments', {
            method: 'POST',
            body: JSON.stringify({content})
        });

        if (res.ok) {
            input.value='';
            await loadComments(resourceId);
            showToast('Comment posted', 'success');
        } else {
            showToast('Failed to post comment', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

function openEditComment(id, content) {
    editingCommentId=id;
    document.getElementById('edit-comment-input').value=content;
    document.getElementById('edit-comment-modal').classList.add('show');
}

function closeEditCommentModal() {
    editingCommentId=null;
    document.getElementById('edit-comment-modal').classList.remove('show');
}

async function submitEditComment() {
    let content=document.getElementById('edit-comment-input').value.trim();
    if (!content) return;

    try {
        let res=await authFetch(API_BASE+'/comments/'+editingCommentId, {
            method: 'PUT',
            body: JSON.stringify({content})
        });

        if (res.ok) {
            closeEditCommentModal();
            await loadComments(resourceId);
            showToast('Comment updated', 'success');
        } else {
            showToast('Update failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

async function deleteComment(id) {
    if (!confirm('Delete this comment?')) return;
    try {
        let res=await authFetch(API_BASE+'/comments/'+id, {
            method: 'DELETE'
        });
        if (res.ok) {
            await loadComments(resourceId);
            showToast('Comment deleted', 'success');
        } else {
            showToast('Delete failed', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

// ---- INIT ----
(function() {
    if (!localStorage.getItem('osls_token')) { window.location.href='/index.html'; return; }

    let userData=localStorage.getItem('osls_user');
    if (userData) {
        currentUser=JSON.parse(userData);
        document.getElementById('nav-username').textContent=currentUser.username;
        if (currentUser.role==='ADMIN') {
            document.getElementById('nav-admin').style.display='';
        }
    }

    let params=new URLSearchParams(window.location.search);
    let idParam=params.get('id');

    if (idParam) {
        resourceId=parseInt(idParam);
        document.getElementById('add-edit-section').style.display='none';
        document.getElementById('detail-section').style.display='';
        loadResourceDetail(resourceId);
    }
    // else: show add form by default
})();
