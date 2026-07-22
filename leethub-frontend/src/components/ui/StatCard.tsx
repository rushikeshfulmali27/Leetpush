import React from 'react';

interface StatCardProps {
  label: string;
  value: number | string;
  sub?: string;
  colorClass?: string;
  icon?: React.ReactNode;
}

export const StatCard: React.FC<StatCardProps> = ({ label, value, sub, colorClass = 'text-white', icon }) => (
  <div className="glass-panel glass-panel-hover rounded-2xl p-6 relative overflow-hidden group">
    {/* Subtle gradient glow inside the card on hover */}
    <div className="absolute -inset-1 bg-gradient-to-r from-emerald-600/20 to-teal-600/20 blur opacity-0 group-hover:opacity-100 transition duration-500 rounded-2xl"></div>
    
    <div className="relative z-10">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-gray-400 group-hover:text-gray-300 transition-colors">{label}</p>
        {icon && <div className="text-gray-500 group-hover:text-emerald-400 transition-colors drop-shadow-md">{icon}</div>}
      </div>
      <p className={`mt-3 text-4xl font-extrabold tracking-tight ${colorClass} drop-shadow-md`}>{value}</p>
      {sub && <p className="mt-2 text-xs font-medium text-gray-500 group-hover:text-gray-400 transition-colors">{sub}</p>}
    </div>
  </div>
);
