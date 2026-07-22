# LeetHub AI — Frontend

React 19 + TypeScript + Tailwind CSS frontend for LeetHub AI platform.

## Quick Start

### Prerequisites
- Node.js 21+
- npm or yarn

### Local Development

1. **Set up environment:**
   ```bash
   cp .env.example .env
   # Edit .env.local with your backend API URL
   ```

2. **Install dependencies:**
   ```bash
   npm install --frozen-lockfile
   ```

3. **Start dev server:**
   ```bash
   npm run dev
   ```

   Frontend will be available at `http://localhost:5173`

## Project Structure

```
src/
├── api/                 # API client and endpoints
├── components/
│   ├── auth/           # Authentication components
│   ├── layout/         # Layout wrapper
│   ├── dashboard/      # Dashboard components
│   ├── problems/       # Problem list/detail
│   ├── search/         # Search interface
│   ├── notes/          # Notes management
│   └── common/         # Reusable components
├── contexts/           # React contexts (Auth)
├── hooks/              # Custom React hooks
├── pages/              # Page components
├── types/              # TypeScript types
├── utils/              # Utility functions
├── styles/             # Global styles
└── App.tsx             # Root component
```

## Available Scripts

```bash
# Development
npm run dev              # Start Vite dev server

# Build
npm run build            # Build for production

# Linting
npm run lint             # Run ESLint

# Preview
npm run preview          # Preview production build locally
```

## Configuration

### Environment Variables

Create `.env` or `.env.local`:

```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_DEBUG=false
```

Different environments:
- **Development**: `http://localhost:8080/api/v1`
- **Staging**: `https://api-staging.leethub.ai/api/v1`
- **Production**: `https://api.leethub.ai/api/v1`

## Features

### Pages
- **Dashboard** — Summary stats, heatmap, streaks
- **Problems** — List all solved problems with filters
- **Problem Detail** — Full solution with AI explanation
- **Search** — Full-text search with filters
- **Notes** — Personal notes and revision tracking
- **Settings** — Repository selection, preferences

### Components
- Error Boundary for crash handling
- Protected routes with auth checks
- Toast notifications
- Loading skeletons
- Form validation

## Authentication

Uses GitHub OAuth 2.0 flow:
1. User clicks "Login with GitHub"
2. Redirected to GitHub authorization
3. GitHub redirects back to `/oauth/callback`
4. Tokens stored in localStorage
5. Automatic token refresh on expiry

**Security:**
- Tokens stored in httpOnly cookies (frontend can't access)
- JWT with 1-hour expiry
- Refresh token with 30-day expiry
- Automatic redirect to login on 401

## API Integration

All API calls use `axiosClient`:

```typescript
import axiosClient from '../api/axiosClient';

// Automatically adds JWT auth header
const response = await axiosClient.get('/problems');
```

Features:
- Automatic JWT injection
- Token refresh on 401
- 30-second timeout
- Error handling interceptor

## Testing

```bash
# (Tests to be added)
npm run test
```

## Building for Production

```bash
# Build optimized bundle
npm run build

# Output in dist/ directory
# Serve with: npx serve -s dist
```

## Docker

Build and run with Docker:

```bash
# Development
docker build -f Dockerfile -t leethub-frontend:dev .
docker run -p 3000:3000 leethub-frontend:dev

# Production (see docker-compose.prod.yml)
```

## Performance

- Code splitting with React Router
- Lazy loading of routes
- Optimized images and assets
- Tailwind CSS purging unused styles
- Service Worker for offline support (future)

## Browser Support

- Chrome/Edge: Latest 2 versions
- Firefox: Latest 2 versions
- Safari: Latest 2 versions

## Troubleshooting

### Port 5173 already in use
```bash
lsof -i :5173
kill -9 <PID>
```

### "Cannot find module" errors
```bash
rm -rf node_modules package-lock.json
npm install --frozen-lockfile
```

### Build fails
```bash
npm run build -- --debug
```

## Contributing

1. Create feature branch: `git checkout -b feature/foo`
2. Make changes and test: `npm run dev`
3. Lint before commit: `npm run lint`
4. Push and open PR

## License

MIT
