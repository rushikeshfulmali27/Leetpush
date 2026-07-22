export interface User {
  id: number;
  githubId: string;
  username: string;
  email: string | null;
  avatarUrl: string | null;
  preferredLanguage: string;
  createdAt: string;
}

export interface Repository {
  id: number;
  repoName: string;
  repoFullName: string;
  defaultBranch: string;
  isActive: boolean;
  solutionCount: number;
}

export interface Solution {
  id: number;
  title: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  language: string;
  syncStatus: 'PENDING' | 'SYNCING' | 'SYNCED' | 'FAILED';
  submittedAt: string;
  githubUrl?: string;
}

export interface SyncHistory {
  syncId: string;
  title: string;
  difficulty: string;
  language: string;
  status: string;
  syncedAt: string;
  githubUrl: string;
}

export interface AnalyticsSummary {
  totalSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  currentStreak: number;
  longestStreak: number;
  thisWeekSolved: number;
  thisMonthSolved: number;
}

export interface HeatmapData {
  year: number;
  data: Array<{ date: string; count: number }>;
  maxCount: number;
}

export interface AiExplanation {
  solutionId: number;
  problemSummary?: string;
  bruteForceApproach?: string;
  optimizedApproach?: string;
  timeComplexity?: string;
  spaceComplexity?: string;
  patterns?: string[];
  interviewNotes?: string;
  commonMistakes?: string;
  revisionNotes?: string;
  aiProvider?: string;
  aiModel?: string;
  generatedAt: string;
}

export interface Note {
  id: number;
  solutionId: number;
  problemTitle: string;
  content: string;
  noteType: 'PERSONAL' | 'INTERVIEW' | 'REVISION';
  reminderAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SearchHit {
  id: number;
  title: string;
  difficulty: string;
  tags: string[];
  language: string;
  syncStatus: string;
  submittedAt: string;
}

export interface SearchResponse {
  content: SearchHit[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  query: string;
}
