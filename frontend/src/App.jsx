import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HeroBanner from './components/HeroBanner';
import ItemCard from './components/ItemCard';
import ItemModal from './components/ItemModal';
import ReportModal from './components/ReportModal';
import AuthModal from './components/AuthModal';
import { api, getStoredUser, removeAuthToken, removeStoredUser } from './api';
import { Loader2, Search, PlusCircle, RefreshCw } from 'lucide-react';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('all'); // 'all', 'lost', 'found'
  const [searchQuery, setSearchQuery] = useState('');
  
  const [lostItems, setLostItems] = useState([]);
  const [foundItems, setFoundItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modals state
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedItemType, setSelectedItemType] = useState('LOST');
  const [isReportOpen, setIsReportOpen] = useState(false);
  const [reportDefaultType, setReportDefaultType] = useState('LOST');
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  // Current user state
  const [currentUser, setCurrentUser] = useState(getStoredUser());

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(''), 4000);
  };

  const fetchItems = async () => {
    setLoading(true);
    setError('');
    try {
      const [lost, found] = await Promise.all([
        api.getLostItems().catch(() => []),
        api.getFoundItems().catch(() => [])
      ]);
      setLostItems(Array.isArray(lost) ? lost : []);
      setFoundItems(Array.isArray(found) ? found : []);
    } catch (err) {
      console.error(err);
      setError('Could not connect to backend server. Please make sure Spring Boot is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, []);

  const handleLogout = () => {
    removeAuthToken();
    removeStoredUser();
    setCurrentUser(null);
    showToast('You have been logged out.');
  };

  const handleOpenReport = (type = 'LOST') => {
    if (!currentUser) {
      setIsAuthOpen(true);
      return;
    }
    setReportDefaultType(type);
    setIsReportOpen(true);
  };

  // Combine items according to active tab
  let displayList = [];
  if (activeTab === 'all' || activeTab === 'lost') {
    displayList = displayList.concat(
      lostItems.map(item => ({ ...item, _type: 'LOST' }))
    );
  }
  if (activeTab === 'all' || activeTab === 'found') {
    displayList = displayList.concat(
      foundItems.map(item => ({ ...item, _type: 'FOUND' }))
    );
  }

  // Sort newest first
  displayList.sort((a, b) => {
    const dateA = new Date(a.createdAt || a.datelost || a.dateFound || 0).getTime();
    const dateB = new Date(b.createdAt || b.datelost || b.dateFound || 0).getTime();
    return dateB - dateA;
  });

  // Filter with search query
  if (searchQuery.trim()) {
    const q = searchQuery.toLowerCase().trim();
    displayList = displayList.filter(item => {
      const title = (item.title || '').toLowerCase();
      const desc = (item.description || '').toLowerCase();
      const loc = (item.locationLost || item.locationFound || '').toLowerCase();
      return title.includes(q) || desc.includes(q) || loc.includes(q);
    });
  }

  return (
    <div className="app-container">
      {/* Toast Banner */}
      {toastMessage && (
        <div className="form-success-banner" style={{ position: 'fixed', top: '20px', right: '20px', zIndex: 100, boxShadow: '0 8px 24px rgba(0,0,0,0.5)' }}>
          {toastMessage}
        </div>
      )}

      {/* Top Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        user={currentUser}
        onOpenAuth={() => setIsAuthOpen(true)}
        onOpenReport={handleOpenReport}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {/* Hero Section */}
        <HeroBanner
          totalLost={lostItems.length}
          totalFound={foundItems.length}
          onReportLost={() => handleOpenReport('LOST')}
          onReportFound={() => handleOpenReport('FOUND')}
        />

        {/* Feed Header */}
        <div className="feed-header">
          <div>
            <h2 className="feed-title">
              {activeTab === 'all' ? 'All Reported Items' : activeTab === 'lost' ? 'Lost Items' : 'Found Items'}
            </h2>
            <span className="feed-count">
              Showing {displayList.length} {displayList.length === 1 ? 'item' : 'items'}
              {searchQuery ? ` matching "${searchQuery}"` : ''}
            </span>
          </div>

          <button className="btn btn-ghost" onClick={fetchItems} title="Refresh Feed">
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        </div>

        {/* Error message */}
        {error && (
          <div className="form-error-banner" style={{ marginBottom: '24px' }}>
            {error}
          </div>
        )}

        {/* Loading Spinner */}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '60px 0' }}>
            <Loader2 size={36} className="animate-spin text-indigo-400" />
          </div>
        )}

        {/* Items Grid */}
        {!loading && (
          <div className="items-grid">
            {displayList.length > 0 ? (
              displayList.map(item => (
                <ItemCard
                  key={`${item._type}-${item.id}`}
                  item={item}
                  type={item._type}
                  onClick={() => {
                    setSelectedItem(item);
                    setSelectedItemType(item._type);
                  }}
                />
              ))
            ) : (
              <div className="empty-feed">
                <Search size={40} className="text-slate-500" style={{ margin: '0 auto 12px' }} />
                <h3>No Items Found</h3>
                <p>
                  {searchQuery
                    ? `No items match your search for "${searchQuery}". Try a different keyword.`
                    : activeTab === 'lost'
                    ? 'No lost items have been reported yet.'
                    : activeTab === 'found'
                    ? 'No found items have been reported yet.'
                    : 'The portal feed is currently empty.'}
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => handleOpenReport(activeTab === 'found' ? 'FOUND' : 'LOST')}
                >
                  <PlusCircle size={16} />
                  <span>Report the First Item</span>
                </button>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Item Detail Modal */}
      {selectedItem && (
        <ItemModal
          item={selectedItem}
          type={selectedItemType}
          onClose={() => setSelectedItem(null)}
        />
      )}

      {/* Report Modal */}
      {isReportOpen && (
        <ReportModal
          defaultType={reportDefaultType}
          onClose={() => setIsReportOpen(false)}
          onRequireAuth={() => {
            setIsReportOpen(false);
            setIsAuthOpen(true);
          }}
          onSuccess={(type) => {
            fetchItems();
            showToast(`${type === 'LOST' ? 'Lost' : 'Found'} item reported successfully!`);
          }}
        />
      )}

      {/* Auth Modal */}
      {isAuthOpen && (
        <AuthModal
          onClose={() => setIsAuthOpen(false)}
          onSuccess={(user) => {
            setCurrentUser(user);
            showToast(`Welcome, ${user.username || 'User'}!`);
          }}
        />
      )}

      {/* Footer */}
      <footer className="app-footer">
        <p>Lostoria — Lost & Found Portal © {new Date().getFullYear()}. Built with Spring Boot 3 & React.</p>
      </footer>
    </div>
  );
}

export default App;

