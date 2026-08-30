import React, { useState } from 'react';
import { X, Loader2, Edit3, AlertCircle, CheckCircle2 } from 'lucide-react';
import { api, getAuthToken } from '../api';

export default function EditModal({ item, type, onClose, onSuccess, onRequireAuth }) {
  const isLost = type === 'LOST';

  // Initialize date from existing item without timezone/UTC shift
  const initialRawDate = isLost ? (item.datelost || item.createdAt) : (item.dateFound || item.createdAt);
  const initialDateStr = initialRawDate
    ? String(initialRawDate).slice(0, 16)
    : new Date().toISOString().slice(0, 16);

  const [title, setTitle] = useState(item.title || '');
  const [location, setLocation] = useState(isLost ? (item.locationLost || '') : (item.locationFound || ''));
  const [status, setStatus] = useState(item.status || (isLost ? 'LOST' : 'FOUND'));
  const [date, setDate] = useState(initialDateStr);
  const [description, setDescription] = useState(item.description || '');
  const [imageUrl, setImageUrl] = useState(item.imageUrl || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!getAuthToken()) {
      onRequireAuth();
      return;
    }

    if (!title.trim()) {
      setError('Please enter an item title');
      return;
    }
    if (!location.trim()) {
      setError('Please enter the location');
      return;
    }

    setLoading(true);
    try {
      const formattedDate = date ? (date.length === 16 ? `${date}:00` : date) : new Date().toISOString().slice(0, 19);

      if (isLost) {
        const payload = {
          title: title.trim(),
          description: description.trim(),
          locationLost: location.trim(),
          status: status.trim(),
          datelost: formattedDate,
        };
        await api.updateLostItem(item.id, payload);
        onSuccess({
          ...item,
          ...payload,
        }, 'LOST');
      } else {
        const payload = {
          title: title.trim(),
          description: description.trim(),
          locationFound: location.trim(),
          imageUrl: imageUrl.trim(),
          status: status.trim(),
          dateFound: formattedDate,
        };
        await api.updateFoundItem(item.id, payload);
        onSuccess({
          ...item,
          ...payload,
        }, 'FOUND');
      }
      onClose();
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to update item. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content report-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close-btn" onClick={onClose}>
          <X size={20} />
        </button>

        <div className="report-modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <Edit3 size={22} className="text-indigo-400" />
            <h2 style={{ margin: 0 }}>Edit {isLost ? 'Lost Item' : 'Found Item'}</h2>
          </div>
          <p>Update details, location, date, or status for Reference #{item.id}.</p>
        </div>

        {error && <div className="form-error-banner">{error}</div>}

        <form onSubmit={handleSubmit} className="report-form">
          <div className="form-group">
            <label className="form-label">Item Title *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g., Space Gray MacBook Pro, Brown Leather Wallet..."
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">
                {isLost ? 'Location Lost *' : 'Location Found *'}
              </label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g., Main Library Floor 2, Cafeteria..."
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Date & Time *</label>
              <input
                type="datetime-local"
                className="form-input"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Status *</label>
            <select
              className="form-input"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              required
            >
              {isLost ? (
                <>
                  <option value="LOST">LOST</option>
                  <option value="CLAIMED">CLAIMED</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </>
              ) : (
                <>
                  <option value="FOUND">FOUND</option>
                  <option value="CLAIMED">CLAIMED</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="RETURNED">RETURNED</option>
                </>
              )}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Detailed Description</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Mention brand, color, unique marks, contents, or circumstances..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  <span>Saving Changes...</span>
                </>
              ) : (
                <span>Save Changes</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
