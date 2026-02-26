import React, { createContext, useContext } from 'react';
import { useColorScheme } from 'react-native';
import { Colors, ColorScheme } from './colors';

type ThemeContextType = {
  colorScheme: ColorScheme;
  colors: typeof Colors.light & typeof Colors.shared;
  isDark: boolean;
};

const ThemeContext = createContext<ThemeContextType>({
  colorScheme: 'dark',
  colors: { ...Colors.dark, ...Colors.shared },
  isDark: true,
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const systemScheme = useColorScheme();
  const colorScheme: ColorScheme = systemScheme === 'light' ? 'light' : 'dark';
  const isDark = colorScheme === 'dark';
  const colors = { ...(isDark ? Colors.dark : Colors.light), ...Colors.shared };

  return (
    <ThemeContext.Provider value={{ colorScheme, colors, isDark }}>
      {children}
    </ThemeContext.Provider>
  );
}

export const useTheme = () => useContext(ThemeContext);
