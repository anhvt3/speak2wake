import React from 'react';
import { View } from 'react-native';

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
}

export function GlassCard({ children, className = '' }: GlassCardProps) {
  return (
    <View
      className={`bg-white/20 dark:bg-white/[0.08] rounded-card border border-white/30 dark:border-white/10 p-5 ${className}`}
    >
      {children}
    </View>
  );
}
