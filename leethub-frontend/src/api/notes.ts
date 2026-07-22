import { apiClient } from './axios';
import type { Note } from '../types';

export interface PagedNotes {
  content: Note[];
  totalElements: number;
  totalPages: number;
  page: number;
}

export const notesApi = {
  list: (params?: { solutionId?: number; noteType?: string; page?: number; size?: number }): Promise<PagedNotes> =>
    apiClient.get('/notes', { params }),

  create: (data: { solutionId: number; content: string; noteType?: string }): Promise<Note> =>
    apiClient.post('/notes', data),

  update: (id: number, data: { content?: string; noteType?: string }): Promise<Note> =>
    apiClient.put(`/notes/${id}`, data),

  delete: (id: number): Promise<void> =>
    apiClient.delete(`/notes/${id}`),
};
