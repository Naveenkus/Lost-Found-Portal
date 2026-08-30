import React, { useState } from 'react';
import { X, MapPin, Calendar, Image as ImageIcon, ShieldCheck, Tag, Trash2, Loader2 } from 'lucide-react';
import { api } from '../api';

export default function ItemModal({ item, type, currentUser, onClose, onDeleteSuccess }) {
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  if (!item) return null;

  const isLost = type === 'LOST';

  let imageId = null;
  if (item.images) {
    if (Array.isArray(item.images) && item.images.length > 0) {
      imageId = item.images[0].imageID;
    } else if (typeof item.images === 'object' && item.images.imageID) {
      imageId = item.images.imageID;
    }
  }

  const imageUrl = imageId ? api.getImageUrl(imageId) : null;
  const dateValue = isLost ? (item.datelost || item.createdAt) : (item.dateFound || item.createdAt);
  const locationText = isLost ? item.locationLost : item.locationFound;

  // Ownership & Admin check
  const isOwner = Boolean(
    currentUser &&
    item.reportedBy &&
    currentUser.id === item.reportedBy.id
  );
  const isAdmin = Boolean(currentUser && currentUser.role === 'ADMIN');
  const canDelete = isOwner || isAdmin;

  const handleDelete = async () => {
    const confirmed = window.confirm(`Are you sure you want to delete "${item.title}"? This action cannot be undone.`);
    if (!confirmed) return;

    setDeleting(true);
    setDeleteError('');
    try {
      if (isLost) {
        await api.deleteLostItem(item.id);
      } else {
        await api.deleteFoundItem(item.id);
      }
      if (onDeleteSuccess) {
        onDeleteSuccess(item.id, type);
      }
    } catch (err) {
      console.error(err);
      setDeleteError(err.message || 'Failed to delete item. Please try again.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content item-modal-card" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close-btn" onClick={onClose}>
          <X size={20} />
        </button>

        <div className="item-modal-layout">
          {/* Image Showcase */}
          <div className="item-modal-media">
            {imageUrl ? (
              <img
                src={imageUrl}
                alt={item.title}
                className="modal-preview-img"
              />
            ) : (
              <div className="modal-no-img">
                <ImageIcon size={48} className="text-slate-500" />
                <span>No Photo Uploaded</span>
              </div>
            )}
          </div>

          {/* Details Column */}
          <div className="item-modal-details">
            <div className="modal-header-badges">
              <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
                {isLost ? 'LOST ITEM' : 'FOUND ITEM'}
              </span>
              <span className="badge badge-status">
                {item.status || (isLost ? 'UNRESOLVED' : 'AVAILABLE')}
              </span>
            </div>

            <h2 className="modal-item-title">{item.title}</h2>

            <div className="item-metadata-list">
              <div className="meta-item">
                <MapPin size={18} className="meta-item-icon text-indigo-400" />
                <div>
                  <span className="meta-label">Reported Location</span>
                  <p className="meta-value">{locationText || 'Not specified'}</p>
                </div>
              </div>

              <div className="meta-item">
                <Calendar size={18} className="meta-item-icon text-emerald-400" />
                <div>
                  <span className="meta-label">Date Recorded</span>
                  <p className="meta-value">
                    {dateValue ? new Date(dateValue).toLocaleString() : 'N/A'}
                  </p>
                </div>
              </div>

              <div className="meta-item">
                <Tag size={18} className="meta-item-icon text-rose-400" />
                <div>
                  <span className="meta-label">Item Reference ID</span>
                  <p className="meta-value">#{item.id}</p>
                </div>
              </div>
            </div>

            <div className="modal-item-description">
              <h3>Description & Notes</h3>
              <p>{item.description || 'No specific notes or additional descriptions provided by the reporter.'}</p>
            </div>

            <div className="modal-claim-box">
              <ShieldCheck size={20} className="text-emerald-400 shrink-0" />
              <p>
                To claim or submit proof of ownership for this item, please visit the campus security office or contact the portal administrator with Reference #{item.id}.
              </p>
            </div>

            {/* Owner / Admin Action Controls */}
            {canDelete && (
              <div className="modal-actions" style={{ marginTop: '20px', display: 'flex', gap: '12px', alignItems: 'center' }}>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleDelete}
                  disabled={deleting}
                >
                  {deleting ? <Loader2 size={16} className="animate-spin" /> : <Trash2 size={16} />}
                  <span>{deleting ? 'Deleting...' : 'Delete Item'}</span>
                </button>
              </div>
            )}

            {deleteError && (
              <div className="form-error-banner" style={{ marginTop: '16px' }}>
                {deleteError}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}