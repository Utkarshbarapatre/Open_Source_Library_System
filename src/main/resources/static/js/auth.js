const API='/api/auth';

function switchTab(tab) {
    document.getElementById('form-login').classList.remove('active');
    document.getElementById('form-register').classList.remove('active');
    document.getElementById('tab-login').classList.remove('active');
    document.getElementById('tab-register').classList.remove('active');

    document.getElementById('form-'+tab).classList.add('active');
    document.getElementById('tab-'+tab).classList.add('active');

    clearErrors();
}

function clearErrors() {
    ['login-error','register-error','register-success'].forEach(id => {
        let el=document.getElementById(id);
        el.textContent='';
        el.classList.remove('show');
    });
}

function showError(id, msg) {
    let el=document.getElementById(id);
    el.textContent=msg;
    el.classList.add('show');
}

function showSuccess(id, msg) {
    let el=document.getElementById(id);
    el.textContent=msg;
    el.classList.add('show');
}

async function doLogin() {
    let username=document.getElementById('login-username').value.trim();
    let password=document.getElementById('login-password').value;

    if (!username || !password) {
        showError('login-error', 'Please fill in all fields.');
        return;
    }

    let btn=document.getElementById('btn-login');
    btn.disabled=true;
    btn.innerHTML='<span class="spinner"></span> Signing in...';

    try {
        let res=await fetch(API+'/login', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({username, password})
        });

        let data=await res.json();
        if (!res.ok) {
            showError('login-error', data.message || 'Invalid credentials');
            return;
        }

        // Store both access token and refresh token
        localStorage.setItem('osls_token', data.token);
        localStorage.setItem('osls_refresh_token', data.refreshToken);
        localStorage.setItem('osls_user', JSON.stringify({
            id: data.id,
            username: data.username,
            email: data.email,
            role: data.role
        }));

        window.location.href='/dashboard.html';
    } catch (e) {
        showError('login-error', 'Network error. Try again.');
    } finally {
        btn.disabled=false;
        btn.textContent='Sign In';
    }
}

async function doRegister() {
    let username=document.getElementById('reg-username').value.trim();
    let email=document.getElementById('reg-email').value.trim();
    let password=document.getElementById('reg-password').value;

    if (!username || !email || !password) {
        showError('register-error', 'Please fill in all fields.');
        return;
    }

    if (password.length<6) {
        showError('register-error', 'Password must be at least 6 characters.');
        return;
    }

    let btn=document.getElementById('btn-register');
    btn.disabled=true;
    btn.innerHTML='<span class="spinner"></span> Creating account...';

    try {
        let res=await fetch(API+'/register', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({username, email, password})
        });

        let data=await res.json();
        if (!res.ok) {
            showError('register-error', data.message || 'Registration failed');
            return;
        }

        showSuccess('register-success', 'Account created! You can now sign in.');
        document.getElementById('reg-username').value='';
        document.getElementById('reg-email').value='';
        document.getElementById('reg-password').value='';
        setTimeout(() => switchTab('login'), 1500);
    } catch (e) {
        showError('register-error', 'Network error. Try again.');
    } finally {
        btn.disabled=false;
        btn.textContent='Create Account';
    }
}

// Enter key support
document.addEventListener('keydown', function(e) {
    if (e.key==='Enter') {
        let loginActive=document.getElementById('form-login').classList.contains('active');
        if (loginActive) doLogin();
        else doRegister();
    }
});

// Redirect if already logged in
(function() {
    if (localStorage.getItem('osls_token')) {
        window.location.href='/dashboard.html';
    }
})();
