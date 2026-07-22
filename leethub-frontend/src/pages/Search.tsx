import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { solutionsApi } from '../api/solutions';
import { DifficultyBadge } from '../components/ui/DifficultyBadge';
import type { SearchResponse, SearchHit } from '../types';
import { MagnifyingGlassIcon, FunnelIcon, XMarkIcon } from '@heroicons/react/24/outline';

const DIFFICULTIES = ['', 'EASY', 'MEDIUM', 'HARD'];
const LANGUAGES    = ['', 'Java', 'Python', 'Python3', 'C++', 'JavaScript', 'TypeScript', 'Go', 'Rust', 'C#', 'Kotlin'];

export const Search: React.FC = () => {
  const [query, setQuery]      = useState('');
  const [difficulty, setDiff]  = useState('');
  const [language, setLang]    = useState('');
  const [result, setResult]    = useState<SearchResponse | null>(null);
  const [loading, setLoading]  = useState(false);
  const [page, setPage]        = useState(0);
  const [showFilters, setShowFilters] = useState(false);

  const search = useCallback(async (q: string, d: string, l: string, p: number) => {
    setLoading(true);
    try {
      const res = await solutionsApi.search({ q: q || undefined, difficulty: d || undefined, language: l || undefined, page: p, size: 20 });
      setResult(res);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, []);

  // Debounced search on query change
  useEffect(() => {
    const t = setTimeout(() => { search(query, difficulty, language, 0); setPage(0); }, 400);
    return () => clearTimeout(t);
  }, [query, difficulty, language, search]);

  // Immediate search on page change (skip page 0, handled by debounced effect above)
  useEffect(() => {
    if (page === 0) return;
    let cancelled = false;
    setLoading(true);
    solutionsApi.search({ q: query || undefined, difficulty: difficulty || undefined, language: language || undefined, page, size: 20 })
      .then(res => { if (!cancelled) setResult(res); })
      .catch(e => console.error(e))
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [page, query, difficulty, language]);

  const clear = () => { setQuery(''); setDiff(''); setLang(''); setPage(0); };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Search</h1>
        <p className="mt-1 text-sm text-gray-400">Find solutions by title, difficulty, or language.</p>
      </div>

      {/* Search bar */}
      <div className="flex gap-3">
        <div className="relative flex-1">
          <MagnifyingGlassIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
          <input
            type="text"
            placeholder="Search by title…"
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="w-full rounded-lg border border-gray-700 bg-gray-800 py-2.5 pl-9 pr-4 text-sm text-white placeholder-gray-500 focus:border-emerald-500 focus:outline-none transition-colors"
          />
          {query && (
            <button onClick={() => setQuery('')} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300">
              <XMarkIcon className="h-4 w-4" />
            </button>
          )}
        </div>
        <button
          onClick={() => setShowFilters(f => !f)}
          className={`flex items-center gap-2 rounded-lg border px-3 py-2.5 text-sm transition-colors ${
            showFilters || difficulty || language
              ? 'border-emerald-600 bg-emerald-600/10 text-emerald-400'
              : 'border-gray-700 bg-gray-800 text-gray-400 hover:text-white'
          }`}
        >
          <FunnelIcon className="h-4 w-4" /> Filters
          {(difficulty || language) && (
            <span className="ml-1 rounded-full bg-emerald-600 px-1.5 py-0.5 text-[10px] text-white">
              {[difficulty, language].filter(Boolean).length}
            </span>
          )}
        </button>
      </div>

      {/* Filters panel */}
      {showFilters && (
        <div className="rounded-xl border border-gray-800 bg-gray-900 p-4 flex flex-wrap gap-6">
          <div>
            <label className="block text-xs font-semibold text-gray-400 mb-2">Difficulty</label>
            <div className="flex gap-2">
              {DIFFICULTIES.map(d => (
                <button
                  key={d}
                  onClick={() => { setDiff(d); setPage(0); }}
                  className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                    difficulty === d
                      ? 'bg-emerald-600 text-white'
                      : 'bg-gray-800 text-gray-400 hover:bg-gray-700'
                  }`}
                >
                  {d || 'All'}
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="block text-xs font-semibold text-gray-400 mb-2">Language</label>
            <select
              value={language}
              onChange={e => { setLang(e.target.value); setPage(0); }}
              className="rounded-md border border-gray-700 bg-gray-800 px-3 py-1.5 text-sm text-gray-300 focus:border-emerald-500 focus:outline-none"
            >
              {LANGUAGES.map(l => <option key={l} value={l}>{l || 'All Languages'}</option>)}
            </select>
          </div>
          {(difficulty || language || query) && (
            <div className="flex items-end">
              <button onClick={clear} className="text-xs text-gray-500 hover:text-red-400 transition-colors">Clear all</button>
            </div>
          )}
        </div>
      )}

      {/* Results */}
      <div className="overflow-hidden rounded-xl border border-gray-800 bg-gray-900">
        {loading ? (
          <div className="space-y-px">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-14 animate-pulse bg-gray-800/50" />
            ))}
          </div>
        ) : !result || result.content.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <MagnifyingGlassIcon className="h-10 w-10 text-gray-700 mb-3" />
            <p className="text-gray-400 text-sm">
              {query || difficulty || language ? 'No results match your filters.' : 'Start typing to search…'}
            </p>
          </div>
        ) : (
          <>
            <div className="border-b border-gray-800 px-4 py-2.5 text-xs text-gray-500">
              {result.totalElements} results
              {result.query && <> for "<span className="text-gray-300">{result.query}</span>"</>}
            </div>
            <ul className="divide-y divide-gray-800">
              {result.content.map((hit: SearchHit) => (
                <li key={hit.id} className="hover:bg-gray-800/50 transition-colors">
                  <Link to={`/problems/${hit.id}`} className="flex items-center gap-4 px-4 py-3">
                    <div className="flex-1">
                      <p className="font-medium text-white hover:text-emerald-400">{hit.title}</p>
                      <p className="text-xs text-gray-500 mt-0.5">{hit.language} · {new Date(hit.submittedAt).toLocaleDateString()}</p>
                    </div>
                    <DifficultyBadge difficulty={hit.difficulty} />
                  </Link>
                </li>
              ))}
            </ul>

            {/* Pagination */}
            {result.totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-gray-800 px-4 py-3 text-xs text-gray-400">
                <span>Page {page + 1} of {result.totalPages}</span>
                <div className="flex gap-2">
                  <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                    className="rounded px-2 py-1 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 transition-colors">Prev</button>
                  <button disabled={page >= result.totalPages - 1} onClick={() => setPage(p => p + 1)}
                    className="rounded px-2 py-1 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 transition-colors">Next</button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};
