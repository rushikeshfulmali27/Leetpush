# 8. API Design

[← Back to Table of Contents](./00_table_of_contents.md)

---

**Base URL:** `https://api.leethub.ai/api/v1`
**Content Type:** `application/json`
**Authentication:** Bearer JWT (except auth endpoints)

---

## 8.1 Authentication APIs

### `GET /auth/github/url` — Get OAuth Authorization URL

**Auth:** None

**Response** `200 OK`:
```json
{
  "authUrl": "https://github.com/login/oauth/authorize?client_id=...&scope=repo,user:email&state=abc123",
  "state": "abc123"
}
```

---

### `POST /auth/github/callback` — Exchange OAuth Code for Tokens

**Auth:** None

**Request:**
```json
{
  "code": "github_oauth_code",
  "state": "abc123"
}
```

**Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "avatarUrl": "https://avatars.githubusercontent.com/u/12345"
  }
}
```

**Error** `401 Unauthorized`:
```json
{
  "timestamp": "2026-06-22T10:15:30Z",
  "status": 401,
  "error": "Unauthorized",
  "code": "OAUTH_CODE_INVALID",
  "message": "The authorization code has expired or is invalid",
  "path": "/api/v1/auth/github/callback"
}
```

---

### `POST /auth/refresh` — Refresh Access Token

**Auth:** None

**Request:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Response** `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...(new)",
  "expiresIn": 3600
}
```

---

### `POST /auth/logout` — Invalidate Tokens

**Auth:** Required

**Response** `204 No Content`

---

## 8.2 Sync APIs

### `POST /sync/submit` — Submit Solution for Sync

**Auth:** Required

**Request:**
```json
{
  "leetcodeId": "1",
  "title": "Two Sum",
  "titleSlug": "two-sum",
  "difficulty": "EASY",
  "tags": ["Array", "Hash Table"],
  "language": "java",
  "code": "class Solution {\n  public int[] twoSum(int[] nums, int target) {\n    Map<Integer, Integer> map = new HashMap<>();\n    for (int i = 0; i < nums.length; i++) {\n      int complement = target - nums[i];\n      if (map.containsKey(complement)) {\n        return new int[] { map.get(complement), i };\n      }\n      map.put(nums[i], i);\n    }\n    throw new IllegalArgumentException();\n  }\n}",
  "runtimeMs": 2,
  "runtimePercentile": "95.2%",
  "memoryKb": 42100,
  "memoryPercentile": "87.1%",
  "submittedAt": "2026-06-22T10:15:30Z"
}
```

**Response** `202 Accepted`:
```json
{
  "syncId": "sync_a1b2c3d4",
  "status": "PENDING",
  "message": "Solution queued for sync"
}
```

**Validation Error** `422 Unprocessable Entity`:
```json
{
  "timestamp": "2026-06-22T10:15:30Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "VALIDATION_FAILED",
  "message": "Validation failed for 2 fields",
  "fieldErrors": [
    { "field": "leetcodeId", "message": "must not be blank" },
    { "field": "code", "message": "must not be blank" }
  ],
  "path": "/api/v1/sync/submit"
}
```

---

### `GET /sync/status/{syncId}` — Check Sync Status

**Auth:** Required

**Response** `200 OK`:
```json
{
  "syncId": "sync_a1b2c3d4",
  "status": "SYNCED",
  "commitSha": "abc123def456",
  "githubUrl": "https://github.com/johndoe/LeetCode/tree/main/Arrays/Two_Sum",
  "aiGenerated": true,
  "syncedAt": "2026-06-22T10:15:35Z"
}
```

**Status Values:**

| Status | Description |
|--------|-------------|
| `PENDING` | Queued for processing |
| `SYNCING` | AI generation or GitHub push in progress |
| `SYNCED` | Successfully pushed to GitHub |
| `FAILED` | Sync failed (see error details) |

---

### `GET /sync/history` — Get Sync History

**Auth:** Required
**Query Params:** `?page=0&size=20&status=SYNCED`

**Response** `200 OK`:
```json
{
  "content": [
    {
      "syncId": "sync_a1b2c3d4",
      "title": "Two Sum",
      "difficulty": "EASY",
      "language": "java",
      "status": "SYNCED",
      "syncedAt": "2026-06-22T10:15:35Z"
    },
    {
      "syncId": "sync_e5f6g7h8",
      "title": "Climbing Stairs",
      "difficulty": "EASY",
      "language": "python",
      "status": "SYNCED",
      "syncedAt": "2026-06-22T09:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8
}
```

---

## 8.3 Problem & Solution APIs

### `GET /problems` — List User's Solved Problems

**Auth:** Required
**Query Params:** `?page=0&size=20&difficulty=EASY&tag=Array&language=java&sort=submittedAt,desc`

**Response** `200 OK`:
```json
{
  "content": [
    {
      "id": 1,
      "leetcodeId": "1",
      "title": "Two Sum",
      "difficulty": "EASY",
      "tags": ["Array", "Hash Table"],
      "language": "java",
      "runtimeMs": 2,
      "syncStatus": "SYNCED",
      "hasAiExplanation": true,
      "notesCount": 2,
      "submittedAt": "2026-06-22T10:15:30Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 142
}
```

---

### `GET /problems/{id}` — Get Problem Detail with AI Explanation

**Auth:** Required

**Response** `200 OK`:
```json
{
  "id": 1,
  "leetcodeId": "1",
  "title": "Two Sum",
  "difficulty": "EASY",
  "tags": ["Array", "Hash Table"],
  "language": "java",
  "code": "class Solution { ... }",
  "runtimeMs": 2,
  "runtimePercentile": "95.2%",
  "memoryKb": 42100,
  "memoryPercentile": "87.1%",
  "githubUrl": "https://github.com/johndoe/LeetCode/tree/main/Arrays/Two_Sum",
  "aiExplanation": {
    "problemSummary": "Given an array of integers and a target, return indices of two numbers that add up to target.",
    "bruteForceApproach": "Use nested loops to check all pairs. For each element, iterate through the rest to find the complement.\n\nTime: O(n²), Space: O(1)",
    "optimizedApproach": "Use a hash map to store each element's value and index. For each element, check if its complement (target - current) exists in the map.\n\nTime: O(n), Space: O(n)",
    "timeComplexity": "O(n)",
    "spaceComplexity": "O(n)",
    "patterns": ["Hash Map Lookup", "Complement Search"],
    "interviewNotes": "Key clarifications to ask:\n1. Can elements be reused? (No)\n2. Is there exactly one solution? (Yes)\n3. Can there be negative numbers? (Yes)\n\nFollow-up: If array is sorted → use Two Pointers for O(1) space.",
    "commonMistakes": "1. Forgetting to check that the two indices are different\n2. Adding the current element to the map before checking for complement (allows self-pairing)\n3. Not handling edge case of empty array",
    "revisionNotes": "**Key Insight:** Trade space for time. Store what you've seen, look for what you need.\n**Pattern:** Single-pass hash map with complement lookup."
  },
  "submittedAt": "2026-06-22T10:15:30Z"
}
```

---

## 8.4 Analytics APIs

### `GET /analytics/summary` — Dashboard Summary

**Auth:** Required

**Response** `200 OK`:
```json
{
  "totalSolved": 142,
  "easySolved": 50,
  "mediumSolved": 65,
  "hardSolved": 27,
  "currentStreak": 12,
  "longestStreak": 34,
  "thisWeekSolved": 7,
  "thisMonthSolved": 28
}
```

---

### `GET /analytics/heatmap` — Activity Heatmap

**Auth:** Required
**Query Params:** `?year=2026`

**Response** `200 OK`:
```json
{
  "year": 2026,
  "data": [
    { "date": "2026-01-01", "count": 3 },
    { "date": "2026-01-02", "count": 0 },
    { "date": "2026-01-03", "count": 5 },
    { "date": "2026-06-22", "count": 2 }
  ],
  "maxCount": 8
}
```

---

### `GET /analytics/languages` — Language Distribution

**Auth:** Required

**Response** `200 OK`:
```json
{
  "distribution": [
    { "language": "Java", "count": 85, "percentage": 59.9 },
    { "language": "Python", "count": 40, "percentage": 28.2 },
    { "language": "C++", "count": 17, "percentage": 11.9 }
  ]
}
```

---

### `GET /analytics/topics` — Topic Performance

**Auth:** Required

**Response** `200 OK`:
```json
{
  "topics": [
    { "topic": "Array", "solved": 35, "total": 40, "percentage": 87.5 },
    { "topic": "Dynamic Programming", "solved": 12, "total": 30, "percentage": 40.0 },
    { "topic": "Graph", "solved": 8, "total": 20, "percentage": 40.0 },
    { "topic": "Tree", "solved": 15, "total": 25, "percentage": 60.0 },
    { "topic": "String", "solved": 20, "total": 28, "percentage": 71.4 }
  ]
}
```

---

## 8.5 Notes APIs

### `POST /notes` — Create Note

**Auth:** Required

**Request:**
```json
{
  "solutionId": 1,
  "content": "## Key Insight\nThe complement approach avoids nested loops. Always ask: *can I trade space for time?*",
  "noteType": "PERSONAL",
  "reminderAt": "2026-07-01T09:00:00Z"
}
```

**Response** `201 Created`:
```json
{
  "id": 42,
  "solutionId": 1,
  "content": "## Key Insight\nThe complement approach avoids nested loops. Always ask: *can I trade space for time?*",
  "noteType": "PERSONAL",
  "reminderAt": "2026-07-01T09:00:00Z",
  "createdAt": "2026-06-22T10:20:00Z"
}
```

---

### `GET /notes` — List Notes

**Auth:** Required
**Query Params:** `?solutionId=1&noteType=MISTAKE&page=0&size=20`

**Response** `200 OK`:
```json
{
  "content": [
    {
      "id": 42,
      "solutionId": 1,
      "problemTitle": "Two Sum",
      "content": "## Key Insight\n...",
      "noteType": "PERSONAL",
      "reminderAt": "2026-07-01T09:00:00Z",
      "createdAt": "2026-06-22T10:20:00Z",
      "updatedAt": "2026-06-22T10:20:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 15
}
```

---

### `PUT /notes/{id}` — Update Note

**Auth:** Required

**Request:**
```json
{
  "content": "## Updated Insight\nAlways consider using a hash map for O(1) lookups.",
  "noteType": "REVISION",
  "reminderAt": "2026-07-15T09:00:00Z"
}
```

**Response** `200 OK`

---

### `DELETE /notes/{id}` — Delete Note

**Auth:** Required

**Response** `204 No Content`

---

## 8.6 Search APIs

### `GET /search` — Full-Text Search

**Auth:** Required
**Query Params:** `?q=two+sum&difficulty=EASY&tags=Array,Hash+Table&pattern=Hash+Map&page=0&size=20`

**Response** `200 OK`:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Two Sum",
      "difficulty": "EASY",
      "tags": ["Array", "Hash Table"],
      "patterns": ["Hash Map Lookup"],
      "language": "java",
      "matchScore": 0.95,
      "submittedAt": "2026-06-22T10:15:30Z"
    }
  ],
  "page": 0,
  "totalElements": 3,
  "query": "two sum",
  "appliedFilters": {
    "difficulty": "EASY",
    "tags": ["Array", "Hash Table"]
  }
}
```

---

## 8.7 Repository APIs

### `GET /repositories` — List User Repositories

**Auth:** Required

**Response** `200 OK`:
```json
{
  "repositories": [
    {
      "id": 1,
      "repoName": "LeetCode",
      "repoFullName": "johndoe/LeetCode",
      "defaultBranch": "main",
      "isActive": true
    },
    {
      "id": 2,
      "repoName": "coding-solutions",
      "repoFullName": "johndoe/coding-solutions",
      "defaultBranch": "main",
      "isActive": false
    }
  ]
}
```

---

### `POST /repositories/select` — Select Target Repository

**Auth:** Required

**Request:**
```json
{
  "repoFullName": "johndoe/LeetCode",
  "branch": "main"
}
```

**Response** `200 OK`:
```json
{
  "id": 1,
  "repoFullName": "johndoe/LeetCode",
  "branch": "main",
  "isActive": true,
  "message": "Repository selected successfully"
}
```

---

### `GET /repositories/active` — Get Active Repository

**Auth:** Required

**Response** `200 OK`:
```json
{
  "id": 1,
  "repoName": "LeetCode",
  "repoFullName": "johndoe/LeetCode",
  "defaultBranch": "main",
  "isActive": true,
  "solutionCount": 142
}
```

---

## 8.8 Error Response Format

All errors follow a consistent structure:

```json
{
  "timestamp": "2026-06-22T10:15:30Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "SYNC_DUPLICATE",
  "message": "Solution for problem 'Two Sum' in 'java' already synced",
  "path": "/api/v1/sync/submit",
  "traceId": "abc-123-def"
}
```

### Error Code Reference

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `AUTH_TOKEN_EXPIRED` | 401 | JWT access token has expired |
| `AUTH_TOKEN_INVALID` | 401 | JWT token is malformed or signature invalid |
| `AUTH_REFRESH_EXPIRED` | 401 | Refresh token has expired |
| `OAUTH_CODE_INVALID` | 401 | GitHub authorization code is invalid |
| `FORBIDDEN` | 403 | User does not own the requested resource |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource does not exist |
| `SYNC_DUPLICATE` | 422 | Solution already synced for this problem + language |
| `VALIDATION_FAILED` | 422 | Request body failed validation |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `GITHUB_API_ERROR` | 502 | GitHub API returned an error |
| `AI_GENERATION_FAILED` | 502 | AI provider returned an error |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 8.9 API Summary Table

| Method | Endpoint | Auth | Rate Limit | Description |
|--------|----------|------|------------|-------------|
| GET | `/auth/github/url` | No | 10/min | Get OAuth URL |
| POST | `/auth/github/callback` | No | 10/min | Exchange code |
| POST | `/auth/refresh` | No | 20/min | Refresh token |
| POST | `/auth/logout` | Yes | 10/min | Logout |
| POST | `/sync/submit` | Yes | 30/min | Submit solution |
| GET | `/sync/status/{id}` | Yes | 60/min | Sync status |
| GET | `/sync/history` | Yes | 30/min | Sync history |
| GET | `/problems` | Yes | 60/min | List problems |
| GET | `/problems/{id}` | Yes | 60/min | Problem detail |
| GET | `/analytics/summary` | Yes | 30/min | Dashboard stats |
| GET | `/analytics/heatmap` | Yes | 10/min | Activity heatmap |
| GET | `/analytics/languages` | Yes | 30/min | Language stats |
| GET | `/analytics/topics` | Yes | 30/min | Topic stats |
| POST | `/notes` | Yes | 30/min | Create note |
| GET | `/notes` | Yes | 60/min | List notes |
| PUT | `/notes/{id}` | Yes | 30/min | Update note |
| DELETE | `/notes/{id}` | Yes | 30/min | Delete note |
| GET | `/search` | Yes | 60/min | Search |
| GET | `/repositories` | Yes | 20/min | List repos |
| POST | `/repositories/select` | Yes | 10/min | Select repo |
| GET | `/repositories/active` | Yes | 20/min | Active repo |

---

## 8.10 Common Headers

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes (auth endpoints excluded) | `Bearer <jwt_token>` |
| `Content-Type` | Yes (POST/PUT) | `application/json` |
| `X-Request-Id` | No | Client-generated trace ID |

### Response Headers

| Header | Description |
|--------|-------------|
| `X-RateLimit-Limit` | Maximum requests per window |
| `X-RateLimit-Remaining` | Remaining requests in current window |
| `X-RateLimit-Reset` | Unix timestamp when the window resets |
| `X-Request-Id` | Server-generated or echoed trace ID |

---

[← Previous: Database Design](./07_database_design.md) | [Next: Security Architecture →](./09_security_architecture.md)
