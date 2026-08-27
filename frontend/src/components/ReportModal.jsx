import React, { useState } from 'react';
import { X, UploadCloud, AlertCircle, CheckCircle2, Loader2, Image as ImageIcon } from 'lucide-react';
import { api, getAuthToken } from '../api';

export default function ReportModal({ defaultType = 'LOST', onClose, onSuccess, onRequireAuth }) {
  const [reportType, setReportType] = useState(defaultType); // 'LOST' or 'FOUND'
  const [title, setTitle] = useState('');
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 16));
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('Image file size must be less than 5MB');
        return;
      }
      setImageFile(file);
      setImagePreview(URL.createObjectURL(file));
      setError('');
    }
  };

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
      const formData = new FormData();
      formData.append('title', title.trim());
      formData.append('description', description.trim());
      formData.append('status', reportType);

      if (reportType === 'LOST') {
        formData.append('locationLost', location.trim());
        formData.append('datelost', new Date(date).toISOString().slice(0, 19));
      } else {
        formData.append('locationFound', location.trim());
        formData.append('dateFound', new Date(date).toISOString().slice(0, 19));
      }

      if (imageFile) {
        formData.append('image', imageFile);
      }

      if (reportType === 'LOST') {
        await api.createLostItem(formData);
      } else {
        await api.createFoundItem(formData);
      }

      onSuccess(reportType);
      onClose();
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to submit report. Please try again.');
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
          <h2>Report an Item</h2>
          <p>Provide accurate details to help find or return the item quickly.</p>
        </div>

        {/* Type Toggle */}
        <div className="type-toggle-wrapper">
          <button
            type="button"
            className={`type-toggle-btn ${reportType === 'LOST' ? 'active-lost' : ''}`}
            onClick={() => setReportType('LOST')}
          >
            <AlertCircle size={18} />
            <span>I Lost an Item</span>
          </button>
          <button
            type="button"
            className={`type-toggle-btn ${reportType === 'FOUND' ? 'active-found' : ''}`}
            onClick={() => setReportType('FOUND')}
          >
            <CheckCircle2 size={18} />
            <span>I Found an Item</span>
          </button>
        </div>

        {error && <div className="form-error-banner">{error}</div>}

        <form onSubmit={handleSubmit} className="report-form">
          <div className="form-group">
            <label className="form-label">Item Name / Title *</label>
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
                {reportType === 'LOST' ? 'Location Lost *' : 'Location Found *'}
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
            <label className="form-label">Detailed Description</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Mention brand, color, unique marks, contents, or circumstances..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          {/* Image Upload Box */}
          <div className="form-group">
            <label className="form-label">Upload Photo (Optional, Max 5MB)</label>
            <div className="upload-dropzone">
              {imagePreview ? (
                <div className="preview-container">
                  <img src={imagePreview} alt="Preview" className="upload-preview-img" />
                  <button
                    type="button"
                    className="remove-img-btn"
                    onClick={() => {
                      setImageFile(null);
                      setImagePreview(null);
                    }}
                  >
                    <X size={16} /> Remove
                  </button>
                </div>
              ) : (
                <label className="upload-label">
                  <UploadCloud size={32} className="text-indigo-400" />
                  <span className="upload-instruction">
                    Click to select an image from your device
                  </span>
                  <span className="upload-hint">PNG, JPG, JPEG up to 5MB</span>
                  <input
                    type="file"
                    accept="image/*"
                    className="hidden-file-input"
                    onChange={handleImageChange}
                  />
                </label>
              )}
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button
              type="submit"
              className={`btn ${reportType === 'LOST' ? 'btn-danger' : 'btn-success'}`}
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  <span>Submitting...</span>
                </>
              ) : (
                <span>Submit {reportType === 'LOST' ? 'Lost Report' : 'Found Report'}</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}