# 12. Folder Structure

[← Back to Table of Contents](./00_table_of_contents.md)

---

## 12.1 Backend (Spring Boot — Java 21)

```
leethub-backend/
├── .github/
│   └── workflows/
│       ├── ci.yml                          # Lint, test, build on PR
│       ├── cd-staging.yml                  # Deploy to staging on merge
│       └── cd-production.yml               # Deploy to prod on tag
├── docker/
│   ├── Dockerfile                          # Multi-stage production build
│   ├── docker-compose.yml                  # Full stack (backend + MySQL + Redis)
│   └── docker-compose.dev.yml              # Dev overrides (hot reload, debug port)
├── docs/
│   ├── api/
│   │   └── openapi.yaml                    # OpenAPI 3.0 specification
│   └── architecture/
│       └── SAD.md                          # Link to this document
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/leethubai/
│   │   │       ├── LeetHubApplication.java              # Main entry point
│   │   │       │
│   │   │       ├── common/                              # ── Shared Module ──
│   │   │       │   ├── config/
│   │   │       │   │   ├── SecurityConfig.java          # Spring Security config
│   │   │       │   │   ├── RedisConfig.java             # Redis connection + serialization
│   │   │       │   │   ├── CorsConfig.java              # CORS policy
│   │   │       │   │   ├── JacksonConfig.java           # JSON serialization config
│   │   │       │   │   ├── AsyncConfig.java             # Thread pool for @Async
│   │   │       │   │   └── WebSocketConfig.java         # WebSocket for real-time sync status
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ApiResponse.java             # Standard success wrapper
│   │   │       │   │   ├── ApiError.java                # Standard error response
│   │   │       │   │   └── PagedResponse.java           # Paginated response wrapper
│   │   │       │   ├── exception/
│   │   │       │   │   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   │   │       │   │   ├── ResourceNotFoundException.java
│   │   │       │   │   ├── SyncException.java
│   │   │       │   │   ├── RateLimitExceededException.java
│   │   │       │   │   └── AiGenerationException.java
│   │   │       │   ├── security/
│   │   │       │   │   ├── JwtTokenProvider.java        # JWT creation + validation
│   │   │       │   │   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│   │   │       │   │   ├── UserPrincipal.java           # Spring Security principal
│   │   │       │   │   └── EncryptionService.java       # AES-256-GCM encrypt/decrypt
│   │   │       │   └── util/
│   │   │       │       ├── SlugUtils.java               # URL-safe slug generation
│   │   │       │       └── DateUtils.java               # Date/timezone helpers
│   │   │       │
│   │   │       ├── auth/                                # ── Auth Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java          # /api/v1/auth/*
│   │   │       │   ├── service/
│   │   │       │   │   ├── AuthService.java             # JWT + session logic
│   │   │       │   │   └── GitHubOAuthService.java      # GitHub OAuth client
│   │   │       │   ├── dto/
│   │   │       │   │   ├── OAuthCallbackRequest.java
│   │   │       │   │   ├── TokenResponse.java
│   │   │       │   │   ├── RefreshRequest.java
│   │   │       │   │   └── UserResponse.java
│   │   │       │   ├── model/
│   │   │       │   │   └── User.java                    # JPA entity
│   │   │       │   └── repository/
│   │   │       │       └── UserRepository.java          # JPA repository
│   │   │       │
│   │   │       ├── sync/                                # ── Sync Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── SyncController.java          # /api/v1/sync/*
│   │   │       │   ├── service/
│   │   │       │   │   ├── SyncService.java             # Sync orchestration
│   │   │       │   │   ├── GitHubSyncService.java       # GitHub API client
│   │   │       │   │   └── FileTreeBuilder.java         # Builds folder/file structure
│   │   │       │   ├── dto/
│   │   │       │   │   ├── SubmitRequest.java
│   │   │       │   │   ├── SyncStatusResponse.java
│   │   │       │   │   └── SyncHistoryResponse.java
│   │   │       │   ├── model/
│   │   │       │   │   ├── Solution.java                # JPA entity
│   │   │       │   │   ├── SyncHistory.java             # JPA entity
│   │   │       │   │   └── SyncStatus.java              # Enum
│   │   │       │   ├── repository/
│   │   │       │   │   ├── SolutionRepository.java
│   │   │       │   │   └── SyncHistoryRepository.java
│   │   │       │   └── event/
│   │   │       │       ├── SyncEventPublisher.java      # Publishes to Redis Stream
│   │   │       │       └── SyncEventConsumer.java       # Consumes from Redis Stream
│   │   │       │
│   │   │       ├── ai/                                  # ── AI Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── AiController.java            # /api/v1/ai/* (optional)
│   │   │       │   ├── service/
│   │   │       │   │   ├── AiService.java               # AI orchestration
│   │   │       │   │   ├── AiProvider.java              # Provider interface
│   │   │       │   │   ├── OpenAiProvider.java          # OpenAI implementation
│   │   │       │   │   ├── GeminiProvider.java          # Gemini implementation
│   │   │       │   │   ├── AiProviderFactory.java       # Factory + fallback logic
│   │   │       │   │   └── PromptTemplateService.java   # Prompt construction
│   │   │       │   ├── dto/
│   │   │       │   │   ├── AiExplanationResponse.java
│   │   │       │   │   └── AiGenerationRequest.java
│   │   │       │   ├── model/
│   │   │       │   │   └── AiExplanation.java           # JPA entity
│   │   │       │   └── repository/
│   │   │       │       └── AiExplanationRepository.java
│   │   │       │
│   │   │       ├── analytics/                           # ── Analytics Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── AnalyticsController.java     # /api/v1/analytics/*
│   │   │       │   ├── service/
│   │   │       │   │   ├── AnalyticsService.java        # Aggregation + caching
│   │   │       │   │   ├── StreakCalculator.java         # Streak computation
│   │   │       │   │   └── HeatmapGenerator.java        # Heatmap data builder
│   │   │       │   ├── dto/
│   │   │       │   │   ├── SummaryResponse.java
│   │   │       │   │   ├── HeatmapResponse.java
│   │   │       │   │   ├── LanguageDistResponse.java
│   │   │       │   │   └── TopicPerfResponse.java
│   │   │       │   ├── model/
│   │   │       │   │   └── AnalyticsSnapshot.java       # JPA entity
│   │   │       │   └── repository/
│   │   │       │       └── AnalyticsSnapshotRepository.java
│   │   │       │
│   │   │       ├── search/                              # ── Search Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── SearchController.java        # /api/v1/search
│   │   │       │   ├── service/
│   │   │       │   │   └── SearchService.java           # Full-text search logic
│   │   │       │   └── dto/
│   │   │       │       ├── SearchRequest.java
│   │   │       │       └── SearchResponse.java
│   │   │       │
│   │   │       ├── notes/                               # ── Notes Module ──
│   │   │       │   ├── controller/
│   │   │       │   │   └── NotesController.java         # /api/v1/notes/*
│   │   │       │   ├── service/
│   │   │       │   │   └── NotesService.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── CreateNoteRequest.java
│   │   │       │   │   ├── UpdateNoteRequest.java
│   │   │       │   │   └── NoteResponse.java
│   │   │       │   ├── model/
│   │   │       │   │   ├── UserNote.java                # JPA entity
│   │   │       │   │   └── NoteType.java                # Enum
│   │   │       │   └── repository/
│   │   │       │       └── UserNoteRepository.java
│   │   │       │
│   │   │       └── repo/                                # ── Repository Module ──
│   │   │           ├── controller/
│   │   │           │   └── RepositoryController.java    # /api/v1/repositories/*
│   │   │           ├── service/
│   │   │           │   └── RepositoryService.java
│   │   │           ├── model/
│   │   │           │   └── Repository.java              # JPA entity
│   │   │           └── repository/
│   │   │               └── RepositoryRepository.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                          # Default config
│   │       ├── application-dev.yml                      # Dev overrides
│   │       ├── application-staging.yml                  # Staging config
│   │       ├── application-prod.yml                     # Production config
│   │       ├── db/migration/                            # Flyway migrations
│   │       │   ├── V1__create_users_table.sql
│   │       │   ├── V2__create_repositories_table.sql
│   │       │   ├── V3__create_solutions_table.sql
│   │       │   ├── V4__create_ai_explanations_table.sql
│   │       │   ├── V5__create_tags_tables.sql
│   │       │   ├── V6__create_user_notes_table.sql
│   │       │   ├── V7__create_analytics_snapshots_table.sql
│   │       │   ├── V8__create_sync_history_table.sql
│   │       │   └── V9__seed_tags_data.sql
│   │       └── prompts/                                 # AI prompt templates
│   │           ├── explanation_prompt.txt
│   │           ├── pattern_prompt.txt
│   │           └── revision_prompt.txt
│   │
│   └── test/
│       └── java/
│           └── com/leethubai/
│               ├── auth/
│               │   ├── AuthControllerTest.java
│               │   └── AuthServiceTest.java
│               ├── sync/
│               │   ├── SyncControllerTest.java
│               │   ├── SyncServiceTest.java
│               │   └── GitHubSyncServiceTest.java
│               ├── ai/
│               │   ├── AiServiceTest.java
│               │   └── PromptTemplateServiceTest.java
│               ├── analytics/
│               │   ├── AnalyticsServiceTest.java
│               │   └── StreakCalculatorTest.java
│               ├── search/
│               │   └── SearchServiceTest.java
│               ├── notes/
│               │   └── NotesServiceTest.java
│               └── integration/
│                   ├── SyncFlowIntegrationTest.java
│                   ├── AuthFlowIntegrationTest.java
│                   └── TestcontainersConfig.java
├── build.gradle                                         # Gradle build config
├── settings.gradle
├── gradle.properties
├── .env.example                                         # Environment variables template
├── .gitignore
├── .editorconfig
└── README.md
```

---

## 12.2 Frontend (React + TypeScript + Tailwind CSS)

```
leethub-frontend/
├── public/
│   ├── index.html
│   ├── favicon.ico
│   ├── logo.svg
│   └── manifest.json
├── src/
│   ├── main.tsx                            # React entry point
│   ├── App.tsx                             # Root component + router
│   ├── vite-env.d.ts                       # Vite type declarations
│   │
│   ├── api/                                # ── API Layer ──
│   │   ├── axiosClient.ts                  # Axios instance + interceptors
│   │   ├── authApi.ts                      # Auth endpoints
│   │   ├── syncApi.ts                      # Sync endpoints
│   │   ├── analyticsApi.ts                 # Analytics endpoints
│   │   ├── problemsApi.ts                  # Problems endpoints
│   │   ├── notesApi.ts                     # Notes endpoints
│   │   ├── searchApi.ts                    # Search endpoints
│   │   └── repositoriesApi.ts              # Repository endpoints
│   │
│   ├── components/                         # ── Reusable Components ──
│   │   ├── layout/
│   │   │   ├── Sidebar.tsx                 # Navigation sidebar
│   │   │   ├── Header.tsx                  # Top header bar
│   │   │   ├── Footer.tsx                  # Footer
│   │   │   └── Layout.tsx                  # Main layout wrapper
│   │   ├── dashboard/
│   │   │   ├── StatsCards.tsx              # Total/Easy/Medium/Hard cards
│   │   │   ├── Heatmap.tsx                 # GitHub-style contribution heatmap
│   │   │   ├── StreakCounter.tsx            # Current + longest streak
│   │   │   ├── LanguageChart.tsx           # Pie/bar chart for languages
│   │   │   ├── TopicRadar.tsx              # Radar chart for topic performance
│   │   │   └── ActivityTimeline.tsx         # Recent activity feed
│   │   ├── problems/
│   │   │   ├── ProblemList.tsx             # Paginated problem table
│   │   │   ├── ProblemCard.tsx             # Single problem card
│   │   │   ├── ProblemDetail.tsx           # Full problem view
│   │   │   ├── CodeViewer.tsx              # Syntax-highlighted code display
│   │   │   ├── AiExplanation.tsx           # AI explanation accordion
│   │   │   └── DifficultyBadge.tsx         # Easy/Medium/Hard badge
│   │   ├── search/
│   │   │   ├── SearchBar.tsx               # Search input with debounce
│   │   │   ├── FilterPanel.tsx             # Difficulty/tag/pattern filters
│   │   │   └── SearchResults.tsx           # Search results list
│   │   ├── notes/
│   │   │   ├── NoteEditor.tsx              # Markdown editor
│   │   │   ├── NoteList.tsx                # Notes list view
│   │   │   └── NoteCard.tsx                # Single note card
│   │   ├── auth/
│   │   │   ├── LoginButton.tsx             # GitHub login button
│   │   │   ├── OAuthCallback.tsx           # OAuth redirect handler
│   │   │   └── ProtectedRoute.tsx          # Auth guard wrapper
│   │   └── common/
│   │       ├── Button.tsx                  # Styled button variants
│   │       ├── Badge.tsx                   # Tag/status badges
│   │       ├── Modal.tsx                   # Modal dialog
│   │       ├── Loader.tsx                  # Loading spinner/skeleton
│   │       ├── Toast.tsx                   # Notification toasts
│   │       ├── EmptyState.tsx              # Empty state illustration
│   │       ├── Pagination.tsx              # Pagination controls
│   │       └── ErrorBoundary.tsx           # React error boundary
│   │
│   ├── hooks/                              # ── Custom Hooks ──
│   │   ├── useAuth.ts                      # Auth state + actions
│   │   ├── useAnalytics.ts                 # Analytics data fetching
│   │   ├── useProblems.ts                  # Problems list + detail
│   │   ├── useNotes.ts                     # Notes CRUD
│   │   ├── useSearch.ts                    # Search with debounce
│   │   ├── useRepositories.ts              # Repository selection
│   │   └── useDebounce.ts                  # Generic debounce hook
│   │
│   ├── context/                            # ── React Context ──
│   │   ├── AuthContext.tsx                 # Auth state provider
│   │   └── ThemeContext.tsx                # Dark/light theme provider
│   │
│   ├── pages/                              # ── Page Components ──
│   │   ├── DashboardPage.tsx               # Main analytics dashboard
│   │   ├── ProblemsPage.tsx                # Problems list page
│   │   ├── ProblemDetailPage.tsx           # Single problem detail
│   │   ├── SearchPage.tsx                  # Search + filter page
│   │   ├── NotesPage.tsx                   # Notes management page
│   │   ├── SettingsPage.tsx                # User settings
│   │   ├── LoginPage.tsx                   # Login landing page
│   │   ├── OAuthCallbackPage.tsx           # OAuth redirect page
│   │   └── NotFoundPage.tsx                # 404 page
│   │
│   ├── types/                              # ── TypeScript Types ──
│   │   ├── auth.ts                         # Auth-related types
│   │   ├── problem.ts                      # Problem + solution types
│   │   ├── analytics.ts                    # Analytics response types
│   │   ├── note.ts                         # Note types
│   │   ├── search.ts                       # Search types
│   │   └── api.ts                          # Generic API types
│   │
│   ├── utils/                              # ── Utilities ──
│   │   ├── constants.ts                    # App constants, API base URL
│   │   ├── formatters.ts                   # Date, number formatters
│   │   └── validators.ts                   # Form validation helpers
│   │
│   └── styles/
│       └── globals.css                     # Tailwind directives + custom CSS
│
├── tailwind.config.ts                      # Tailwind configuration
├── postcss.config.js                       # PostCSS config
├── tsconfig.json                           # TypeScript config
├── tsconfig.node.json                      # Node TypeScript config
├── vite.config.ts                          # Vite build config
├── package.json
├── package-lock.json
├── .env.example
├── .eslintrc.cjs                           # ESLint config
├── .prettierrc                             # Prettier config
├── .gitignore
└── README.md
```

---

## 12.3 Browser Extension (Chrome Manifest V3)

```
leethub-extension/
├── public/
│   ├── manifest.json                       # MV3 manifest
│   ├── icons/
│   │   ├── icon16.png
│   │   ├── icon32.png
│   │   ├── icon48.png
│   │   └── icon128.png
│   └── _locales/
│       └── en/
│           └── messages.json               # i18n strings
│
├── src/
│   ├── background/                         # ── Service Worker ──
│   │   ├── serviceWorker.ts                # Main SW entry point
│   │   ├── syncManager.ts                  # Sync queue + API calls
│   │   ├── authManager.ts                  # Token management + refresh
│   │   ├── offlineQueue.ts                 # IndexedDB offline queue
│   │   ├── alarmHandler.ts                 # chrome.alarms for periodic tasks
│   │   └── webSocketClient.ts              # Real-time sync status
│   │
│   ├── content/                            # ── Content Scripts ──
│   │   ├── contentScript.ts                # Main injection entry point
│   │   ├── leetcodeDetector.ts             # LeetCode page detection
│   │   ├── domObserver.ts                  # MutationObserver setup
│   │   ├── metadataExtractor.ts            # Extract problem metadata
│   │   ├── codeExtractor.ts                # Extract submitted code
│   │   ├── submissionParser.ts             # Parse submission result
│   │   └── ui/
│   │       ├── syncBadge.ts                # Inject sync status badge
│   │       └── syncBadge.css               # Badge styles
│   │
│   ├── popup/                              # ── Popup UI ──
│   │   ├── index.html                      # Popup HTML entry
│   │   ├── Popup.tsx                       # Root popup component
│   │   ├── components/
│   │   │   ├── LoginView.tsx               # Login with GitHub button
│   │   │   ├── DashboardView.tsx           # Quick stats overview
│   │   │   ├── SyncStatus.tsx              # Current sync status
│   │   │   ├── HistoryList.tsx             # Recent sync history
│   │   │   ├── SettingsView.tsx            # Extension settings
│   │   │   └── StatusBadge.tsx             # Sync status indicator
│   │   └── hooks/
│   │       ├── useExtensionAuth.ts         # Extension auth hook
│   │       └── useSyncHistory.ts           # Sync history hook
│   │
│   ├── options/                            # ── Options Page ──
│   │   ├── index.html
│   │   ├── Options.tsx                     # Full settings page
│   │   └── components/
│   │       ├── RepoSelector.tsx            # Repository selection
│   │       ├── AiToggle.tsx                # Enable/disable AI
│   │       └── FolderConfig.tsx            # Folder structure preferences
│   │
│   ├── shared/                             # ── Shared Utilities ──
│   │   ├── types.ts                        # Shared TypeScript types
│   │   ├── constants.ts                    # API URLs, storage keys
│   │   ├── messages.ts                     # Message types for chrome.runtime
│   │   └── storage.ts                      # chrome.storage wrapper
│   │
│   └── utils/
│       ├── encryption.ts                   # Client-side encryption helpers
│       ├── logger.ts                       # Structured logging
│       └── retry.ts                        # Retry with backoff utility
│
├── webpack.config.js                       # Webpack config for extension
├── tsconfig.json
├── package.json
├── .gitignore
└── README.md
```

---

## 12.4 Manifest V3 Configuration

```json
{
  "manifest_version": 3,
  "name": "LeetHub AI",
  "version": "1.0.0",
  "description": "Automatically sync LeetCode solutions to GitHub with AI-powered explanations",
  "permissions": [
    "storage",
    "alarms",
    "identity"
  ],
  "host_permissions": [
    "https://leetcode.com/*",
    "https://api.leethub.ai/*"
  ],
  "background": {
    "service_worker": "background/serviceWorker.js",
    "type": "module"
  },
  "content_scripts": [
    {
      "matches": ["https://leetcode.com/problems/*"],
      "js": ["content/contentScript.js"],
      "css": ["content/ui/syncBadge.css"],
      "run_at": "document_idle"
    }
  ],
  "action": {
    "default_popup": "popup/index.html",
    "default_icon": {
      "16": "icons/icon16.png",
      "32": "icons/icon32.png",
      "48": "icons/icon48.png",
      "128": "icons/icon128.png"
    }
  },
  "options_page": "options/index.html",
  "icons": {
    "16": "icons/icon16.png",
    "48": "icons/icon48.png",
    "128": "icons/icon128.png"
  }
}
```

---

[← Previous: Development Roadmap](./11_development_roadmap.md) | [Next: Deployment Architecture →](./13_deployment_architecture.md)
