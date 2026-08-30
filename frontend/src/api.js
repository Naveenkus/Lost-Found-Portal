// Lostoria Backend API Client
export const API_BASE = '/api';

export const getAuthToken = () => localStorage.getItem('lostoria_token');
export const setAuthToken = (token) => localStorage.setItem('lostoria_token', token);
export const removeAuthToken = () => localStorage.removeItem('lostoria_token');

// Stale localStorage cached entries pre-dating this fix only have { username }; re-login populates full id and role.
export const getStoredUser = () => {
  const user = localStorage.getItem('lostoria_user');
  return user ? JSON.parse(user) : null;
};
export const setStoredUser = (user) => localStorage.setItem('lostoria_user', JSON.stringify(user));
export const removeStoredUser = () => localStorage.removeItem('lostoria_user');

const authHeaders = (isMultipart = false) => {
  const token = getAuthToken();
  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (!isMultipart) {
    headers['Content-Type'] = 'application/json';
  }
  return headers;
};

export const api = {
  // Auth
  async login(usernameOrEmail, password) {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: usernameOrEmail, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Invalid credentials' }));
      throw new Error(err.message || 'Login failed');
    }
    return res.json();
  },

  async register(username, email, password) {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Registration failed' }));
      throw new Error(err.message || 'Registration failed');
    }
    return res.json();
  },

  // Lost Items
  async getLostItems() {
    const res = await fetch(`${API_BASE}/lost-items`);
    if (!res.ok) throw new Error('Failed to fetch lost items');
    return res.json();
  },

  async createLostItem(formData) {
    const res = await fetch(`${API_BASE}/lost-items`, {
      method: 'POST',
      headers: authHeaders(true),
      body: formData,
    });
    if (!res.ok) throw new Error('Failed to post lost item');
    return res.json();
  },

  async deleteLostItem(id) {
    const res = await fetch(`${API_BASE}/lost-items/${id}`, {
      method: 'DELETE',
      headers: authHeaders(),
    });
    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      const err = new Error(msg || 'Failed to delete item');
      err.status = res.status;
      throw err;
    }
    return res;
  },

  // Found Items
  async getFoundItems() {
    const res = await fetch(`${API_BASE}/found-items`);
    if (!res.ok) throw new Error('Failed to fetch found items');
    return res.json();
  },

  async createFoundItem(formData) {
    const res = await fetch(`${API_BASE}/found-items`, {
      method: 'POST',
      headers: authHeaders(true),
      body: formData,
    });
    if (!res.ok) throw new Error('Failed to post found item');
    return res.json();
  },

  async deleteFoundItem(id) {
    const res = await fetch(`${API_BASE}/found-items/${id}`, {
      method: 'DELETE',
      headers: authHeaders(),
    });
    if (!res.ok) {
      const msg = await res.text().catch(() => '');
      const err = new Error(msg || 'Failed to delete item');
      err.status = res.status;
      throw err;
    }
    return res;
  },

  // Image URL Helper
  getImageUrl(imageId) {
    if (!imageId) return null;
    return `${API_BASE}/images/view/id/${imageId}`;
  }
};