import React from 'react';
import { MapPin, Calendar, Image as ImageIcon, ArrowRight } from 'lucide-react';
import { api } from '../api';

export default function ItemCard({ item, type, onClick }) {
  const isLost = type === 'LOST';

  // Resolve image name
  let imageName = null;
  if (item.images) {
    if (Array.isArray(item.images) && item.images.length > 0) {
      imageName = item.images[0].imageName;
    } else if (typeof item.images === 'object' && item.images.imageName) {
      imageName = item.images.imageName;
    }
  }

  const imageUrl = imageName ? api.getImageUrl(imageName) : null;
  const dateValue = isLost ? (item.datelost || item.createdAt) : (item.dateFound || item.createdAt);
  
  const formattedDate = dateValue 
    ? new Date(dateValue).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    : 'Recently';

  const locationText = isLost ? item.locationLost : item.locationFound;

  return (
    <div className={`item-card ${isLost ? 'card-lost' : 'card-found'}`} onClick={onClick}>
      <div className="card-image-container">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={item.title}
            className="card-img"
            loading="lazy"
            onError={(e) => {
              e.target.style.display = 'none';
              e.target.nextSibling.style.display = 'flex';
            }}
          />
        ) : null}
        <div className={`card-img-placeholder ${imageUrl ? 'hidden' : 'flex'}`}>
          <ImageIcon size={32} className="text-slate-500" />
          <span className="placeholder-text">No Photo Attached</span>
        </div>

        <div className="card-badges">
          <span className={`badge ${isLost ? 'badge-lost' : 'badge-found'}`}>
            {isLost ? 'LOST' : 'FOUND'}
          </span>
          {item.status && item.status !== 'LOST' && item.status !== 'FOUND' && (
            <span className="badge badge-status">{item.status}</span>
          )}
        </div>
      </div>

      <div className="card-body">
        <h3 className="card-title" title={item.title}>
          {item.title}
        </h3>

        <p className="card-description">
          {item.description || 'No additional description provided.'}
        </p>

        <div className="card-meta">
          <div className="meta-row">
            <MapPin size={14} className="meta-icon" />
            <span className="meta-text">{locationText || 'Location not specified'}</span>
          </div>
          <div className="meta-row">
            <Calendar size={14} className="meta-icon" />
            <span className="meta-text">{formattedDate}</span>
          </div>
        </div>

        <div className="card-footer">
          <span className="view-details-link">
            View Details <ArrowRight size={14} />
          </span>
        </div>
      </div>
    </div>
  );
}