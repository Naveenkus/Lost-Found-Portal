import React from 'react';
import { Search, Compass, PlusCircle, LogIn, LogOut, User as UserIcon, ShieldAlert } from 'lucide-react';

export default function Navbar({
  activeTab,
  setActiveTab,
  searchQuery,
  setSearchQuery,
  user,
  onOpenAuth,
  onOpenReport,
  onLogout
}) {
  return (
    <header className="navbar-container">
      <div className="navbar-content">
        {/* Brand Logo */}
        <div className="brand-logo" onClick={() => setActiveTab('all')}>
          <div className="logo-icon">
            <Compass className="w-6 h-6 text-indigo-400 animate-spin-slow" />
          </div>
          <div className="logo-text">
            <span className="brand-title">Lostoria</span>
            <span className="brand-badge">PORTAL</span>
          </div>
        </div>

        {/* Search Bar */}
        <div className="nav-search-bar">
          <Search className="search-icon" size={18} />
          <input
            type="text"
            placeholder="Search lost & found items by keyword, location..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
          {searchQuery && (
            <button className="clear-search" onClick={() => setSearchQuery('')}>
              ×
            </button>
          )}
        </div>

        {/* Actions & User State */}
        <div className="nav-actions">
          <button
            className="btn btn-primary btn-report"
            onClick={() => onOpenReport(activeTab === 'found' ? 'FOUND' : 'LOST')}
          >
            <PlusCircle size={18} />
            <span>Report Item</span>
          </button>

          {user ? (
            <div className="user-profile-menu">
              <div className="user-avatar" title={user.username || user.email}>
                <UserIcon size={16} />
                <span className="username-display">{user.username || 'User'}</span>
              </div>
              <button className="btn btn-ghost btn-logout" onClick={onLogout} title="Log Out">
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <button className="btn btn-secondary btn-login" onClick={onOpenAuth}>
              <LogIn size={18} />
              <span>Sign In</span>
            </button>
          )}
        </div>
      </div>

      {/* Filter Navigation Tabs */}
      <div className="nav-tabs-bar">
        <div className="tabs-wrapper">
          <button
            className={`tab-btn ${activeTab === 'all' ? 'active' : ''}`}
            onClick={() => setActiveTab('all')}
          >
            All Items
          </button>
          <button
            className={`tab-btn tab-lost ${activeTab === 'lost' ? 'active' : ''}`}
            onClick={() => setActiveTab('lost')}
          >
            <span className="tab-dot dot-lost"></span>
            Lost Items
          </button>
          <button
            className={`tab-btn tab-found ${activeTab === 'found' ? 'active' : ''}`}
            onClick={() => setActiveTab('found')}
          >
            <span className="tab-dot dot-found"></span>
            Found Items
          </button>
        </div>
      </div>
    </header>
  );
}