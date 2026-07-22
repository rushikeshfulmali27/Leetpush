import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { useAuth } from '../contexts/useAuth';
import { analyticsApi } from '../api/analytics';
import { StatCard } from '../components/ui/StatCard';
import { Heatmap } from '../components/ui/Heatmap';
import type { AnalyticsSummary, HeatmapData } from '../types';
import {
  FireIcon,
  TrophyIcon,
  CalendarDaysIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';

const DIFF_COLORS = { EASY: '#10b981', MEDIUM: '#f59e0b', HARD: '#ef4444' };

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [heatmap, setHeatmap] = useState<HeatmapData | null>(null);
  const [languages, setLanguages] = useState<Array<{ language: string; count: number; percentage: number }>>([]);
  const [loading, setLoading] = useState(true);
  const year = new Date().getFullYear();

  useEffect(() => {
    const load = async () => {
      try {
        const [s, h, l] = await Promise.all([
          analyticsApi.getSummary(),
          analyticsApi.getHeatmap(year),
          analyticsApi.getLanguageDistribution(),
        ]);
        setSummary(s);
        setHeatmap(h);
        setLanguages(l.distribution ?? []);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [year]);

  const diffData = summary
    ? [
        { name: 'Easy',   value: summary.easySolved,   fill: DIFF_COLORS.EASY },
        { name: 'Medium', value: summary.mediumSolved, fill: DIFF_COLORS.MEDIUM },
        { name: 'Hard',   value: summary.hardSolved,   fill: DIFF_COLORS.HARD },
      ]
    : [];

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-gray-800 border-t-emerald-500" />
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in-up">
      {/* Header */}
      <div className="relative z-10 flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight text-white drop-shadow-md">
            Welcome back, <span className="text-gradient">{user?.username}</span> 👋
          </h1>
          <p className="mt-2 text-sm font-medium text-gray-400">Here's how you're progressing on your LeetCode journey.</p>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 gap-5 sm:grid-cols-4 animate-fade-in-up delay-100">
        <StatCard
          label="Total Solved"
          value={summary?.totalSolved ?? 0}
          colorClass="text-emerald-400"
          icon={<CheckCircleIcon className="h-5 w-5" />}
        />
        <StatCard
          label="Current Streak"
          value={`${summary?.currentStreak ?? 0} days`}
          colorClass="text-orange-400"
          sub={`Best: ${summary?.longestStreak ?? 0} days`}
          icon={<FireIcon className="h-5 w-5" />}
        />
        <StatCard
          label="This Week"
          value={summary?.thisWeekSolved ?? 0}
          sub="submissions"
          icon={<CalendarDaysIcon className="h-5 w-5" />}
        />
        <StatCard
          label="This Month"
          value={summary?.thisMonthSolved ?? 0}
          sub="submissions"
          icon={<TrophyIcon className="h-5 w-5" />}
        />
      </div>

      {/* Heatmap */}
      <div className="glass-panel rounded-2xl p-6 animate-fade-in-up delay-200">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-lg font-bold text-white tracking-tight">{year} Activity</h2>
          <div className="h-1 w-20 rounded bg-gradient-to-r from-emerald-500 to-transparent opacity-50"></div>
        </div>
        {heatmap ? (
          <div className="overflow-x-auto pb-2">
            <Heatmap data={heatmap.data} year={year} />
          </div>
        ) : (
          <div className="h-32 animate-pulse rounded-xl bg-gray-800/50" />
        )}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2 animate-fade-in-up delay-300">
        {/* Difficulty breakdown */}
        <div className="glass-panel rounded-2xl p-6">
          <h2 className="mb-6 text-lg font-bold text-white tracking-tight">Difficulty Breakdown</h2>
          <div className="flex flex-col sm:flex-row items-center gap-8">
            <ResponsiveContainer width={160} height={160} className="drop-shadow-lg">
              <PieChart>
                <Pie data={diffData} cx="50%" cy="50%" innerRadius={55} outerRadius={75} dataKey="value" strokeWidth={0} paddingAngle={2}>
                  {diffData.map((entry, i) => <Cell key={i} fill={entry.fill} />)}
                </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: 'rgba(17, 24, 39, 0.9)', borderColor: 'rgba(16, 185, 129, 0.2)', borderRadius: '12px', backdropFilter: 'blur(8px)' }}
                    itemStyle={{ color: '#f9fafb', fontWeight: 600 }}
                  />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-4 w-full sm:w-auto">
              {diffData.map(d => (
                <div key={d.name} className="flex items-center gap-3 rounded-lg bg-gray-800/30 p-2 transition-colors hover:bg-gray-800/60">
                  <span className="h-3 w-3 rounded-full shadow-[0_0_8px_currentColor]" style={{ background: d.fill, color: d.fill }} />
                  <span className="text-sm font-medium text-gray-300">{d.name}</span>
                  <span className="ml-auto text-sm font-bold text-white">{d.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Language distribution */}
        <div className="glass-panel rounded-2xl p-6">
          <h2 className="mb-6 text-lg font-bold text-white tracking-tight">Language Distribution</h2>
          {languages.length > 0 ? (
            <div className="mt-2 drop-shadow-md">
              <ResponsiveContainer width="100%" height={160}>
                <BarChart data={languages.slice(0, 6)} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                  <XAxis dataKey="language" tick={{ fill: '#9ca3af', fontSize: 11, fontWeight: 500 }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fill: '#9ca3af', fontSize: 11 }} axisLine={false} tickLine={false} />
                  <Tooltip
                    cursor={{ fill: 'rgba(255,255,255,0.02)' }}
                    contentStyle={{ background: 'rgba(17, 24, 39, 0.9)', border: '1px solid rgba(16, 185, 129, 0.2)', borderRadius: '12px', color: '#f9fafb', fontSize: 13, fontWeight: 500, backdropFilter: 'blur(8px)' }}
                  />
                  <Bar dataKey="count" fill="url(#colorUv)" radius={[6, 6, 0, 0]} />
                  <defs>
                    <linearGradient id="colorUv" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#10b981" stopOpacity={1}/>
                      <stop offset="100%" stopColor="#047857" stopOpacity={0.8}/>
                    </linearGradient>
                  </defs>
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex h-40 items-center justify-center rounded-xl border border-dashed border-gray-700 bg-gray-800/30 text-sm font-medium text-gray-500">
              No data yet — sync your first solution!
            </div>
          )}
        </div>
      </div>

      {/* Quick action */}
      <div className="relative overflow-hidden rounded-2xl border border-emerald-500/20 bg-gradient-to-r from-emerald-900/20 to-teal-900/20 p-8 text-center shadow-[0_0_30px_rgba(16,185,129,0.1)] animate-fade-in-up delay-400">
        <div className="absolute -right-20 -top-20 h-40 w-40 rounded-full bg-emerald-500/20 blur-3xl"></div>
        <div className="absolute -left-20 -bottom-20 h-40 w-40 rounded-full bg-teal-500/20 blur-3xl"></div>
        
        <div className="relative z-10 max-w-2xl mx-auto">
          <h3 className="text-xl font-bold text-white mb-2">Automate Your Portfolio</h3>
          <p className="text-sm font-medium text-gray-400 leading-relaxed">
            Install the{' '}
            <span className="font-semibold text-emerald-400 drop-shadow-md">LeetHub AI Chrome Extension</span>{' '}
            to automatically sync LeetCode solutions to GitHub as soon as you solve them.
          </p>
          <Link
            to="/settings"
            className="mt-6 inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-emerald-500 to-emerald-600 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-emerald-500/30 transition-all hover:scale-105 hover:from-emerald-400 hover:to-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-gray-900"
          >
            Set up Extension Now
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
          </Link>
        </div>
      </div>
    </div>
  );
};
