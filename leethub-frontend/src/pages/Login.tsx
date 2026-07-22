import React from 'react';
import { ArrowRightIcon } from '@heroicons/react/20/solid';
import axiosClient from '../api/axiosClient';

export const Login: React.FC = () => {
  const handleGithubLogin = async () => {
    try {
      const response = await axiosClient.get('/auth/github/url');
      if (response.data?.authUrl) {
        window.location.href = response.data.authUrl;
      }
    } catch (error) {
      console.error('Failed to initiate GitHub login:', error);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-gray-950 px-4 sm:px-6 lg:px-8">
      {/* Animated background blobs */}
      <div className="absolute -top-40 -left-40 h-96 w-96 rounded-full bg-emerald-600/30 blur-3xl animate-blob"></div>
      <div className="absolute top-1/4 -right-20 h-80 w-80 rounded-full bg-teal-600/20 blur-3xl animate-blob delay-200"></div>
      <div className="absolute -bottom-40 left-1/2 h-96 w-96 -translate-x-1/2 rounded-full bg-emerald-900/40 blur-3xl animate-blob delay-500"></div>

      <div className="relative z-10 w-full max-w-md animate-fade-in-up">
        <div className="glass-panel rounded-2xl p-10 text-center">
          <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-400 to-emerald-600 shadow-lg shadow-emerald-500/30">
            <svg className="h-10 w-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h2 className="text-3xl font-extrabold tracking-tight text-white mb-2">
            Welcome to <span className="text-gradient">LeetHub</span>
          </h2>
          <p className="mb-8 text-sm text-gray-400 leading-relaxed">
            Automatically sync your LeetCode solutions to GitHub with AI-generated explanations and smart notes.
          </p>
          
          <button
            onClick={handleGithubLogin}
            className="group relative flex w-full items-center justify-center gap-3 rounded-xl bg-gray-800 px-4 py-3.5 text-sm font-semibold text-white transition-all hover:bg-gray-700 hover:shadow-[0_0_20px_rgba(16,185,129,0.3)] focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 focus:ring-offset-gray-900 border border-gray-700 hover:border-emerald-500/50"
          >
            <svg className="h-5 w-5 text-gray-300 transition-colors group-hover:text-white" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
            </svg>
            Continue with GitHub
            <ArrowRightIcon className="h-5 w-5 text-gray-500 transition-transform group-hover:translate-x-1 group-hover:text-emerald-400" aria-hidden="true" />
          </button>
        </div>
        
        <p className="mt-6 text-center text-xs text-gray-500 animate-fade-in-up delay-300">
          By continuing, you agree to our Terms of Service and Privacy Policy.
        </p>
      </div>
    </div>
  );
};
