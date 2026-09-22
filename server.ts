import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const APK_PATH = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
  <title>Get Taxi Meter</title>
  <meta name="description" content="Professional native Android taxi meter with persistent foreground location service, robust GPS filtering, dynamic waiting/moving fare calculation, trip recovery, and offline support.">
  <meta property="og:title" content="Get Taxi Meter">
  <meta property="og:description" content="Professional native Android taxi meter with persistent foreground location service, robust GPS filtering, dynamic waiting/moving fare calculation, trip recovery, and offline support.">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@500;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --brand-red: #E51E25;
      --brand-red-dark: #C6181E;
      --brand-red-light: #FEE2E2;
      --brand-black: #111827;
      --app-bg: #FFFFFF;
      --card-bg: #FFFFFF;
      --card-border: #E5E7EB;
      --meter-green: #10B981;
      --meter-amber: #F59E0B;
      --text-main: #111827;
      --text-sec: #6B7280;
      --text-muted: #9CA3AF;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
    }
    html, body {
      min-height: 100%;
      height: 100%;
      height: 100dvh;
      overflow-x: hidden;
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
      background-color: #E2E8F0;
      color: var(--text-main);
      display: flex;
      justify-content: center;
      align-items: stretch;
    }
    .app-viewport {
      width: 100%;
      max-width: 480px;
      min-height: 100%;
      min-height: 100dvh;
      background: #FFFFFF;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      box-shadow: 0 4px 24px rgba(0,0,0,0.08);
      overflow-y: auto;
      overflow-x: hidden;
    }

    /* 1. Header Section with Brand Art, Menu, Share, Settings */
    .header-section {
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 10px 16px 6px 16px;
      flex-shrink: 0;
      position: relative;
    }
    .top-actions-row {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: relative;
      z-index: 10;
      height: 36px;
    }
    .icon-btn {
      background: none;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 38px;
      height: 38px;
      border-radius: 8px;
      color: #000;
      transition: background 0.15s ease;
    }
    .icon-btn:hover { background: #F3F4F6; }
    .top-right-btns {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .share-pill {
      background: #F3F4F6;
      border: 1px solid #E5E7EB;
      font-size: 13.5px;
      font-weight: 700;
      color: #111827;
      cursor: pointer;
      padding: 5px 12px;
      border-radius: 8px;
      transition: all 0.15s ease;
    }
    .share-pill:hover { background: #E5E7EB; }

    /* Center Brand Header: EXACT ORIGINAL UPLOADED GET TAXI METER LOGO */
    .brand-hero-center {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin-top: -2px;
      margin-bottom: 2px;
      flex-shrink: 0;
    }
    .brand-original-logo-img {
      width: 90px;
      height: 90px;
      max-width: 90px;
      max-height: 90px;
      aspect-ratio: 1 / 1;
      object-fit: contain;
      display: block;
      margin: 0 auto;
      filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.08));
      transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .brand-original-logo-img:hover {
      transform: scale(1.02);
    }
    .brand-slogan-pill {
      margin-top: 4px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: #FFFFFF;
      border: 1px solid rgba(229, 30, 37, 0.35);
      border-radius: 999px;
      padding: 2px 12px;
      font-size: 10px;
      font-weight: 700;
      color: #1E293B;
      letter-spacing: 0.3px;
    }
    .brand-slogan-pill .dot {
      color: var(--brand-red);
      font-weight: 900;
      font-size: 11px;
    }

    /* Main Dashboard Body: Fills available space with comfortable padding & breathing room */
    .dashboard-body {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-evenly;
      padding: 6px 16px 12px 16px;
      gap: 10px;
    }

    /* 1. Status Cards Row */
    .status-row {
      display: grid;
      grid-template-columns: 1.15fr 1fr 1.05fr;
      gap: 8px;
      flex-shrink: 0;
    }
    .status-card {
      background: #FFFFFF;
      border: 1.2px solid var(--card-border);
      border-radius: 12px;
      padding: 8px 10px;
      display: flex;
      align-items: center;
      gap: 8px;
      min-height: 48px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.02);
    }
    .status-icon-wrap {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .status-texts {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
    }
    .status-title-green {
      font-size: 10px;
      font-weight: 800;
      color: #10B981;
      letter-spacing: 0.2px;
    }
    .status-sub-acc {
      font-size: 9px;
      font-weight: 600;
      color: var(--text-sec);
    }
    .status-bold-text {
      font-size: 10px;
      font-weight: 800;
      color: #000;
      line-height: 1.2;
    }
    .status-sub-time {
      font-size: 9.5px;
      color: #111827;
      font-weight: 700;
    }

    /* 2. CURRENT FARE Hero Card */
    .fare-card {
      background: #FFFFFF;
      border: 2px solid var(--brand-red);
      border-radius: 16px;
      padding: 12px 16px 14px 16px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      flex-shrink: 0;
      box-shadow: 0 4px 14px rgba(229,30,37,0.06);
    }
    .fare-header-label {
      font-size: 11px;
      font-weight: 800;
      letter-spacing: 1.5px;
      color: var(--text-sec);
    }
    .fare-number {
      font-size: 46px;
      font-weight: 900;
      color: #000000;
      letter-spacing: -1.2px;
      line-height: 1.05;
      margin: 4px 0 8px 0;
      font-family: 'JetBrains Mono', 'Plus Jakarta Sans', monospace;
    }
    .fare-pill {
      background: #F3F4F6;
      border: 1px solid #E5E7EB;
      border-radius: 999px;
      padding: 4px 16px;
      font-size: 11px;
      font-weight: 700;
      color: #374151;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .fare-pill-divider { color: #CBD5E1; }

    /* 3. Metric Tiles Row */
    .metrics-row {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 8px;
      flex-shrink: 0;
    }
    .metric-card {
      background: #FFFFFF;
      border: 1.2px solid var(--card-border);
      border-radius: 12px;
      padding: 10px 6px 8px 6px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      min-height: 74px;
      justify-content: space-between;
      box-shadow: 0 1px 3px rgba(0,0,0,0.02);
    }
    .metric-icon-wrap {
      width: 22px;
      height: 22px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .metric-title {
      font-size: 9.5px;
      font-weight: 800;
      letter-spacing: 0.4px;
      color: var(--text-sec);
    }
    .metric-value {
      font-size: 16px;
      font-weight: 900;
      color: #000000;
      line-height: 1.1;
      font-family: 'JetBrains Mono', monospace;
    }
    .metric-sub {
      font-size: 9px;
      color: var(--text-sec);
      font-weight: 600;
    }

    /* 4. Extra Charges Card */
    .extras-card {
      background: #FFFFFF;
      border: 1.2px solid var(--card-border);
      border-radius: 12px;
      padding: 9px 12px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      flex-shrink: 0;
      box-shadow: 0 1px 3px rgba(0,0,0,0.02);
    }
    .extras-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 10.5px;
      font-weight: 800;
    }
    .extras-title { color: #000; letter-spacing: 0.4px; }
    .extras-total { color: #000; }
    .extras-grid {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr;
      gap: 6px;
    }
    .extra-item-btn {
      background: #FFFFFF;
      border: 1px solid #E5E7EB;
      border-radius: 8px;
      padding: 6px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      cursor: pointer;
      min-height: 36px;
      transition: all 0.15s ease;
    }
    .extra-item-btn:hover {
      border-color: var(--brand-red);
      background: #FEF2F2;
      transform: translateY(-1px);
    }
    .extra-texts {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      line-height: 1.15;
    }
    .extra-name {
      font-size: 10px;
      font-weight: 800;
      color: #000;
    }
    .extra-price {
      font-size: 9px;
      font-weight: 800;
      color: #000;
    }

    /* 5. Main Action Button (START TRIP / END TRIP) */
    .main-action-btn {
      width: 100%;
      height: 48px;
      background: var(--brand-red);
      color: #FFFFFF;
      border: none;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 900;
      letter-spacing: 0.8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      box-shadow: 0 4px 12px rgba(229, 30, 37, 0.3);
      flex-shrink: 0;
      transition: all 0.15s ease;
    }
    .main-action-btn:hover {
      background: var(--brand-red-dark);
      box-shadow: 0 6px 16px rgba(229, 30, 37, 0.35);
    }
    .main-action-btn:active { transform: scale(0.985); }

    /* 6. Bottom 4 Quick Action Navigation Cards */
    .quick-actions-row {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr;
      gap: 6px;
      flex-shrink: 0;
    }
    .qa-card {
      background: #FFFFFF;
      border: 1.2px solid var(--card-border);
      border-radius: 10px;
      padding: 8px 4px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      cursor: pointer;
      min-height: 48px;
      transition: all 0.15s ease;
      box-shadow: 0 1px 3px rgba(0,0,0,0.02);
    }
    .qa-card:hover {
      border-color: var(--brand-red);
      transform: translateY(-1px);
    }
    .qa-icon { color: #000; }
    .qa-title {
      font-size: 9px;
      font-weight: 800;
      color: #000;
      text-align: center;
      line-height: 1.15;
      white-space: nowrap;
    }

    /* 7. Curved Footer */
    .curved-footer-wrap {
      width: 100%;
      flex-shrink: 0;
      position: relative;
      margin-top: 4px;
    }
    .curved-svg {
      width: 100%;
      height: 18px;
      display: block;
    }
    .footer-black-base {
      background: #000000;
      color: #FFFFFF;
      padding: 4px 16px 10px 16px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;
    }
    .footer-brand-row {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .f-get { font-size: 16px; font-weight: 900; color: #FFFFFF; }
    .f-taxi { font-size: 16px; font-weight: 900; color: var(--brand-red); }
    .f-meter-pill {
      background: #0F172A;
      border: 1.2px solid var(--brand-red);
      color: #FFFFFF;
      font-size: 8.5px;
      font-weight: 800;
      letter-spacing: 1.2px;
      padding: 2px 8px;
      border-radius: 999px;
    }
    .footer-tagline {
      font-size: 8px;
      font-weight: 800;
      letter-spacing: 1.5px;
      color: rgba(255,255,255,0.85);
    }

    /* Modal Styling */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.6);
      display: none;
      align-items: center;
      justify-content: center;
      padding: 16px;
      z-index: 100;
    }
    .modal-overlay.open { display: flex; }
    .modal-card {
      background: #FFFFFF;
      border-radius: 14px;
      padding: 16px;
      max-width: 340px;
      width: 100%;
      box-shadow: 0 16px 32px rgba(0,0,0,0.2);
    }
    .receipt-box {
      background: #F9FAFB;
      border: 1px solid #E5E7EB;
      border-radius: 10px;
      padding: 8px 10px;
      margin: 8px 0;
      font-size: 11.5px;
    }
    .receipt-row {
      display: flex;
      justify-content: space-between;
      padding: 2.5px 0;
      color: var(--text-sec);
    }
    .receipt-row.bold {
      color: #000;
      font-weight: 800;
      border-top: 1px solid #E5E7EB;
      padding-top: 6px;
      margin-top: 4px;
      font-size: 14px;
    }
    .receipt-row.bold span:last-child {
      color: var(--brand-red);
      font-family: 'JetBrains Mono', monospace;
      font-size: 16px;
    }
    .modal-btns {
      display: flex;
      gap: 8px;
      margin-top: 10px;
    }
    .modal-btns button {
      flex: 1;
      padding: 8px;
      border-radius: 8px;
      font-weight: 800;
      font-size: 11.5px;
      cursor: pointer;
      border: none;
    }
    .btn-cancel { background: #E5E7EB; color: #111827; }
    .btn-confirm { background: var(--brand-red); color: #FFFFFF; }
  </style>
</head>
<body>
  <div class="app-viewport">
    <!-- Header: Action buttons + EXACT Taxi artwork matching reference screenshot -->
    <div class="header-section">
      <div class="top-actions-row">
        <button class="icon-btn" onclick="openMenuModal()" title="Menu">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
        </button>
        <div class="top-right-btns">
          <button class="share-pill" onclick="shareApp()">Share</button>
          <button class="icon-btn" onclick="openSettingsModal()" title="Settings">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
          </button>
        </div>
      </div>

      <!-- Centered EXACT ORIGINAL UPLOADED GET TAXI METER LOGO -->
      <div class="brand-hero-center">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 1000" class="brand-original-logo-img">
          <defs>
            <style>
              @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@800;900&amp;display=swap');
              .logo-text-get { font-family: 'Montserrat', sans-serif; font-weight: 900; font-size: 195px; fill: #0A0A0A; }
              .logo-text-taxi { font-family: 'Montserrat', sans-serif; font-weight: 900; font-size: 195px; fill: #ED1C24; }
              .logo-text-meter { font-family: 'Montserrat', sans-serif; font-weight: 900; font-size: 68px; fill: #FFFFFF; letter-spacing: 16px; }
              .logo-text-banner { font-family: 'Montserrat', sans-serif; font-weight: 800; font-size: 36px; fill: #FFFFFF; letter-spacing: 6px; }
            </style>
            <filter id="badgeShadow" x="-10%" y="-10%" width="125%" height="125%">
              <feDropShadow dx="0" dy="16" stdDeviation="24" flood-color="#000000" flood-opacity="0.18" />
            </filter>
            <clipPath id="squircleClip">
              <rect x="25" y="25" width="950" height="950" rx="210" ry="210" />
            </clipPath>
            <linearGradient id="redBannerGrad" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#ED1C24" />
              <stop offset="60%" stop-color="#D9141D" />
              <stop offset="100%" stop-color="#BA0C14" />
            </linearGradient>
            <linearGradient id="swooshGrad" x1="0%" y1="100%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#ED1C24" />
              <stop offset="100%" stop-color="#EA1A22" />
            </linearGradient>
          </defs>
          <rect x="25" y="25" width="950" height="950" rx="210" ry="210" fill="#FFFFFF" stroke="#ED1C24" stroke-width="24" />
          <g clip-path="url(#squircleClip)">
            <path d="M 12 375 C 20 250, 115 150, 375 140 C 378 148, 380 156, 380 160 C 180 185, 75 285, 45 420 Z" fill="url(#swooshGrad)" />
            <path d="M 105 325 C 160 235, 235 180, 365 152 L 362 165 C 230 195, 160 250, 112 334 Z" fill="#FFFFFF" />
            <path d="M 15 725 Q 500 645 985 725 L 985 985 L 15 985 Z" fill="url(#redBannerGrad)" />
            <path d="M 15 725 Q 500 645 985 725" fill="none" stroke="#FF6B72" stroke-width="3" opacity="0.6" />
            <path d="M 15 915 Q 500 895 985 915 L 985 985 L 15 985 Z" fill="#9E0A10" opacity="0.4" />
            <path d="M 390 154 L 610 154 L 602 165 L 398 165 Z" fill="#0A0A0A" />
            <path d="M 412 96 L 588 96 Q 598 96 601 106 L 615 145 Q 617 154 606 154 L 394 154 Q 383 154 385 145 L 399 106 Q 402 96 412 96 Z" fill="#FFD100" stroke="#0A0A0A" stroke-width="9" stroke-linejoin="round" />
            <g fill="#0A0A0A">
              <path d="M 436 114 L 466 114 L 466 120 L 454 120 L 454 142 L 448 142 L 448 120 L 436 120 Z" />
              <path d="M 470 142 L 480 114 L 488 114 L 498 142 L 491 142 L 488 134 L 480 134 L 477 142 Z M 481 128 L 487 128 L 484 119 Z" />
              <path d="M 504 114 L 513 128 L 522 114 L 529 114 L 517 130 L 530 142 L 522 142 L 513 131 L 504 142 L 497 142 L 510 128 L 498 114 Z" />
              <path d="M 536 114 L 544 114 L 544 142 L 536 142 Z" />
            </g>
            <path d="M 345 165 C 400 158, 600 158, 655 165 C 685 178, 715 220, 736 250 C 750 246, 785 244, 804 258 C 810 263, 808 274, 796 280 C 775 288, 746 285, 735 278 C 745 305, 775 320, 810 330 C 845 342, 855 365, 842 398 C 830 422, 800 435, 765 442 C 680 455, 320 455, 235 442 C 200 435, 170 422, 158 398 C 145 365, 155 342, 190 330 C 225 320, 255 305, 265 278 C 254 285, 225 288, 204 280 C 192 274, 190 263, 196 258 C 215 244, 250 246, 264 250 C 285 220, 315 178, 345 165 Z" fill="#0A0A0A" />
            <path d="M 358 175 C 410 168, 590 168, 642 175 C 665 186, 692 225, 712 258 C 630 266, 370 266, 288 258 C 308 225, 335 186, 358 175 Z" fill="#1A1C21" />
            <path d="M 382 216 Q 402 196 422 216 L 428 258 L 376 258 Z" fill="#0A0A0A" />
            <path d="M 578 216 Q 598 196 618 216 L 624 258 L 572 258 Z" fill="#0A0A0A" />
            <path d="M 400 270 L 415 345 Q 500 355 585 345 L 600 270" fill="none" stroke="#2B2F38" stroke-width="3" />
            <path d="M 382 342 Q 500 352 618 342 L 610 376 Q 500 390 390 376 Z" fill="#16181D" />
            <path d="M 390 382 Q 500 395 610 382 L 605 398 Q 500 412 395 398 Z" fill="#111317" />
            <path d="M 235 345 C 265 340, 315 352, 342 376 C 315 372, 270 365, 240 352 Z" fill="#FFFFFF" />
            <path d="M 250 365 C 275 363, 310 372, 332 388 C 305 385, 275 380, 255 372 Z" fill="#FFFFFF" />
            <path d="M 338 368 L 358 384 L 348 388 L 330 373 Z" fill="#FFFFFF" />
            <path d="M 765 345 C 735 340, 685 352, 658 376 C 685 372, 730 365, 760 352 Z" fill="#FFFFFF" />
            <path d="M 750 365 C 725 363, 690 372, 668 388 C 695 385, 725 380, 745 372 Z" fill="#FFFFFF" />
            <path d="M 662 368 L 642 384 L 652 388 L 670 373 Z" fill="#FFFFFF" />
            
            <!-- Clean Typography using Montserrat Google Font -->
            <text x="95" y="585" class="logo-text-get">Get</text>
            <text x="500" y="585" class="logo-text-taxi">Taxi</text>

            <!-- Map Pin over Taxi 'i' -->
            <g transform="translate(835, 360)">
              <path d="M 43 0 C 21 0, 3 18, 3 40 C 3 68, 43 105, 43 105 C 43 105, 83 68, 83 40 C 83 18, 65 0, 43 0 Z" fill="#ED1C24" />
              <circle cx="43" cy="40" r="16" fill="#FFFFFF" />
            </g>

            <!-- Black Pill Badge for METER -->
            <g filter="url(#badgeShadow)">
              <rect x="200" y="625" width="600" height="110" rx="55" ry="55" fill="#111318" />
              <!-- Red accent lines beside METER -->
              <rect x="120" y="672" width="65" height="16" rx="8" ry="8" fill="#ED1C24" />
              <rect x="815" y="672" width="65" height="16" rx="8" ry="8" fill="#ED1C24" />
              <text x="500" y="700" class="logo-text-meter" text-anchor="middle">METER</text>
            </g>

            <!-- Bottom Red Banner: DRIVE • TRACK • EARN -->
            <text x="500" y="836" class="logo-text-banner" text-anchor="middle">DRIVE   •   TRACK   •   EARN</text>
          </g>
        </svg>
      </div>
    </div>

    <!-- Main Dashboard Body: Strictly fitted without scroll -->
    <div class="dashboard-body">
      <!-- 1. Three Status Cards -->
      <div class="status-row">
        <!-- GPS Ready -->
        <div class="status-card">
          <div class="status-icon-wrap">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5">
              <circle cx="12" cy="12" r="9"/>
              <circle cx="12" cy="12" r="3.5" fill="#10B981"/>
              <line x1="12" y1="1" x2="12" y2="5"/>
              <line x1="12" y1="19" x2="12" y2="23"/>
              <line x1="1" y1="12" x2="5" y2="12"/>
              <line x1="19" y1="12" x2="23" y2="12"/>
            </svg>
          </div>
          <div class="status-texts">
            <span class="status-title-green">GPS READY</span>
            <span class="status-sub-acc">Accuracy ±4 m</span>
          </div>
        </div>

        <!-- Driving / Ready to Drive -->
        <div class="status-card">
          <div class="status-icon-wrap">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style="color: #000;">
              <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.04 3H5.81l1.04-3zM19 17H5v-4.66l.12-.34h13.77l.11.34V17z"/>
              <circle cx="7.5" cy="14.5" r="1.5"/>
              <circle cx="16.5" cy="14.5" r="1.5"/>
            </svg>
          </div>
          <div class="status-texts">
            <span class="status-bold-text" id="statusMotionText">READY<br>TO DRIVE</span>
          </div>
        </div>

        <!-- Date & Time -->
        <div class="status-card">
          <div class="status-icon-wrap">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#000" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="status-texts">
            <span class="status-bold-text" id="statusDateText">20 Sep 2026</span>
            <span class="status-sub-time" id="statusTimeText">10:28 AM</span>
          </div>
        </div>
      </div>

      <!-- 2. CURRENT FARE Hero Card -->
      <div class="fare-card">
        <span class="fare-header-label">CURRENT FARE</span>
        <div class="fare-number" id="fareDisplay">₹50.00</div>
        <div class="fare-pill" id="farePill">
          <span>Base ₹50</span>
          <span class="fare-pill-divider">|</span>
          <span>Dist ₹0</span>
          <span class="fare-pill-divider">|</span>
          <span>Wait ₹0</span>
        </div>
      </div>

      <!-- 3. Three Metric Tiles -->
      <div class="metrics-row">
        <!-- Distance -->
        <div class="metric-card">
          <div class="metric-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24">
              <path d="M4 22L8.5 2h7L20 22h-3.5L14 14h-4l-2.5 8H4z" fill="#E51E25"/>
              <rect x="11.2" y="4" width="1.6" height="4" fill="#FFFFFF"/>
              <rect x="11.2" y="11" width="1.6" height="4" fill="#FFFFFF"/>
            </svg>
          </div>
          <span class="metric-title">DISTANCE</span>
          <span class="metric-value" id="distDisplay">0.00 km</span>
          <span class="metric-sub" id="distSub">₹18.0 / km</span>
        </div>

        <!-- Trip Time -->
        <div class="metric-card">
          <div class="metric-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" fill="#E51E25"/>
              <path d="M12 7v5l3 2.5" stroke="#FFFFFF" stroke-width="2.2" stroke-linecap="round" fill="none"/>
            </svg>
          </div>
          <span class="metric-title">TRIP TIME</span>
          <span class="metric-value" id="timeDisplay">00:00:00</span>
          <span class="metric-sub">Total elapsed</span>
        </div>

        <!-- Waiting Time -->
        <div class="metric-card">
          <div class="metric-icon-wrap">
            <svg width="18" height="18" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" fill="#E51E25"/>
              <rect x="8.5" y="7" width="2.4" height="10" rx="1.2" fill="#FFFFFF"/>
              <rect x="13.1" y="7" width="2.4" height="10" rx="1.2" fill="#FFFFFF"/>
            </svg>
          </div>
          <span class="metric-title">WAITING TIME</span>
          <span class="metric-value" id="waitDisplay">00:00:00</span>
          <span class="metric-sub" id="waitSub">₹2.0 / min</span>
        </div>
      </div>

      <!-- 4. Extra Charges Card -->
      <div class="extras-card">
        <div class="extras-header">
          <span class="extras-title">EXTRA CHARGES</span>
          <span class="extras-total" id="extrasTotalLabel">Total: ₹0.00</span>
        </div>
        <div class="extras-grid">
          <!-- Toll -->
          <button class="extra-item-btn" onclick="addExtra('Toll', 50)">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#000" stroke-width="2.2" stroke-linecap="round">
              <path d="M4 21v-4a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v4"/>
              <path d="M3 7h18"/>
              <path d="M6 3v4"/>
              <path d="M18 3v4"/>
            </svg>
            <div class="extra-texts">
              <span class="extra-name">Toll</span>
              <span class="extra-price">+ ₹50</span>
            </div>
          </button>

          <!-- Parking -->
          <button class="extra-item-btn" onclick="addExtra('Parking', 30)">
            <div style="width: 15px; height: 15px; background: #2563EB; border-radius: 3px; display: flex; align-items: center; justify-content: center; color: #FFF; font-size: 10px; font-weight: 900;">P</div>
            <div class="extra-texts">
              <span class="extra-name">Parking</span>
              <span class="extra-price">+ ₹30</span>
            </div>
          </button>

          <!-- Airport -->
          <button class="extra-item-btn" onclick="addExtra('Airport', 100)">
            <div style="width: 15px; height: 15px; background: #0EA5E9; border-radius: 3px; display: flex; align-items: center; justify-content: center; color: #FFF;">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
              </svg>
            </div>
            <div class="extra-texts">
              <span class="extra-name">Airport</span>
              <span class="extra-price">+ ₹100</span>
            </div>
          </button>

          <!-- Custom -->
          <button class="extra-item-btn" onclick="promptCustomExtra()">
            <div style="width: 15px; height: 15px; background: #E51E25; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #FFF;">
              <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
            </div>
            <div class="extra-texts">
              <span class="extra-name">Custom</span>
              <span class="extra-price">Add</span>
            </div>
          </button>
        </div>
      </div>

      <!-- 5. Main Action Button -->
      <button class="main-action-btn" id="mainActionBtn" onclick="handleActionClick()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="#FFFFFF" id="mainBtnIcon">
          <polygon points="6,4 20,12 6,20"></polygon>
        </svg>
        <span id="mainBtnText">START TRIP</span>
      </button>
    </div>

    <!-- 6. Curved Brand Footer -->
    <div class="curved-footer-wrap">
      <svg class="curved-svg" viewBox="0 0 400 16" preserveAspectRatio="none">
        <path d="M0,16 Q200,-2 400,16 Z" fill="#E51E25" />
        <path d="M0,16 Q200,6 400,16 Z" fill="#000000" />
      </svg>
      <div class="footer-black-base">
        <div class="footer-brand-row">
          <span class="f-get">Get</span><span class="f-taxi">Taxi</span>
          <span class="f-meter-pill">— M E T E R —</span>
        </div>
        <div class="footer-tagline">S A F E   R I D E S   •   T R A N S P A R E N T   F A R E S</div>
      </div>
    </div>
  </div>

  <!-- End Trip Confirmation Modal -->
  <div class="modal-overlay" id="endModal">
    <div class="modal-card">
      <h3 style="font-size: 15px; margin-bottom: 3px; color: #000;">End this trip?</h3>
      <p style="color: var(--text-sec); font-size: 11px;">Confirm final fare breakdown before completing.</p>
      
      <div class="receipt-box">
        <div class="receipt-row"><span>Base Fare:</span><span id="mBase">₹50.00</span></div>
        <div class="receipt-row"><span>Distance Fare:</span><span id="mDist">₹0.00</span></div>
        <div class="receipt-row"><span>Waiting Fare:</span><span id="mWait">₹0.00</span></div>
        <div class="receipt-row"><span>Extra Charges:</span><span id="mExtra">₹0.00</span></div>
        <div class="receipt-row bold"><span>TOTAL FARE:</span><span id="mTotal">₹50.00</span></div>
      </div>

      <div class="modal-btns">
        <button class="btn-cancel" onclick="closeEndModal()">Cancel</button>
        <button class="btn-confirm" onclick="confirmEndTrip()">CONFIRM END TRIP</button>
      </div>
    </div>
  </div>

  <!-- Trip Receipt Modal -->
  <div class="modal-overlay" id="receiptModal">
    <div class="modal-card">
      <div style="text-align: center; margin-bottom: 8px;">
        <span style="background: #DCFCE7; color: #16A34A; font-size: 9.5px; font-weight: 800; padding: 2px 10px; border-radius: 999px;">TRIP COMPLETED</span>
        <h3 style="font-size: 16px; color: #000; margin-top: 4px;">Trip Receipt</h3>
      </div>
      
      <div class="receipt-box" id="completedReceiptContent"></div>

      <div class="modal-btns">
        <button class="btn-cancel" onclick="closeReceiptModal()">Close</button>
        <button class="btn-confirm" onclick="shareReceipt()">Share Receipt</button>
      </div>
    </div>
  </div>

  <!-- Settings / Tariff Modal -->
  <div class="modal-overlay" id="settingsModal">
    <div class="modal-card">
      <h3 style="font-size: 15px; color: #000; margin-bottom: 8px;">Tariff & Rates</h3>
      <div style="display: flex; flex-direction: column; gap: 7px; font-size: 11.5px;">
        <div>
          <label style="display: block; font-weight: 700; font-size: 10px; color: var(--text-sec);">BASE FARE (₹)</label>
          <input type="number" id="cfgBaseFare" value="50" style="width: 100%; padding: 5px 7px; border-radius: 6px; border: 1px solid var(--card-border);">
        </div>
        <div>
          <label style="display: block; font-weight: 700; font-size: 10px; color: var(--text-sec);">DISTANCE RATE (₹/km)</label>
          <input type="number" id="cfgDistRate" value="18" style="width: 100%; padding: 5px 7px; border-radius: 6px; border: 1px solid var(--card-border);">
        </div>
        <div>
          <label style="display: block; font-weight: 700; font-size: 10px; color: var(--text-sec);">WAITING RATE (₹/min)</label>
          <input type="number" id="cfgWaitRate" value="2" style="width: 100%; padding: 5px 7px; border-radius: 6px; border: 1px solid var(--card-border);">
        </div>
        <div>
          <label style="display: block; font-weight: 700; font-size: 10px; color: var(--text-sec);">FREE DISTANCE (km)</label>
          <input type="number" id="cfgFreeDist" value="1.5" step="0.1" style="width: 100%; padding: 5px 7px; border-radius: 6px; border: 1px solid var(--card-border);">
        </div>
      </div>
      <div class="modal-btns">
        <button class="btn-cancel" onclick="closeSettingsModal()">Cancel</button>
        <button class="btn-confirm" onclick="saveSettings()">Save Rates</button>
      </div>
    </div>
  </div>

  <!-- History Modal -->
  <div class="modal-overlay" id="historyModal">
    <div class="modal-card">
      <h3 style="font-size: 15px; color: #000; margin-bottom: 8px;">Trip History</h3>
      <div id="historyList" style="max-height: 220px; overflow-y: auto; display: flex; flex-direction: column; gap: 5px;">
        <div style="text-align: center; color: var(--text-muted); padding: 14px 0; font-size: 11.5px;">No completed trips recorded yet in this session.</div>
      </div>
      <div class="modal-btns">
        <button class="btn-confirm" onclick="closeHistoryModal()">Back</button>
      </div>
    </div>
  </div>

  <!-- Today's Summary Modal -->
  <div class="modal-overlay" id="todayModal">
    <div class="modal-card">
      <h3 style="font-size: 15px; color: #000; margin-bottom: 4px;">Today's Summary</h3>
      <p style="color: var(--text-sec); font-size: 11px; margin-bottom: 8px;">Driver shift earnings & performance</p>
      <div class="receipt-box">
        <div class="receipt-row"><span>Trips Completed:</span><strong id="sumTripCount">0</strong></div>
        <div class="receipt-row"><span>Total Distance:</span><strong id="sumTotalDist">0.00 km</strong></div>
        <div class="receipt-row"><span>Total Active Time:</span><strong id="sumTotalTime">00:00:00</strong></div>
        <div class="receipt-row bold"><span>TOTAL REVENUE:</span><span id="sumTotalRev">₹0.00</span></div>
      </div>
      <div class="modal-btns">
        <button class="btn-confirm" onclick="closeTodayModal()">Close</button>
      </div>
    </div>
  </div>

  <!-- More Options Modal -->
  <div class="modal-overlay" id="moreModal">
    <div class="modal-card">
      <h3 style="font-size: 15px; color: #000; margin-bottom: 4px;">Get Taxi Meter Menu</h3>
      <div style="font-size: 11.5px; color: var(--text-sec); line-height: 1.35; display: flex; flex-direction: column; gap: 7px; margin: 8px 0;">
        <div style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0;" onclick="closeMoreModal(); openHistoryModal();">
          <div><strong>Trip History & Receipts</strong><div style="font-size: 10px; color: #9CA3AF;">View logged rides and fare receipts</div></div>
          <span>›</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0;" onclick="closeMoreModal(); openSettingsModal();">
          <div><strong>Tariff & Meter Settings</strong><div style="font-size: 10px; color: #9CA3AF;">Adjust base fare, per km, waiting charges</div></div>
          <span>›</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0;" onclick="closeMoreModal(); openTodayModal();">
          <div><strong>Today's Summary</strong><div style="font-size: 10px; color: #9CA3AF;">Shift earnings, distance and completed trips</div></div>
          <span>›</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; padding: 4px 0;" onclick="closeMoreModal(); shareApp();">
          <div><strong>Share Get Taxi Meter</strong><div style="font-size: 10px; color: #9CA3AF;">Share app with fellow drivers & fleets</div></div>
          <span>›</span>
        </div>
      </div>
      <div class="modal-btns">
        <button class="btn-confirm" onclick="closeMoreModal()">Close</button>
      </div>
    </div>
  </div>

  <script>
    const TARIFF = {
      baseFare: 50.0,
      minFare: 50.0,
      distRate: 18.0,
      waitRate: 2.0,
      freeDistKm: 1.5,
      freeWaitMin: 5.0
    };

    let tripActive = false;
    let isMoving = false;
    let speedKmH = 0.0;
    let totalDistMeters = 0.0;
    let tripDurationSec = 0;
    let waitDurationSec = 0;
    let extras = [];
    let timerInterval = null;
    let pastTrips = [];

    // Live dynamic system clock update matching exact screenshot format
    function updateClock() {
      const now = new Date();
      const optionsDate = { day: '2-digit', month: 'short', year: 'numeric' };
      const dateStr = now.toLocaleDateString('en-US', optionsDate);
      const timeStr = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
      document.getElementById('statusDateText').innerText = dateStr;
      document.getElementById('statusTimeText').innerText = timeStr;
    }
    setInterval(updateClock, 1000);
    updateClock();

    function formatTime(secs) {
      const h = Math.floor(secs / 3600);
      const m = Math.floor((secs % 3600) / 60);
      const s = secs % 60;
      return [h, m, s].map(v => String(v).padStart(2, '0')).join(':');
    }

    function calculateBreakdown() {
      const distKm = totalDistMeters / 1000.0;
      const chDistKm = Math.max(0, distKm - TARIFF.freeDistKm);
      const distFare = chDistKm * TARIFF.distRate;

      const waitMin = waitDurationSec / 60.0;
      const chWaitMin = Math.max(0, waitMin - TARIFF.freeWaitMin);
      const waitFare = chWaitMin * TARIFF.waitRate;

      const meterRaw = TARIFF.baseFare + distFare + waitFare;
      const meterFare = Math.max(TARIFF.minFare, Math.round(meterRaw));
      const extrasTotal = extras.reduce((sum, e) => sum + e.amount, 0);
      const totalFare = meterFare + extrasTotal;

      return { distKm, chDistKm, distFare, waitMin, chWaitMin, waitFare, extrasTotal, totalFare };
    }

    function updateUI() {
      const b = calculateBreakdown();
      document.getElementById('fareDisplay').innerText = '₹' + b.totalFare.toFixed(2);
      
      const extraTxt = b.extrasTotal > 0 ? (' | Extra ₹' + Math.round(b.extrasTotal)) : '';
      document.getElementById('farePill').innerHTML = \`
        <span>Base ₹\${TARIFF.baseFare.toFixed(0)}</span>
        <span class="fare-pill-divider">|</span>
        <span>Dist ₹\${Math.round(b.distFare)}</span>
        <span class="fare-pill-divider">|</span>
        <span>Wait ₹\${Math.round(b.waitFare)}</span>
        \${extraTxt ? '<span class="fare-pill-divider">|</span><span>' + extraTxt.replace(' | ', '') + '</span>' : ''}
      \`;

      document.getElementById('distDisplay').innerText = b.distKm.toFixed(2) + ' km';
      document.getElementById('timeDisplay').innerText = formatTime(tripDurationSec);
      document.getElementById('waitDisplay').innerText = formatTime(waitDurationSec);

      const mStatus = document.getElementById('statusMotionText');
      if (!tripActive) {
        mStatus.innerHTML = 'READY<br>TO DRIVE';
      } else if (isMoving) {
        mStatus.innerHTML = 'DRIVING<br>' + speedKmH.toFixed(0) + ' km/h';
      } else {
        mStatus.innerHTML = 'WAITING<br>STOPPED';
      }

      document.getElementById('extrasTotalLabel').innerText = 'Total: ₹' + b.extrasTotal.toFixed(2);
    }

    function addExtra(label, amount) {
      extras.push({ id: Date.now(), label, amount });
      updateUI();
    }

    function promptCustomExtra() {
      const label = prompt('Extra charge label:', 'Highway Toll') || 'Extra';
      const amtStr = prompt('Amount (₹):', '50');
      const amt = parseFloat(amtStr);
      if (amt && amt > 0) addExtra(label, amt);
    }

    function handleActionClick() {
      if (!tripActive) {
        startTrip();
      } else {
        openEndModal();
      }
    }

    function startTrip() {
      tripActive = true;
      isMoving = true;
      speedKmH = 36.0;
      totalDistMeters = 0.0;
      tripDurationSec = 0;
      waitDurationSec = 0;
      extras = [];

      document.getElementById('mainBtnText').innerText = 'END TRIP';
      document.getElementById('mainBtnIcon').innerHTML = '<rect x="6" y="6" width="12" height="12" fill="#FFFFFF"></rect>';

      if (timerInterval) clearInterval(timerInterval);
      timerInterval = setInterval(() => {
        tripDurationSec++;
        if (isMoving) {
          totalDistMeters += (speedKmH * 1000.0) / 3600.0;
        } else {
          waitDurationSec++;
        }
        updateUI();
      }, 1000);

      updateUI();
    }

    function openEndModal() {
      const b = calculateBreakdown();
      document.getElementById('mBase').innerText = '₹' + TARIFF.baseFare.toFixed(2);
      document.getElementById('mDist').innerText = '₹' + b.distFare.toFixed(2);
      document.getElementById('mWait').innerText = '₹' + b.waitFare.toFixed(2);
      document.getElementById('mExtra').innerText = '₹' + b.extrasTotal.toFixed(2);
      document.getElementById('mTotal').innerText = '₹' + b.totalFare.toFixed(2);
      document.getElementById('endModal').classList.add('open');
    }

    function closeEndModal() {
      document.getElementById('endModal').classList.remove('open');
    }

    function confirmEndTrip() {
      closeEndModal();
      const finalB = calculateBreakdown();
      const savedTrip = {
        id: Date.now(),
        date: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        distKm: finalB.distKm,
        duration: tripDurationSec,
        wait: waitDurationSec,
        fare: finalB.totalFare
      };
      pastTrips.unshift(savedTrip);

      tripActive = false;
      isMoving = false;
      speedKmH = 0.0;
      if (timerInterval) clearInterval(timerInterval);

      document.getElementById('mainBtnText').innerText = 'START TRIP';
      document.getElementById('mainBtnIcon').innerHTML = '<polygon points="6,4 20,12 6,20" fill="#FFFFFF"></polygon>';
      showReceiptModal(savedTrip, finalB);
    }

    function showReceiptModal(trip, b) {
      const container = document.getElementById('completedReceiptContent');
      container.innerHTML = \`
        <div class="receipt-row"><span>Total Distance:</span><strong>\${trip.distKm.toFixed(2)} km</strong></div>
        <div class="receipt-row"><span>Total Trip Time:</span><strong>\${formatTime(trip.duration)}</strong></div>
        <div class="receipt-row"><span>Waiting Time:</span><strong>\${formatTime(trip.wait)}</strong></div>
        <div class="receipt-row"><span>Base Fare:</span><span>₹\${TARIFF.baseFare.toFixed(2)}</span></div>
        <div class="receipt-row"><span>Distance Fare:</span><span>₹\${b.distFare.toFixed(2)}</span></div>
        <div class="receipt-row"><span>Waiting Fare:</span><span>₹\${b.waitFare.toFixed(2)}</span></div>
        \${b.extrasTotal > 0 ? '<div class="receipt-row"><span>Extra Charges:</span><span>₹' + b.extrasTotal.toFixed(2) + '</span></div>' : ''}
        <div class="receipt-row bold"><span>TOTAL FARE:</span><span>₹\${trip.fare.toFixed(2)}</span></div>
      \`;
      document.getElementById('receiptModal').classList.add('open');
    }

    function closeReceiptModal() {
      document.getElementById('receiptModal').classList.remove('open');
      updateUI();
    }

    function shareReceipt() {
      alert('Trip receipt ready to share with passenger!');
      closeReceiptModal();
    }

    function shareApp() {
      if (navigator.share) {
        navigator.share({
          title: 'Get Taxi Meter',
          text: 'Get Taxi Meter - Safe Rides & Transparent Fares',
          url: window.location.href
        }).catch(() => {});
      } else {
        navigator.clipboard.writeText(window.location.href);
        alert('App link copied to clipboard!');
      }
    }

    /* History Modal */
    function openHistoryModal() {
      const list = document.getElementById('historyList');
      if (pastTrips.length === 0) {
        list.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 14px 0; font-size: 11.5px;">No completed trips recorded yet in this session.</div>';
      } else {
        list.innerHTML = pastTrips.map(t => \`
          <div style="display: flex; justify-content: space-between; align-items: center; padding: 7px 9px; background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 8px;">
            <div>
              <div style="font-weight: 800; font-size: 11.5px; color: #000;">Trip at \${t.date}</div>
              <div style="font-size: 9.5px; color: var(--text-sec);">\${t.distKm.toFixed(2)} km • \${formatTime(t.duration)}</div>
            </div>
            <div style="font-weight: 900; font-size: 13px; color: var(--brand-red);">₹\${t.fare.toFixed(2)}</div>
          </div>
        \`).join('');
      }
      document.getElementById('historyModal').classList.add('open');
    }

    function closeHistoryModal() {
      document.getElementById('historyModal').classList.remove('open');
    }

    /* Today's Summary Modal */
    function openTodayModal() {
      const count = pastTrips.length;
      const totalDist = pastTrips.reduce((s, t) => s + t.distKm, 0);
      const totalTime = pastTrips.reduce((s, t) => s + t.duration, 0);
      const totalRev = pastTrips.reduce((s, t) => s + t.fare, 0);

      document.getElementById('sumTripCount').innerText = count;
      document.getElementById('sumTotalDist').innerText = totalDist.toFixed(2) + ' km';
      document.getElementById('sumTotalTime').innerText = formatTime(totalTime);
      document.getElementById('sumTotalRev').innerText = '₹' + totalRev.toFixed(2);
      document.getElementById('todayModal').classList.add('open');
    }

    function closeTodayModal() {
      document.getElementById('todayModal').classList.remove('open');
    }

    /* Settings Modal */
    function openSettingsModal() {
      document.getElementById('cfgBaseFare').value = TARIFF.baseFare;
      document.getElementById('cfgDistRate').value = TARIFF.distRate;
      document.getElementById('cfgWaitRate').value = TARIFF.waitRate;
      document.getElementById('cfgFreeDist').value = TARIFF.freeDistKm;
      document.getElementById('settingsModal').classList.add('open');
    }

    function closeSettingsModal() {
      document.getElementById('settingsModal').classList.remove('open');
    }

    function saveSettings() {
      TARIFF.baseFare = parseFloat(document.getElementById('cfgBaseFare').value) || 50.0;
      TARIFF.distRate = parseFloat(document.getElementById('cfgDistRate').value) || 18.0;
      TARIFF.waitRate = parseFloat(document.getElementById('cfgWaitRate').value) || 2.0;
      TARIFF.freeDistKm = parseFloat(document.getElementById('cfgFreeDist').value) || 1.5;

      document.getElementById('distSub').innerText = '₹' + TARIFF.distRate.toFixed(1) + ' / km';
      document.getElementById('waitSub').innerText = '₹' + TARIFF.waitRate.toFixed(1) + ' / min';
      closeSettingsModal();
      updateUI();
    }

    function openMoreModal() {
      document.getElementById('moreModal').classList.add('open');
    }

    function closeMoreModal() {
      document.getElementById('moreModal').classList.remove('open');
    }

    function openMenuModal() {
      openMoreModal();
    }

    updateUI();
  </script>
</body>
</html>`;

const server = http.createServer((req, res) => {
  try {
    const host = req.headers.host || `localhost:${PORT}`;
    let pathname = '/';
    try {
      const parsedUrl = new URL(req.url || '/', `http://${host}`);
      pathname = parsedUrl.pathname;
    } catch {
      pathname = req.url || '/';
    }

    // Handle native Android APK download
    if (pathname === '/app-debug.apk' || pathname === '/download/apk' || pathname === '/download-apk') {
      if (fs.existsSync(APK_PATH)) {
        const stat = fs.statSync(APK_PATH);
        res.writeHead(200, {
          'Content-Type': 'application/vnd.android.package-archive',
          'Content-Length': stat.size,
          'Content-Disposition': 'attachment; filename="GetTaxiMeter-debug.apk"'
        });
        const stream = fs.createReadStream(APK_PATH);
        stream.pipe(res);
        return;
      } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('APK not found. Please compile the app first.');
        return;
      }
    }

    // Health and API routes
    if (pathname === '/api/status' || pathname === '/health') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        app: 'Get Taxi Meter',
        status: 'healthy',
        port: PORT,
        apkReady: fs.existsSync(APK_PATH),
        apkPath: APK_PATH
      }));
      return;
    }

    // Brand icon / logo SVG assets
    if (pathname.endsWith('.svg')) {
      const fileName = path.basename(pathname);
      const iconPath = path.join(__dirname, 'public', fileName);
      if (fs.existsSync(iconPath)) {
        res.writeHead(200, {
          'Content-Type': 'image/svg+xml',
          'Cache-Control': 'public, max-age=86400'
        });
        fs.createReadStream(iconPath).pipe(res);
        return;
      }
    }

    // Default: Serve the interactive Get Taxi Meter application
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    res.end(htmlContent);
  } catch (err) {
    console.error('Request handler error:', err);
    try {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('Internal Server Error');
    } catch {}
  }
});

server.on('error', (err) => {
  console.error('Server listener error:', err);
});

process.on('uncaughtException', (err) => {
  console.error('Uncaught exception in server process:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled rejection in server process:', reason);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Get Taxi Meter dev server listening on port ${PORT}`);
});

