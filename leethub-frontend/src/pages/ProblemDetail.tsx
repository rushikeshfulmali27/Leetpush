import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { solutionsApi } from '../api/solutions';
import type { SolutionDetail } from '../api/solutions';
import { DifficultyBadge } from '../components/ui/DifficultyBadge';
import type { AiExplanation } from '../types';
import {
  ArrowLeftIcon,
  ArrowTopRightOnSquareIcon,
  SparklesIcon,
  ArrowPathIcon,
  ClockIcon,
  CpuChipIcon
} from '@heroicons/react/24/outline';
import toast from 'react-hot-toast';

export const ProblemDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [solution, setSolution] = useState<SolutionDetail | null>(null);
  const [aiExplanation, setAiExplanation] = useState<AiExplanation | null>(null);
  const [loading, setLoading] = useState(true);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiTab, setAiTab] = useState<'summary' | 'approach' | 'interview' | 'revision'>('summary');

  useEffect(() => {
    const load = async () => {
      if (!id) return;
      try {
        const [sol, ai] = await Promise.allSettled([
          solutionsApi.get(Number(id)),
          solutionsApi.getAiExplanation(Number(id)),
        ]);
        if (sol.status === 'fulfilled') setSolution(sol.value);
        if (ai.status === 'fulfilled') setAiExplanation(ai.value as unknown as AiExplanation);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const regenerateAi = async () => {
    if (!id) return;
    setAiLoading(true);
    try {
      const ai = await solutionsApi.regenerateAi(Number(id));
      setAiExplanation(ai as unknown as AiExplanation);
      toast.success('AI explanation regenerated!');
    } catch {
      toast.error('Failed to regenerate AI explanation');
    } finally {
      setAiLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-gray-800 border-t-emerald-500" />
      </div>
    );
  }

  if (!solution) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-gray-400">Solution not found.</p>
        <button onClick={() => navigate(-1)} className="mt-4 text-sm text-emerald-400 hover:underline">Go back</button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back */}
      <button
        onClick={() => navigate('/problems')}
        className="flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors"
      >
        <ArrowLeftIcon className="h-4 w-4" /> Back to Problems
      </button>

      {/* Title row */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-white">{solution.title}</h1>
          <div className="mt-2 flex items-center gap-3 flex-wrap">
            <DifficultyBadge difficulty={solution.difficulty} />
            <span className="rounded-full bg-gray-800 px-2.5 py-0.5 text-xs text-gray-300">{solution.language}</span>
            {solution.runtimeMs !== undefined && (
              <span className="flex items-center gap-1 text-xs text-gray-400">
                <ClockIcon className="h-3.5 w-3.5" /> {solution.runtimeMs} ms
                {solution.runtimePercentile && <span className="text-green-400">({solution.runtimePercentile})</span>}
              </span>
            )}
            {solution.memoryKb !== undefined && (
              <span className="flex items-center gap-1 text-xs text-gray-400">
                <CpuChipIcon className="h-3.5 w-3.5" /> {(solution.memoryKb / 1024).toFixed(1)} MB
                {solution.memoryPercentile && <span className="text-green-400">({solution.memoryPercentile})</span>}
              </span>
            )}
          </div>
        </div>
        {solution.githubUrl && (
          <a
            href={solution.githubUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 rounded-md border border-gray-700 bg-gray-800 px-3 py-2 text-sm text-gray-300 hover:bg-gray-700 transition-colors"
          >
            <ArrowTopRightOnSquareIcon className="h-4 w-4" /> View on GitHub
          </a>
        )}
      </div>

      {/* Main grid */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        {/* Code panel */}
        <div className="rounded-xl border border-gray-800 bg-gray-900 overflow-hidden">
          <div className="flex items-center justify-between border-b border-gray-800 px-4 py-3">
            <span className="text-sm font-medium text-gray-300">Solution Code</span>
            <span className="rounded bg-gray-800 px-2 py-0.5 text-xs text-gray-400">{solution.language}</span>
          </div>
          <pre className="overflow-auto p-4 text-xs leading-relaxed text-gray-200 max-h-[500px]">
            <code>{solution.code}</code>
          </pre>
        </div>

        {/* AI Explanation panel */}
        <div className="rounded-xl border border-gray-800 bg-gray-900 overflow-hidden">
          <div className="flex items-center justify-between border-b border-gray-800 px-4 py-3">
            <div className="flex items-center gap-2">
              <SparklesIcon className="h-4 w-4 text-emerald-400" />
              <span className="text-sm font-medium text-gray-300">AI Explanation</span>
              {aiExplanation?.aiModel && (
                <span className="rounded bg-emerald-900/40 px-2 py-0.5 text-[10px] text-emerald-400">{aiExplanation.aiModel}</span>
              )}
            </div>
            <button
              onClick={regenerateAi}
              disabled={aiLoading}
              className="flex items-center gap-1 text-xs text-gray-400 hover:text-white disabled:opacity-40 transition-colors"
            >
              <ArrowPathIcon className={`h-3.5 w-3.5 ${aiLoading ? 'animate-spin' : ''}`} />
              Regenerate
            </button>
          </div>

          {aiLoading ? (
            <div className="flex items-center justify-center py-20">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-800 border-t-emerald-500" />
            </div>
          ) : aiExplanation ? (
            <div className="flex flex-col h-full">
              {/* Tabs */}
              <div className="flex border-b border-gray-800 px-4 text-xs">
                {(['summary', 'approach', 'interview', 'revision'] as const).map(tab => (
                  <button
                    key={tab}
                    onClick={() => setAiTab(tab)}
                    className={`px-3 py-2.5 capitalize font-medium transition-colors border-b-2 ${
                      aiTab === tab
                        ? 'border-emerald-500 text-emerald-400'
                        : 'border-transparent text-gray-500 hover:text-gray-300'
                    }`}
                  >
                    {tab}
                  </button>
                ))}
              </div>

              {/* Tab content */}
              <div className="p-4 overflow-y-auto max-h-[430px] text-sm text-gray-300 leading-relaxed space-y-4">
                {aiTab === 'summary' && (
                  <>
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-emerald-400 mb-2">Problem Summary</h3>
                      <p>{aiExplanation.problemSummary ?? '—'}</p>
                    </div>
                    {aiExplanation.timeComplexity && (
                      <div className="flex gap-6">
                        <div>
                          <span className="text-xs text-gray-500">Time</span>
                          <p className="font-mono font-semibold text-white">{aiExplanation.timeComplexity}</p>
                        </div>
                        <div>
                          <span className="text-xs text-gray-500">Space</span>
                          <p className="font-mono font-semibold text-white">{aiExplanation.spaceComplexity}</p>
                        </div>
                      </div>
                    )}
                    {aiExplanation.patterns && aiExplanation.patterns.length > 0 && (
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-emerald-400 mb-2">Patterns</h3>
                        <div className="flex gap-2 flex-wrap">
                          {aiExplanation.patterns.map(p => (
                            <span key={p} className="rounded-full bg-gray-800 px-2.5 py-0.5 text-xs text-gray-300">{p}</span>
                          ))}
                        </div>
                      </div>
                    )}
                  </>
                )}
                {aiTab === 'approach' && (
                  <>
                    {aiExplanation.bruteForceApproach && (
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-yellow-400 mb-2">Brute Force</h3>
                        <p className="whitespace-pre-wrap">{aiExplanation.bruteForceApproach}</p>
                      </div>
                    )}
                    {aiExplanation.optimizedApproach && (
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-emerald-400 mb-2">Optimized Approach</h3>
                        <p className="whitespace-pre-wrap">{aiExplanation.optimizedApproach}</p>
                      </div>
                    )}
                  </>
                )}
                {aiTab === 'interview' && (
                  <>
                    {aiExplanation.interviewNotes && (
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-blue-400 mb-2">Interview Tips</h3>
                        <p className="whitespace-pre-wrap">{aiExplanation.interviewNotes}</p>
                      </div>
                    )}
                    {aiExplanation.commonMistakes && (
                      <div>
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-red-400 mb-2">Common Mistakes</h3>
                        <p className="whitespace-pre-wrap">{aiExplanation.commonMistakes}</p>
                      </div>
                    )}
                  </>
                )}
                {aiTab === 'revision' && (
                  <div>
                    <h3 className="text-xs font-semibold uppercase tracking-wide text-purple-400 mb-2">Quick Revision</h3>
                    <p className="whitespace-pre-wrap">{aiExplanation.revisionNotes ?? '—'}</p>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-16 text-center px-6">
              <SparklesIcon className="h-10 w-10 text-gray-700 mb-3" />
              <p className="text-sm text-gray-400">No AI explanation available yet.</p>
              <button
                onClick={regenerateAi}
                className="mt-4 flex items-center gap-2 rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 transition-colors"
              >
                <SparklesIcon className="h-4 w-4" /> Generate Explanation
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
