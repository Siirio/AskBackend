# Google OAuth setup

ASK uses Google's server-side authorization-code flow. A verified Google email creates or reuses only the `CUSTOMER` role; business owners and staff continue through their dedicated onboarding.

## Google Cloud Console

Create an OAuth 2.0 client with application type **Web application**.

Authorized JavaScript origins:

- `http://localhost:3000`
- `http://localhost:5173`
- `https://ask.com.kz`
- `https://stage.ask.com.kz`
- `https://ask-frontend-stage-ask7.vercel.app`

Authorized redirect URIs:

- `http://localhost:2020/login/oauth2/code/google`
- `https://api-stage.ask.com.kz/login/oauth2/code/google`
- `https://api.ask.com.kz/login/oauth2/code/google`

## Runtime variables

Local:

```dotenv
OAUTH2_GOOGLE_CLIENT_ID=your-web-client-id
OAUTH2_GOOGLE_CLIENT_SECRET=your-web-client-secret
OAUTH2_FRONTEND_REDIRECT_URI=http://localhost:3000/oauth/callback
```

VPS deployment variables:

```dotenv
OAUTH2_PROD_GOOGLE_CLIENT_ID=...
OAUTH2_PROD_GOOGLE_CLIENT_SECRET=...
OAUTH2_STAGE_GOOGLE_CLIENT_ID=...
OAUTH2_STAGE_GOOGLE_CLIENT_SECRET=...
```

Never commit the real client secret. When either the client ID or secret is empty, Google OAuth remains disabled and the backend does not expose the authorization endpoint.

## Frontend flow

The frontend opens `{API_BASE_URL}/oauth2/authorization/google`. After Google verification, the backend redirects to the configured frontend callback with the ASK opaque session token in the URL fragment. The callback removes the fragment immediately, validates the session through `/api/v1/auth/session`, then persists the session.
