import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/useAuth';
import { apiClient } from '../api/axios';
import type { Repository } from '../types';
import {
  FolderOpenIcon,
  CheckCircleIcon,
  ArrowTopRightOnSquareIcon,
  Cog6ToothIcon,
  PuzzlePieceIcon
} from '@heroicons/react/24/outline';
import toast from 'react-hot-toast';

export const Settings: React.FC = () => {
  const { user, logout } = useAuth();
  const [repos, setRepos]         = useState<Repository[]>([]);
  const [loadingRepos, setLoadingRepos] = useState(true);
  const [selectingRepo, setSelectingRepo] = useState<string | null>(null);

  useEffect(() => {
    // Backend returns: { success: true, data: { repositories: [...] } }
    // Axios interceptor unwraps to data = { repositories: [...] }
    apiClient.get<never, { repositories: Repository[] }>(`/repositories?_t=${Date.now()}`)
      .then(data => setRepos(data?.repositories ?? []))
      .catch(() => setRepos([]))
      .finally(() => setLoadingRepos(false));
  }, []);

  const selectRepo = async (repoFullName: string) => {
    setSelectingRepo(repoFullName);
    try {
      await apiClient.post('/repositories/select', { repoFullName, branch: 'main' });
      setRepos(prev => prev.map(r => ({ ...r, isActive: r.repoFullName === repoFullName })));
      toast.success(`Repository "${repoFullName}" selected!`);
    } catch {
      toast.error('Failed to select repository');
    } finally {
      setSelectingRepo(null);
    }
  };

  return (
    <div className="max-w-2xl space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white">Settings</h1>
        <p className="mt-1 text-sm text-gray-400">Manage your account, repository, and extension setup.</p>
      </div>

      {/* Account */}
      <div className="rounded-xl border border-gray-800 bg-gray-900 p-6">
        <div className="flex items-center gap-2 mb-5">
          <Cog6ToothIcon className="h-5 w-5 text-gray-400" />
          <h2 className="text-base font-semibold text-white">Account</h2>
        </div>
        <div className="flex items-center gap-4">
          <img
            src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${user?.username}&background=0D8ABC&color=fff`}
            alt=""
            className="h-14 w-14 rounded-full"
          />
          <div>
            <p className="font-semibold text-white">{user?.username}</p>
            <p className="text-sm text-gray-400">{user?.email ?? 'No email'}</p>
            <p className="text-xs text-gray-600 mt-0.5">
              Member since {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}
            </p>
          </div>
        </div>
        <div className="mt-6 border-t border-gray-800 pt-4">
          <button
            onClick={logout}
            className="text-sm text-red-400 hover:text-red-300 transition-colors"
          >
            Sign out
          </button>
        </div>
      </div>

      {/* Repository */}
      <div className="rounded-xl border border-gray-800 bg-gray-900 p-6">
        <div className="flex items-center gap-2 mb-5">
          <FolderOpenIcon className="h-5 w-5 text-gray-400" />
          <h2 className="text-base font-semibold text-white">Target Repository</h2>
        </div>
        <p className="text-sm text-gray-400 mb-4">
          Choose which GitHub repository your LeetCode solutions will be pushed to.
        </p>

        {loadingRepos ? (
          <div className="space-y-3">
            {[1, 2, 3].map(i => <div key={i} className="h-12 animate-pulse rounded-lg bg-gray-800" />)}
          </div>
        ) : repos.length === 0 ? (
          <div className="rounded-lg border border-dashed border-gray-700 p-6 text-center">
            <p className="text-sm text-gray-400">No repositories found.</p>
            <p className="text-xs text-gray-600 mt-1">Make sure you've granted repository access to the GitHub OAuth app.</p>
          </div>
        ) : (
          <ul className="space-y-2">
            {repos.map(repo => (
              <li key={repo.id} className={`flex items-center justify-between rounded-lg border p-3 transition-colors ${
                repo.isActive ? 'border-emerald-600 bg-emerald-950/20' : 'border-gray-800 bg-gray-800/50 hover:border-gray-700'
              }`}>
                <div className="flex items-center gap-3">
                  {repo.isActive ? (
                    <CheckCircleIcon className="h-5 w-5 text-emerald-400 shrink-0" />
                  ) : (
                    <div className="h-5 w-5 rounded-full border-2 border-gray-700 shrink-0" />
                  )}
                  <div>
                    <p className="text-sm font-medium text-white">{repo.repoFullName}</p>
                    <p className="text-xs text-gray-500">branch: {repo.defaultBranch}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <a
                    href={`https://github.com/${repo.repoFullName}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-gray-500 hover:text-gray-300 transition-colors"
                  >
                    <ArrowTopRightOnSquareIcon className="h-4 w-4" />
                  </a>
                  {!repo.isActive && (
                    <button
                      onClick={() => selectRepo(repo.repoFullName)}
                      disabled={selectingRepo === repo.repoFullName}
                      className="rounded-md bg-gray-700 px-3 py-1 text-xs font-medium text-white hover:bg-gray-600 disabled:opacity-50 transition-colors"
                    >
                      {selectingRepo === repo.repoFullName ? 'Selecting…' : 'Select'}
                    </button>
                  )}
                  {repo.isActive && (
                    <span className="text-xs font-semibold text-emerald-400">Active</span>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Extension */}
      <div className="rounded-xl border border-gray-800 bg-gray-900 p-6">
        <div className="flex items-center gap-2 mb-5">
          <PuzzlePieceIcon className="h-5 w-5 text-gray-400" />
          <h2 className="text-base font-semibold text-white">Chrome Extension</h2>
        </div>
        <p className="text-sm text-gray-400 mb-4">
          The LeetHub AI Chrome extension detects when you submit a successful LeetCode solution
          and automatically syncs it to your GitHub repository with AI explanations.
        </p>
        <div className="rounded-lg border border-dashed border-gray-700 p-4 text-center">
          <p className="text-sm text-gray-500">Extension coming soon — load it from the <code className="text-gray-400 text-xs bg-gray-800 px-1 rounded">leethub-extension/</code> folder as an unpacked extension.</p>
        </div>
      </div>
    </div>
  );
};
