import React from 'react';

interface DifficultyBadgeProps {
  difficulty: string;
}

export const DifficultyBadge: React.FC<DifficultyBadgeProps> = ({ difficulty }) => {
  const d = difficulty?.toUpperCase();
  const cls =
    d === 'EASY'   ? 'badge-easy' :
    d === 'MEDIUM' ? 'badge-medium' :
    d === 'HARD'   ? 'badge-hard' :
    'text-gray-400 bg-gray-800';

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${cls}`}>
      {difficulty}
    </span>
  );
};
