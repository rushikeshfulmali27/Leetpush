import { apiClient } from './axios';
import type { AnalyticsSummary, HeatmapData } from '../types';

export const analyticsApi = {
  getSummary: (): Promise<AnalyticsSummary> =>
    apiClient.get('/analytics/summary'),

  getHeatmap: (year?: number): Promise<HeatmapData> =>
    apiClient.get('/analytics/heatmap', { params: { year: year ?? new Date().getFullYear() } }),

  getLanguageDistribution: (): Promise<{ distribution: Array<{ language: string; count: number; percentage: number }> }> =>
    apiClient.get('/analytics/languages'),
};
