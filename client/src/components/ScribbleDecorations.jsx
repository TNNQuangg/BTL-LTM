import { useEffect, useState, useMemo } from 'react';

/* ═══════════════════════════════════════════════
   Scribble It! — Decoration Components
   Hand-drawn SVG elements for doodle UI
   ═══════════════════════════════════════════════ */

/**
 * ScribbleCircles — 25-30 random empty circles scattered across the viewport
 * Creates that "sketchbook / draft paper" feel
 */
export function ScribbleCircles({ count = 28 }) {
  const circles = useMemo(() => {
    return Array.from({ length: count }, (_, i) => ({
      id: i,
      cx: Math.random() * 100,
      cy: Math.random() * 100,
      r: 3 + Math.random() * 8,
      opacity: 0.06 + Math.random() * 0.08,
      strokeWidth: 1 + Math.random() * 1,
    }));
  }, [count]);

  return (
    <div className="bg-circles">
      <svg
        width="100%"
        height="100%"
        style={{ position: 'absolute', inset: 0 }}
        xmlns="http://www.w3.org/2000/svg"
      >
        {circles.map((c) => (
          <circle
            key={c.id}
            cx={`${c.cx}%`}
            cy={`${c.cy}%`}
            r={c.r}
            fill="none"
            stroke="#1A1A1A"
            strokeWidth={c.strokeWidth}
            opacity={c.opacity}
          />
        ))}
      </svg>
    </div>
  );
}

/**
 * BrushStrokeLogo — Paint brush stroke behind logo text
 * Black asymmetric brush stroke with mustard yellow text on top
 */
export function BrushStrokeLogo({ text = "SCRIBBLE IT!", fontSize = '2rem', style = {} }) {
  return (
    <div style={{ position: 'relative', display: 'inline-flex', alignItems: 'center', gap: 12, ...style }}>
      {/* Paintbrush icon */}
      <svg width="36" height="36" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ transform: 'rotate(-15deg)' }}>
        <path d="M6 30 L14 22 L20 16 L26 10 L30 6" stroke="#1A1A1A" strokeWidth="2.5" strokeLinecap="round" fill="none"/>
        <path d="M26 10 C28 8, 32 6, 30 6 C28 6, 26 8, 26 10Z" fill="#1A1A1A"/>
        <path d="M6 30 C4 32, 3 33, 5 33 C7 33, 8 32, 6 30Z" fill="#1A1A1A"/>
        <rect x="13" y="17" width="10" height="4" rx="1" transform="rotate(-45 18 19)" fill="#F5B800"/>
      </svg>

      {/* Brush stroke background + text */}
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <svg
          viewBox="0 0 260 55"
          style={{
            position: 'absolute',
            inset: '-8px -16px',
            width: 'calc(100% + 32px)',
            height: 'calc(100% + 16px)',
            zIndex: 0,
          }}
          preserveAspectRatio="none"
        >
          <path
            d="M8 10 C4 8, 2 18, 6 25 C3 30, 2 40, 12 45 C30 52, 80 50, 130 48 C180 46, 230 50, 248 44 C258 38, 260 28, 254 20 C250 12, 240 6, 200 8 C160 5, 60 4, 20 8 Z"
            fill="#1A1A1A"
          />
        </svg>
        <span style={{
          position: 'relative',
          zIndex: 1,
          fontFamily: "var(--font-logo)",
          fontSize: fontSize,
          color: '#F5B800',
          letterSpacing: '3px',
          whiteSpace: 'nowrap',
          padding: '4px 8px',
        }}>
          {text}
        </span>
      </div>
    </div>
  );
}

/**
 * SpeechBubble — Hand-drawn speech bubble with tail pointing left
 */
export function SpeechBubble({ children, tailDirection = 'left', style = {} }) {
  return (
    <div style={{ position: 'relative', ...style }}>
      <div style={{
        background: 'var(--bg-panel)',
        border: '3px solid var(--ink)',
        borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px',
        padding: '24px 28px',
        position: 'relative',
      }}>
        {children}
      </div>
      {/* Tail */}
      <svg
        width="30"
        height="24"
        viewBox="0 0 30 24"
        style={{
          position: 'absolute',
          ...(tailDirection === 'left' ? { left: -25, top: 30 } : { right: -25, top: 30 }),
          transform: tailDirection === 'right' ? 'scaleX(-1)' : 'none',
        }}
      >
        <path d="M28 0 C20 4, 4 8, 0 22 C4 16, 12 12, 28 10 Z" fill="var(--bg-panel)" stroke="#1A1A1A" strokeWidth="3"/>
      </svg>
    </div>
  );
}

/**
 * WobblyBox — A div with a hand-drawn wobbly SVG border drawn around it
 */
export function WobblyBox({ children, style = {}, className = '', onClick, onContextMenu, onMouseEnter, onMouseLeave }) {
  return (
    <div
      className={className}
      onClick={onClick}
      onContextMenu={onContextMenu}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      style={{
        background: 'var(--bg-panel)',
        border: '3px solid var(--ink)',
        borderRadius: '255px 15px 225px 15px / 15px 225px 15px 255px',
        position: 'relative',
        boxShadow: 'none',
        ...style,
      }}
    >
      {children}
    </div>
  );
}

/**
 * DoodleIcon — Simple hand-drawn doodle icons (SVG inline)
 */
export function DoodleIcon({ type, size = 24, color = '#1A1A1A' }) {
  const icons = {
    pencil: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
        <path d="M3 21 L5 13 L16 2 C18 0, 22 0, 22 2 C22 4, 20 6, 18 8 L7 19 Z"/>
        <path d="M14 4 L20 10"/>
        <path d="M5 13 L11 19"/>
      </svg>
    ),
    marker: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
        <rect x="7" y="2" width="10" height="6" rx="1"/>
        <path d="M9 8 L9 18 L12 22 L15 18 L15 8"/>
      </svg>
    ),
    tape: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
        <circle cx="12" cy="12" r="9"/>
        <circle cx="12" cy="12" r="3"/>
        <path d="M21 12 L24 12"/>
      </svg>
    ),
    star: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2 L15 9 L22 9 L16 14 L18 21 L12 17 L6 21 L8 14 L2 9 L9 9 Z"/>
      </svg>
    ),
    paint: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
        <path d="M2 22 C2 16, 8 14, 12 10 C14 8, 18 4, 20 2"/>
        <circle cx="20" cy="4" r="3" fill={color}/>
      </svg>
    ),
    hatch: (
      <svg width={size} height={size} viewBox="0 0 24 24" stroke={color} strokeWidth="1.5">
        <line x1="2" y1="2" x2="22" y2="22"/>
        <line x1="8" y1="2" x2="22" y2="16"/>
        <line x1="14" y1="2" x2="22" y2="10"/>
        <line x1="2" y1="8" x2="16" y2="22"/>
        <line x1="2" y1="14" x2="10" y2="22"/>
      </svg>
    ),
  };

  return icons[type] || null;
}

/**
 * MascotSimon — Simple line-art character (standing, pointing up, with bow tie)
 * Minimalist wireframe mascot
 */
export function MascotSimon({ size = 200 }) {
  const scale = size / 200;
  return (
    <div className="mascot-container" style={{ width: size, height: size * 1.6 }}>
      <svg
        width={size}
        height={size * 1.6}
        viewBox="0 0 200 320"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ overflow: 'visible' }}
      >
        {/* Head */}
        <circle cx="100" cy="55" r="40" stroke="#1A1A1A" strokeWidth="3" fill="none"/>
        {/* Eyes */}
        <circle cx="85" cy="48" r="4" fill="#1A1A1A"/>
        <circle cx="115" cy="48" r="4" fill="#1A1A1A"/>
        {/* Smile */}
        <path d="M85 65 Q100 78, 115 65" stroke="#1A1A1A" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
        {/* Bow tie */}
        <path d="M90 95 L100 102 L110 95 L100 108 Z" stroke="#1A1A1A" strokeWidth="2" fill="none"/>
        {/* Body */}
        <line x1="100" y1="95" x2="100" y2="200" stroke="#1A1A1A" strokeWidth="3"/>
        {/* Left arm (resting) */}
        <path d="M100 130 L55 170" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
        {/* Right arm (pointing up with index finger) */}
        <path d="M100 130 L140 100 L145 70" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
        {/* Index finger */}
        <line x1="145" y1="70" x2="147" y2="55" stroke="#1A1A1A" strokeWidth="2.5" strokeLinecap="round"/>
        {/* Left leg */}
        <path d="M100 200 L70 280" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
        {/* Right leg */}
        <path d="M100 200 L130 280" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
        {/* Left foot */}
        <path d="M70 280 L55 285" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
        {/* Right foot */}
        <path d="M130 280 L145 285" stroke="#1A1A1A" strokeWidth="3" strokeLinecap="round"/>
      </svg>
    </div>
  );
}

/**
 * VersionFooter — Small version text at bottom-right
 */
export function VersionFooter({ version = "v1.1.9" }) {
  return (
    <div className="version-footer">{version}</div>
  );
}
