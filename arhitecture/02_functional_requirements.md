# 2. Functional Requirements

[← Back to Table of Contents](./00_table_of_contents.md)

---

## 2.1 Browser Extension

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-EXT-01 | Submission Detection | P0 | Detect accepted LeetCode submissions via DOM observation and network interception |
| FR-EXT-02 | Metadata Extraction | P0 | Extract problem title, difficulty, tags, runtime, memory, language, and submitted code |
| FR-EXT-03 | Auto Sync | P0 | Automatically push accepted solutions to GitHub via the backend |
| FR-EXT-04 | Manual Sync | P1 | Allow users to manually trigger sync for any visible submission |
| FR-EXT-05 | Sync History | P1 | Display a log of all past sync operations with status indicators |
| FR-EXT-06 | Settings Panel | P1 | Configure target repository, folder structure preferences, AI toggle, and notification preferences |
| FR-EXT-07 | Auth State | P0 | Persist and refresh authentication tokens securely |
| FR-EXT-08 | Offline Queue | P2 | Queue submissions when offline and sync when connectivity is restored |

## 2.2 GitHub Integration

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-GH-01 | OAuth Login | P0 | GitHub OAuth 2.0 flow with `repo` and `user:email` scopes |
| FR-GH-02 | Repository Selection | P0 | List user repositories and allow selection of target repo |
| FR-GH-03 | Auto Folder Creation | P0 | Create topic-based folders (e.g., `Arrays/Two Sum/`) automatically |
| FR-GH-04 | Solution Upload | P0 | Upload `solution.{ext}`, `README.md`, and `notes.md` per problem |
| FR-GH-05 | Solution Update | P1 | Update existing files if the user re-submits a better solution |
| FR-GH-06 | Commit History | P1 | Track and display commit history per problem |
| FR-GH-07 | Branch Support | P2 | Allow syncing to non-default branches |

### GitHub Repository Structure

```
LeetCode/
├── Arrays/
│   ├── Two Sum/
│   │   ├── solution.java
│   │   ├── README.md          ← AI-generated explanation
│   │   └── notes.md           ← User personal notes
│   └── Best Time to Buy and Sell Stock/
│       ├── solution.py
│       ├── README.md
│       └── notes.md
├── Dynamic Programming/
│   └── Climbing Stairs/
│       ├── solution.cpp
│       ├── README.md
│       └── notes.md
└── README.md                   ← Auto-generated index
```

## 2.3 AI Features

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-AI-01 | Problem Summary | P0 | Generate a concise summary of the problem statement |
| FR-AI-02 | Brute Force Approach | P0 | Describe the naive approach with pseudocode |
| FR-AI-03 | Optimized Approach | P0 | Describe the optimal approach with the submitted code's logic |
| FR-AI-04 | Complexity Analysis | P0 | Determine time and space complexity with justification |
| FR-AI-05 | Pattern Recognition | P1 | Identify algorithmic patterns (e.g., Sliding Window, Two Pointers) |
| FR-AI-06 | Interview Notes | P1 | Generate interviewer-perspective talking points |
| FR-AI-07 | Common Mistakes | P1 | List frequent pitfalls for the problem type |
| FR-AI-08 | Revision Notes | P2 | Create spaced-repetition-friendly summaries |
| FR-AI-09 | Multi-Provider | P1 | Support both OpenAI and Gemini with fallback |

### AI-Generated README Example

```markdown
# Two Sum

## Problem Summary
Given an array of integers `nums` and an integer `target`, return indices 
of the two numbers that add up to `target`.

## Brute Force Approach
- Nested loop checking all pairs → O(n²) time, O(1) space

## Optimized Approach  
- Single-pass hash map storing complement as key → O(n) time, O(n) space

## Time Complexity: O(n)
## Space Complexity: O(n)

## Patterns: Hash Map Lookup, Complement Search

## Interview Notes
- Clarify: can elements be reused? Are there duplicates?
- Follow-up: what if the array is sorted? → Two Pointers O(1) space

## Common Mistakes
- Forgetting to check for duplicate indices
- Not handling the case where no solution exists
```

## 2.4 Analytics Dashboard

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-AN-01 | Summary Stats | P0 | Total solved, Easy/Medium/Hard breakdown |
| FR-AN-02 | Streak Tracking | P0 | Current streak, longest streak, daily/weekly/monthly views |
| FR-AN-03 | Activity Heatmap | P0 | GitHub-style contribution heatmap for submissions |
| FR-AN-04 | Language Stats | P1 | Pie/bar chart of language distribution |
| FR-AN-05 | Topic Performance | P1 | Radar chart of topic-wise solve rates |
| FR-AN-06 | Time Trends | P2 | Runtime improvement trends over time |

## 2.5 Search & Knowledge Base

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-KB-01 | Full-Text Search | P0 | Search by problem name, tags, difficulty |
| FR-KB-02 | Filter by Pattern | P1 | Filter by algorithm pattern (DP, BFS, DFS, etc.) |
| FR-KB-03 | Filter by DS | P1 | Filter by data structure (Array, Tree, Graph, etc.) |
| FR-KB-04 | Sort Options | P2 | Sort by date, difficulty, topic |

### Supported Patterns & Data Structures

| Algorithm Patterns | Data Structures |
|-------------------|-----------------|
| Binary Search | Array / List |
| Dynamic Programming | Hash Map / Set |
| BFS / DFS | Tree / BST |
| Sliding Window | Graph |
| Two Pointers | Stack / Queue |
| Greedy | Heap / Priority Queue |
| Backtracking | Trie |
| Divide & Conquer | Linked List |
| Union Find | Matrix |

## 2.6 User Notes

| ID | Requirement | Priority | Description |
|----|-------------|----------|-------------|
| FR-NT-01 | Personal Notes | P1 | Free-form markdown notes per problem |
| FR-NT-02 | Mistake Log | P1 | Structured mistake tracking |
| FR-NT-03 | Revision Reminders | P2 | Schedule revision reminders (spaced repetition) |
| FR-NT-04 | Interview Observations | P2 | Tag notes as interview-specific |

---

[← Previous: Executive Summary](./01_executive_summary.md) | [Next: Non-Functional Requirements →](./03_non_functional_requirements.md)
