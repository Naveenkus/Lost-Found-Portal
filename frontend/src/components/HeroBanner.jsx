import React from 'react';
import { AlertCircle, CheckCircle2, ShieldCheck, Sparkles } from 'lucide-react';

export default function HeroBanner({ totalLost, totalFound, onReportLost, onReportFound }) {
  return (
    <div className="hero-banner">
      <div className="hero-glow"></div>
      <div className="hero-content">
        <div className="hero-pill">
          <Sparkles size={14} className="text-indigo-400" />
          <span>Centralized Lost & Found Recovery Platform</span>
        </div>

        <h1 className="hero-heading">
          Lost Something? <span className="gradient-text">Found Something?</span>
        </h1>
        <p className="hero-subtext">
          Reuniting people with their lost belongings quickly, securely, and seamlessly through community reporting.
        </p>

        <div className="hero-stats-row">
          <div className="stat-pill stat-lost" onClick={onReportLost}>
            <div className="stat-icon-wrapper">
              <AlertCircle size={20} />
            </div>
            <div className="stat-info">
              <span className="stat-count">{totalLost}</span>
              <span className="stat-label">Active Lost Reports</span>
            </div>
          </div>

          <div className="stat-pill stat-found" onClick={onReportFound}>
            <div className="stat-icon-wrapper">
              <CheckCircle2 size={20} />
            </div>
            <div className="stat-info">
              <span className="stat-count">{totalFound}</span>
              <span className="stat-label">Reported Found Items</span>
            </div>
          </div>

          <div className="stat-pill stat-total">
            <div className="stat-icon-wrapper">
              <ShieldCheck size={20} />
            </div>
            <div className="stat-info">
              <span className="stat-count">{totalLost + totalFound}</span>
              <span className="stat-label">Total Portal Postings</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}