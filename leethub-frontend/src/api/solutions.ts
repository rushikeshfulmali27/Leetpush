import { apiClient } from './axios';
import type { SearchResponse } from '../types';

export interface SolutionDetail {
  id: number;
  title: string;
  difficulty: string;
  language: string;
  code: string;
  runtimeMs?: number;
  runtimePercentile?: string;
  memoryKb?: number;
  memoryPercentile?: string;
  syncStatus: string;
  githubUrl?: string;
  submittedAt: string;
  syncedAt?: string;
}

export interface PagedSolutions {
  content: SolutionDetail[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const solutionsApi = {
  list: (params: { page?: number; size?: number; status?: string }): Promise<PagedSolutions> =>
    apiClient.get('/sync/history', { params }),

  get: (id: number): Promise<SolutionDetail> =>
    apiClient.get(`/solutions/${id}`),

  search: (params: {
    q?: string;
    difficulty?: string;
    language?: string;
    page?: number;
    size?: number;
  }): Promise<SearchResponse> =>
    apiClient.get('/search', { params }),

  getAiExplanation: (solutionId: number) =>
    apiClient.get(`/ai/explanations/${solutionId}`),

  regenerateAi: (solutionId: number) =>
    apiClient.post(`/ai/explanations/${solutionId}/regenerate`),
};
