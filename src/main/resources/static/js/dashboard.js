const API_BASE='/api';
let searchTimer=null;
let currentUser=null;

// logout() and authFetch() are provided by api.js


function badgeClass(category) {
    let map={
        BOOK:'badge-book', WHITEPAPER:'badge-whitepaper', STANDARD:'badge-standard',
        LEGAL:'badge-legal', TOOL:'badge-tool', WEBSITE:'badge-website', OTHER:'badge-other'
    };
    return map[category] || 'badge-other';
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    let d=new Date(dateStr);
    return d.toLocaleDateString('en-GB', {day:'2-digit', month:'short', year:'numeric'});
}

function showToast(msg, type='') {
    let t=document.getElementById('toast');
    t.textContent=msg;
    t.className='toast'+(type ? ' toast-'+type : '');
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3000);
}

async function loadResources() {
    let q=document.getElementById('search-input').value.trim();
    let category=document.getElementById('category-filter').value;
    let container=document.getElementById('resources-container');
    container.innerHTML='<div class="loading-overlay"><div class="spinner"></div></div>';

    let url=API_BASE+'/resources/search?';
    if (q) url+='q='+encodeURIComponent(q)+'&';
    if (category) url+='category='+category;

    try {
        let res=await authFetch(url);
        let data=await res.json();
        renderResources(data);
    } catch (e) {
        container.innerHTML='<div class="empty-state"><div class="icon">&#9888;</div><p>Failed to load resources.</p></div>';
    }
}

function renderResources(list) {
    let container=document.getElementById('resources-container');
    let countEl=document.getElementById('results-count');

    countEl.textContent=list.length+' resource'+(list.length!==1 ? 's' : '')+' found';

    if (list.length===0) {
        container.innerHTML='<div class="empty-state"><div class="icon">&#128193;</div><p>No resources found. <a href="/resource.html">Add the first one!</a></p></div>';
        return;
    }

    let html='<div class="resources-grid">';
    list.forEach(r => {
        html+=`
        <div class="resource-card" onclick="openResource(${r.id})">
            <div>
                <span class="category-badge ${badgeClass(r.category)}">${r.category}</span>
            </div>
            <h3>${escHtml(r.title)}</h3>
            <div class="desc">${escHtml(r.description || 'No description provided.')}</div>
            <div class="meta">
                <span>By <span class="contributor">${escHtml(r.addedByUsername)}</span></span>
                <span>${formatDate(r.createdAt)} &bull; ${r.commentCount} comment${r.commentCount!==1 ? 's' : ''}</span>
            </div>
        </div>`;
    });
    html+='</div>';
    container.innerHTML=html;
}

function openResource(id) {
    window.location.href='/resource.html?id='+id;
}

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function debounceSearch() {
    clearTimeout(searchTimer);
    searchTimer=setTimeout(loadResources, 350);
}

function clearSearch() {
    document.getElementById('search-input').value='';
    document.getElementById('category-filter').value='';
    loadResources();
}

// Init
(function() {
    let token=localStorage.getItem('osls_token');
    if (!token) { window.location.href='/index.html'; return; }

    let userData=localStorage.getItem('osls_user');
    if (userData) {
        currentUser=JSON.parse(userData);
        document.getElementById('nav-username').textContent=currentUser.username;
        if (currentUser.role==='ADMIN') {
            document.getElementById('nav-admin').style.display='';
        }
    }

    loadResources();
})();
