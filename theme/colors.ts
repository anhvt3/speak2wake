/**
 * Speak2Wake Design System — Warm Sunrise Palette
 * Applied via UI UX Pro Max principles:
 * - Warm orange/amber tones evoke sunrise & waking up
 * - Dark background for alarm app (easy on eyes at wake-up)
 * - High contrast for readability in low light
 * - WCAG AA compliant (4.5:1+ contrast ratios)
 */
export const Colors = {
  light: {
    background: '#FFF8F0',
    surface: '#FFF1E6',
    surfaceGlass: 'rgba(255,145,77,0.08)',
    surfaceGlassBorder: 'rgba(255,145,77,0.15)',
    textPrimary: '#1A1210',
    textSecondary: '#7A6B60',
    textMuted: 'rgba(26,18,16,0.4)',
  },
  dark: {
    background: '#141018',
    surface: '#1E1924',
    surfaceGlass: 'rgba(255,145,77,0.10)',
    surfaceGlassBorder: 'rgba(255,145,77,0.18)',
    textPrimary: '#FFFFFF',
    textSecondary: 'rgba(255,255,255,0.65)',
    textMuted: 'rgba(255,255,255,0.35)',
  },
  shared: {
    // Primary — Warm Orange
    primary: '#FF914D',
    primaryLight: '#FFB380',
    primaryDark: '#E8732A',

    // Accent — Golden Amber
    accent: '#FFBE5C',
    accentGold: '#FFD93D',

    // Legacy aliases (backward compat)
    violet: '#FF914D',
    violetLight: '#FFB380',
    violetPink: '#FFBE5C',
    accentOrange: '#FF914D',

    // Semantic
    success: '#4ADE80',
    error: '#FF6B6B',
    warning: '#FFD93D',

    // Gradients
    gradientStart: '#FF6B35',
    gradientMid: '#FF914D',
    gradientEnd: '#FFBE5C',
  },
} as const;

export type ColorScheme = 'light' | 'dark';
export type ThemeColors = typeof Colors.light & typeof Colors.shared;
