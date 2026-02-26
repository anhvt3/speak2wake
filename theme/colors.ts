export const Colors = {
  light: {
    background: '#FFFFFF',
    surface: '#F5F3F8',
    surfaceGlass: 'rgba(255,255,255,0.2)',
    surfaceGlassBorder: 'rgba(255,255,255,0.3)',
    textPrimary: '#1C1721',
    textSecondary: '#7F8187',
    textMuted: 'rgba(28,23,33,0.4)',
  },
  dark: {
    background: '#1C1721',
    surface: '#2B2630',
    surfaceGlass: 'rgba(255,255,255,0.08)',
    surfaceGlassBorder: 'rgba(255,255,255,0.1)',
    textPrimary: '#FFFFFF',
    textSecondary: 'rgba(255,255,255,0.6)',
    textMuted: 'rgba(255,255,255,0.3)',
  },
  shared: {
    violet: '#9E6DFB',
    violetLight: '#8A70F8',
    violetPink: '#D28AED',
    accentOrange: '#F79F40',
    accentGold: '#FBF66E',
    success: '#4ADE80',
    error: '#F87171',
    warning: '#FBBF24',
    gradientStart: '#8A70F8',
    gradientEnd: '#D28AED',
  },
} as const;

export type ColorScheme = 'light' | 'dark';
export type ThemeColors = typeof Colors.light & typeof Colors.shared;
