import React from 'react';
import { View } from 'react-native';

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
}

export function GlassCard({ children, className = '' }: GlassCardProps) {
  return (
    <View
      className={`bg-white/[0.08] rounded-card border border-[#FF914D]/20 p-5 ${className}`}
    >
      {children}
    </View>
  );
}
