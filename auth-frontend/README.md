# Auth Frontend 

Modern Angular frontend for JWT authentication. Clean standalone components, auto token refresh, and all the auth UI you need. Pairs with the Spring Boot auth backend.

## Tech Stack

- Angular 19 (standalone architecture)
- Tailwind CSS for styling
- RxJS + Angular Signals for state
- JWT handling with auto-refresh
- Reactive Forms with validation
- Modern router with guards

## Features

### Authentication
- Login & Registration with JWT tokens
- Auto-refresh expired tokens (no random logouts)
- Protected routes with guards
- User session management with signals

### Password Reset
- Forgot password flow
- Secure token-based reset
- Clean form validation

### UI/UX
- Minimal, developer-focused design
- Responsive Tailwind styling
- Loading states and error handling
- Clean form components

### Testing
- Unit tests for services and guards
- Modern testing with `provideHttpClientTesting`
- Component interaction tests

## Quick Start

```bash
git clone https://github.com/pw2712gz/auth-frontend.git
cd auth-frontend
npm install
```

**Set API endpoint:**
```ts
// src/environments/environment.ts
export const environment = {
apiUrl: 'http://localhost:8080/api',
production: false,
};
```

**Run it:**
```bash
ng serve
```

App runs at `http://localhost:4200`

## 🔧 Routes

| Route | What it does | Guard |
|-------|--------------|-------|
| `/login` | Login page | Redirect if logged in |
| `/register` | Sign up page | Redirect if logged in |
| `/forgot-password` | Password reset request | Public |
| `/reset-password` | Reset form with token | Public |
| `/dashboard` | Protected main page | Auth required |

## Structure

```
auth-frontend/
├── app/
│   ├── core/              # Services, guards, interceptors
│   │   ├── services/      # Auth, session, guards
│   │   └── interceptors/  # Token handling
│   ├── features/          # Feature modules
│   │   ├── auth/         # Login, register, reset
│   │   └── dashboard/    # Protected pages
│   ├── shared/           # Reusable components
│   │   ├── components/   # UI components
│   │   ├── models/       # TypeScript interfaces
│   │   └── services/     # Utility services
│   └── app.routes.ts     # Route configuration
└── environments/         # Environment configs
```

## Auth Flow

1. **Login/Register** → Get access + refresh tokens
2. **Token Interceptor** → Auto-adds JWT to API calls
3. **Auto Refresh** → Handles expired tokens seamlessly
4. **Route Guards** → Protect pages and handle redirects
5. **Signal Store** → Reactive user state management

## Styling

- **Tailwind CSS** with custom config
- Clean, minimal design (no flashy stuff)
- Monospace fonts for that dev feel
- Dark-friendly color scheme
- Responsive by default

## Testing

```bash
npm test
```

Covers:
- **AuthService** - login, register, token handling
- **Guards** - route protection logic
- **Interceptors** - token refresh flow
- **Components** - form validation and interactions

## Backend

Works with the **Spring Boot 3.5.0 Auth Backend**. Check it out here: [Spring Boot Auth Backend](

## License

MIT - build something cool with it.
