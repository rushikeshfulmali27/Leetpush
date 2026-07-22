import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { solutionsApi } from '../api/solutions';
import type { SolutionDetail, PagedSolutions } from '../api/solutions';
import { DifficultyBadge } from '../components/ui/DifficultyBadge';
import { MagnifyingGlassIcon, ArrowTopRightOnSquareIcon } from '@heroicons/react/24/outline';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/20/solid';

const STATUS_STYLE: Record<string, string> = {
  SYNCED:  'text-emerald-400 bg-emerald-400/10',
  SYNCING: 'text-yellow-400 bg-yellow-400/10',
  PENDING: 'text-gray-400 bg-gray-400/10',
  FAILED:  'text-red-400 bg-red-400/10',
};

export const Problems: React.FC = () => {
  const [data, setData] = useState<PagedSolutions | null>(null);
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    solutionsApi.list({ page, size: 20, status: status || undefined })
      .then(res => { if (!cancelled) setData(res); })
      .catch(e => console.error(e))
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [page, status]);

  const handleStatus = (s: string) => { setStatus(s); setPage(0); };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Problems</h1>
          <p className="mt-1 text-sm text-gray-400">
            {data ? `${data.totalElements} solutions synced` : 'Loading…'}
          </p>
        </div>
        <Link
          to="/search"
          className="flex items-center gap-2 rounded-md border border-gray-700 bg-gray-800 px-3 py-2 text-sm text-gray-300 hover:bg-gray-700 transition-colors"
        >
          <MagnifyingGlassIcon className="h-4 w-4" />
          Search
        </Link>
      </div>

      {/* Filter chips */}
      <div className="flex gap-2 flex-wrap">
        {['', 'SYNCED', 'FAILED', 'PENDING'].map((s) => (
          <button
            key={s}
            onClick={() => handleStatus(s)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              status === s
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-white'
            }`}
          >
            {s || 'All'}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-800 bg-gray-900">
        {loading ? (
          <div className="space-y-px">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="h-14 animate-pulse bg-gray-800/50" />
            ))}
          </div>
        ) : !data || data.content.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <p className="text-gray-400 text-sm">No solutions found.</p>
            <p className="text-gray-600 text-xs mt-1">Install the Chrome extension to start syncing!</p>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-xs text-gray-500 uppercase tracking-wide">
                <th className="px-4 py-3 text-left">Title</th>
                <th className="px-4 py-3 text-left">Difficulty</th>
                <th className="px-4 py-3 text-left">Language</th>
                <th className="px-4 py-3 text-left">Status</th>
                <th className="px-4 py-3 text-left">Synced At</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {data.content.map((s: SolutionDetail) => (
                <tr key={s.id} className="hover:bg-gray-800/50 transition-colors">
                  <td className="px-4 py-3">
                    <Link to={`/problems/${s.id}`} className="font-medium text-white hover:text-emerald-400 transition-colors">
                      {s.title}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <DifficultyBadge difficulty={s.difficulty} />
                  </td>
                  <td className="px-4 py-3 text-gray-400">{s.language}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${STATUS_STYLE[s.syncStatus] ?? ''}`}>
                      {s.syncStatus}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs">
                    {s.syncedAt
                      ? new Date(s.syncedAt).toLocaleDateString()
                      : s.submittedAt
                      ? new Date(s.submittedAt).toLocaleDateString()
                      : '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    {s.githubUrl && (
                      <a href={s.githubUrl} target="_blank" rel="noopener noreferrer"
                        className="text-gray-500 hover:text-white transition-colors">
                        <ArrowTopRightOnSquareIcon className="h-4 w-4" />
                      </a>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm text-gray-400">
          <span>Page {data.page + 1} of {data.totalPages}</span>
          <div className="flex gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="flex items-center gap-1 rounded-md border border-gray-700 bg-gray-800 px-3 py-1.5 text-xs hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeftIcon className="h-4 w-4" /> Prev
            </button>
            <button
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage(p => p + 1)}
              className="flex items-center gap-1 rounded-md border border-gray-700 bg-gray-800 px-3 py-1.5 text-xs hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              Next <ChevronRightIcon className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
