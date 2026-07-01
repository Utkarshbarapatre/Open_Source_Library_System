/**
 * api.js — Shared authenticated fetch with automatic refresh-token handling.
 *
 * Usage:  const data = await authFetch('/api/resources');
 *
 * On a 401, it will attempt to silently renew the access token using the
 * stored refresh token, then retry the original request once.
 * If renewal fails, the user is redirected to the login page.
 */

async function authFetch(url, options = {}) {
    const token = localStorage.getItem('osls_token');

    const defaults = {
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': 'Bearer ' + token } : {})
        }
    };

    // Merge caller headers with defaults (caller headers win)
    const merged = {
        ...defaults,
        ...options,
        headers: {
            ...defaults.headers,
            ...(options.headers || {})
        }
    };

    let response = await fetch(url, merged);

    // If 401 — try to refresh the access token and retry once
    if (response.status === 401) {
        const refreshed = await tryRefreshToken();
        if (!refreshed) {
            redirectToLogin();
            return response; // won't reach, but satisfies linters
        }

        // Retry with the new access token
        const newToken = localStorage.getItem('osls_token');
        merged.headers['Authorization'] = 'Bearer ' + newToken;
        response = await fetch(url, merged);

        // If still 401 after refresh — session is truly invalid
        if (response.status === 401) {
            redirectToLogin();
        }
    }

    return response;
}

/**
 * Calls POST /api/auth/refresh with the stored refresh token.
 * On success, updates localStorage with the new tokens.
 * Returns true if successful, false otherwise.
 */
async function tryRefreshToken() {
    const refreshToken = localStorage.getItem('osls_refresh_token');
    if (!refreshToken) return false;

    try {
        const res = await fetch('/api/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (!res.ok) return false;

        const data = await res.json();
        localStorage.setItem('osls_token', data.accessToken);
        localStorage.setItem('osls_refresh_token', data.refreshToken);
        return true;
    } catch (e) {
        return false;
    }
}

/**
 * Calls POST /api/auth/logout to revoke the refresh token, then clears
 * localStorage and redirects to the login page.
 */
async function logout() {
    const refreshToken = localStorage.getItem('osls_refresh_token');

    if (refreshToken) {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });
        } catch (e) {
            // Fire-and-forget — clear storage regardless
        }
    }

    localStorage.removeItem('osls_token');
    localStorage.removeItem('osls_refresh_token');
    localStorage.removeItem('osls_user');
    window.location.href = '/index.html';
}

function redirectToLogin() {
    localStorage.removeItem('osls_token');
    localStorage.removeItem('osls_refresh_token');
    localStorage.removeItem('osls_user');
    window.location.href = '/index.html';
}
