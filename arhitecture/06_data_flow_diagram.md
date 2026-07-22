# 6. Data Flow Diagram

[← Back to Table of Contents](./00_table_of_contents.md)

---

## 6.1 Primary Flow — Submission Sync

This is the core workflow of the entire system: detecting a LeetCode submission, processing it through AI, and pushing to GitHub.

```mermaid
sequenceDiagram
    participant LC as LeetCode
    participant CS as Content Script
    participant SW as Service Worker
    participant BE as Backend API
    participant Q as Redis Stream
    participant AI as AI Service
    participant GH as GitHub API
    participant DB as MySQL

    LC->>CS: Page mutation (accepted verdict)
    CS->>CS: Extract metadata + code
    CS->>SW: chrome.runtime.sendMessage(submission)
    SW->>SW: Validate + enrich payload
    SW->>BE: POST /api/v1/sync/submit
    BE->>DB: INSERT submission (status=PENDING)
    BE->>Q: Publish SYNC_EVENT
    BE-->>SW: 202 Accepted {syncId}
    SW-->>CS: Show "Syncing..." badge

    Q->>AI: Consume SYNC_EVENT
    AI->>AI: Build prompt with code + metadata
    
    alt OpenAI Available
        AI->>AI: Call OpenAI GPT-4o
    else Fallback
        AI->>AI: Call Gemini 2.0
    end
    
    AI->>DB: UPSERT ai_explanation
    AI->>Q: Publish AI_COMPLETE

    Q->>BE: Consume AI_COMPLETE
    BE->>BE: Build file tree (solution + README + notes)
    BE->>GH: GET repo tree (check existing)
    BE->>GH: PUT contents (create/update files)
    BE->>DB: UPDATE submission (status=SYNCED, commit_sha)
    BE-->>SW: WebSocket push {status: SYNCED}
    SW-->>CS: Show "✓ Synced" badge
```

### Step-by-Step Breakdown

| Step | Component | Action | Details |
|------|-----------|--------|---------|
| 1 | Content Script | Detect accepted submission | MutationObserver watches for verdict DOM element; XHR interceptor catches submission response |
| 2 | Content Script | Extract metadata | Scrapes: title, difficulty, tags, runtime, memory, language, code from DOM |
| 3 | Service Worker | Validate & enqueue | Validates required fields, adds timestamp, checks for duplicates |
| 4 | Backend API | Persist submission | INSERT into `solutions` table with `status=PENDING` |
| 5 | Backend API | Publish event | Publish `SYNC_EVENT` to Redis Stream for async processing |
| 6 | AI Service | Generate explanation | Builds structured prompt, calls LLM, parses response |
| 7 | AI Service | Store explanation | UPSERT into `ai_explanations` table |
| 8 | Sync Service | Build file tree | Creates `solution.java`, `README.md` (from AI), `notes.md` (template) |
| 9 | Sync Service | Push to GitHub | Uses GitHub Contents API to create/update files |
| 10 | Sync Service | Update status | Marks solution as `SYNCED` with commit SHA |
| 11 | Backend | Notify client | WebSocket push to extension with sync completion status |

## 6.2 Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant EXT as Extension Popup
    participant BE as Backend
    participant GH as GitHub OAuth
    participant DB as MySQL
    participant R as Redis

    U->>EXT: Click "Login with GitHub"
    EXT->>BE: GET /api/v1/auth/github/url
    BE-->>EXT: {authUrl, state}
    EXT->>GH: Redirect to GitHub OAuth
    U->>GH: Authorize application
    GH->>BE: Callback with ?code=&state=
    BE->>GH: POST /access_token (exchange code)
    GH-->>BE: {access_token}
    BE->>GH: GET /user (fetch profile)
    BE->>DB: UPSERT user record
    BE->>BE: Generate JWT (access + refresh)
    BE->>R: Store refresh token
    BE-->>EXT: {accessToken, refreshToken, user}
    EXT->>EXT: Store tokens in chrome.storage
```

### OAuth Security Measures

| Measure | Implementation |
|---------|---------------|
| **State Parameter** | Cryptographic random string to prevent CSRF |
| **PKCE** | Code challenge/verifier for authorization code interception prevention |
| **Minimal Scopes** | `repo` (for file access) + `user:email` (for profile) only |
| **Token Encryption** | GitHub access token encrypted with AES-256-GCM before database storage |
| **Session Storage** | JWT stored in `chrome.storage.session` (cleared on browser close) |

## 6.3 Analytics Query Flow

```mermaid
sequenceDiagram
    participant WEB as Dashboard
    participant BE as Backend
    participant R as Redis
    participant DB as MySQL

    WEB->>BE: GET /api/v1/analytics/summary
    BE->>R: GET cache:analytics:{userId}
    
    alt Cache Hit
        R-->>BE: Cached analytics JSON
    else Cache Miss
        BE->>DB: Aggregate queries (COUNT, GROUP BY)
        DB-->>BE: Raw aggregates
        BE->>BE: Compute streaks, heatmap, distributions
        BE->>R: SET cache:analytics:{userId} EX 300
    end

    BE-->>WEB: {totalSolved, easyCount, streak, heatmap, ...}
```

### Cache Invalidation Strategy

| Event | Invalidated Caches |
|-------|-------------------|
| New solution synced | `analytics:summary`, `analytics:heatmap`, `analytics:languages`, `analytics:topics` |
| Solution updated | `analytics:summary`, `analytics:topics` |
| Note created/updated | None (notes not cached) |
| User settings changed | `user:profile` |

## 6.4 Search Flow

```mermaid
sequenceDiagram
    participant WEB as Search Page
    participant BE as Backend
    participant DB as MySQL

    WEB->>WEB: User types query (debounce 300ms)
    WEB->>BE: GET /api/v1/search?q=two+sum&difficulty=EASY&tags=Array
    BE->>BE: Parse query + build SQL
    BE->>DB: SELECT with FULLTEXT MATCH + filters
    DB-->>BE: Matched solutions with relevance score
    BE->>BE: Enrich with tags, patterns, AI status
    BE-->>WEB: {content: [...], totalElements: 3}
```

## 6.5 Complete System Data Flow (Overview)

```mermaid
flowchart LR
    A["🖥️ LeetCode<br/>Submission"] --> B["🧩 Extension<br/>Content Script"]
    B --> C["⚙️ Extension<br/>Service Worker"]
    C --> D["🌐 Backend<br/>Sync API"]
    D --> E["📨 Redis<br/>Stream Queue"]
    E --> F["🤖 AI Service<br/>OpenAI / Gemini"]
    F --> G["💾 MySQL<br/>Database"]
    D --> H["📦 GitHub<br/>API"]
    H --> I["📂 GitHub<br/>Repository"]
    G --> J["📊 Analytics<br/>Dashboard"]
    G --> K["🔍 Search<br/>Knowledge Base"]
    
    style A fill:#ff6b6b,stroke:#c92a2a,color:#fff
    style F fill:#7950f2,stroke:#5f3dc4,color:#fff
    style H fill:#1c7ed6,stroke:#1864ab,color:#fff
    style I fill:#2b8a3e,stroke:#1b5e20,color:#fff
    style J fill:#f59f00,stroke:#e67700,color:#fff
```

---

[← Previous: Component Diagram](./05_component_diagram.md) | [Next: Database Design →](./07_database_design.md)
