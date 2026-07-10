# FYO — Project Progress

**Last updated:** 2026-07-10  
**Branch:** `main` @ `d27b55d` (synced with `origin/main`)

Find Your Opponent (FYO) — Spring Boot + React/Vite sports matchmaking app (SWE2026 final project, 5-person team).

---

## Recent merges

| PR | Branch | What landed |
|----|--------|-------------|
| #78 | `feat/friends` | Friend requests API, Friends page, profile actions, dashboard panel |
| #79 | `feat/match-accept-chat` | Match listings backend (post/respond/accept → match + auto chat) |
| #80 | `feat/tier1-matches-listings-chat` | Matches page UI, chat polish (pagination + deep links), team/match chat entry points |
| #81 | `fix/captain-only-approve-reject` | Join-request accept/decline restricted to team captain |

---

## Done on `main`

### Backend (`http://localhost:8081`)

| Area | Endpoints / behavior | Auth |
|------|----------------------|------|
| **Auth** | `POST /api/auth/signup`, `POST /api/auth/login` | Firebase Bearer |
| **Onboarding** | `POST /api/onboarding`, `GET /api/onboarding/status` | `?userId=` param |
| **Teams** | CRUD-ish: list, detail, create, join, `/mine`, `/my-requests`, accept/decline join requests | `?userId=` param; captain check on accept/decline (PR #81) |
| **Match listings** | Browse open, post, respond, list responses, accept/decline | Browse public; mutations Bearer |
| **Matches** | List, get, cancel | Cancel uses `?actingUserId=` |
| **Chat** | Conversations (direct, team, by-match), paginated messages, STOMP `/ws` | Bearer |
| **Friends** | List, search status, send/accept/decline/cancel/unfriend | Bearer |
| **Profiles** | `GET/PUT /api/profiles/me`, `GET /api/profiles/{id}` (sports, match history, reviews, rating) | Bearer on `/me` |
| **Admin** | Users/teams/sports list, archive, create sport, delete user | `?adminUserId=` param |
| **Users** | `GET /api/users?q=` (player search) | Public |

### Frontend (`http://localhost:5173`)

| Area | Routes | Notes |
|------|--------|-------|
| **Marketing** | `#/`, `#/home` | Landing page |
| **Auth** | `#/login`, `#/signup`, `#/onboarding` | Firebase session |
| **Public teams** | `#/teams`, `#/teams/:id` | Redirects to app when signed in |
| **App shell** | `#/app`, `#/app/teams`, `#/app/teams/:id`, `#/app/my-teams`, `#/app/friends`, `#/app/matches`, `#/app/profile`, `#/app/admin` | Sidebar nav |
| **Chat** | `#/chat`, `#/chat/:conversationId`, `#/chat/match/:matchId` | Top bar link; not in sidebar |
| **Profiles** | `#/profile/:userId` | Public profile + friend actions |

**Key pages:** Dashboard (matches snippet + friend requests), Teams browse/detail, Friends (search + requests), **Matches** (listings + scheduled tabs), Profile edit, Admin, ChatView (pagination, deep links, offline send fallback).

### Database (Flyway V1–V9)

| Ver | Purpose |
|-----|---------|
| V1 | Core schema: users, sports, teams, matches, match_results, user_reviews |
| V2 | Seed sports |
| V3 | Seed mock users/teams |
| V4 | Join requests |
| V5 | Match listings + responses |
| V6 | Seed 1v1 matches + reviews (profile demo) |
| V7 | Chat tables |
| V8 | Conversation type (MATCH / DIRECT / TEAM) + `team_id` |
| V9 | Friend requests |

### Backend tests (23 test classes)

Auth, onboarding, profiles, admin, teams, join requests, match listings, match cancel, chat (+ STOMP auth), friends, repositories, smoke.

**No frontend tests.**

---

## Not started

| Feature | Notes |
|---------|-------|
| **Achievements** | Required for 5-person team per project spec — no schema, API, or UI |
| **Write reviews** | `user_reviews` exists; profiles **read** seeded reviews only — no `POST` endpoint or form |
| **Match results** | `match_results` entity + repo exist; no submit/confirm/reject API or UI |
| **Global Spring Security** | No `SecurityFilterChain`; auth is manual per controller |
| **GET /api/sports** | Frontend still uses hardcoded sport list (`frontend/src/api/Sports.ts`) |

---

## Known gaps & tech debt

### Inconsistent auth

| Secure (Bearer token) | Legacy (client-supplied id) |
|-----------------------|-------------------------------|
| Chat, friends, profiles `/me`, match-listings mutations | Teams, onboarding, admin, match cancel |

Onboarding still has TODOs to replace `userId` query param with token context.

### Orphan / stale code

- `frontend/src/teams/TeamDetail.tsx` — drawer component, **not routed**; app uses `TeamDetailPage.tsx`
- `feat/chat-frontend-polish` — superseded by PR #80; safe to close
- `feat/matches-frontend` — superseded by PR #80; optional cherry-pick: broader `MatchServiceTests` from that branch

### Submission / docs

- Root `README.md` has run instructions but no feature matrix, API overview, or team-member responsibilities
- No dedicated submission README for grading (demo accounts, screencast links, etc.)

### UX polish (optional)

- Chat not in app sidebar (only TopBar)
- Dashboard matches panel could link to `#/app/matches`
- Admin gate is UI-only (`user.isAdmin`); API trusts `adminUserId` param

---

## Prioritized next steps

### Tier 2 — Rubric / grading impact

1. **Achievements** — V10 migration, entity, service, API, profile/dashboard badges (5-person requirement)
2. **Write reviews API + UI** — post-match review form on completed fixtures; update profile rating
3. **Submission README** — graders: how to run, test accounts, feature checklist, who built what
4. **Bearer auth cleanup** — teams, onboarding, admin, match cancel (one PR, touch controllers + frontend callers)

### Tier 3 — Product completeness

5. **Match results flow** — submit score, opponent confirm/reject, reflect on profile
6. **GET /api/sports** — replace hardcoded frontend list
7. **Cherry-pick `MatchServiceTests`** from `feat/matches-frontend` if broader match test coverage is wanted
8. **Delete or repurpose `TeamDetail.tsx`** to avoid confusion

### Tier 4 — Nice to have

9. Chat link in sidebar nav
10. Frontend component tests (Vitest) for critical flows
11. Close stale feature branches after confirming nothing unique remains

---

## Feature matrix (quick reference)

| Domain | Backend | Frontend | DB | Tests |
|--------|---------|----------|-----|-------|
| Auth | ✅ | ✅ | V1 | ✅ |
| Onboarding | ✅ | ✅ | V1 | ✅ |
| Teams | ✅ | ✅ | V1–V4 | ✅ |
| Join requests | ✅ | ✅ | V4 | ✅ |
| Match listings | ✅ | ✅ | V5 | ✅ |
| Matches (scheduled) | ✅ | ✅ | V1,V5,V6 | Cancel only |
| Chat | ✅ | ✅ | V7–V8 | ✅ |
| Friends | ✅ | ✅ | V9 | ✅ |
| Profiles | ✅ | ✅ | V1,V6 | ✅ |
| Reviews | Read-only | Display | V1,V6 | Via profile |
| Admin | ✅ | ✅ | V1 | ✅ |
| Achievements | — | — | — | — |
| Match results | Schema only | — | V1 | — |

---

## Workflow reminder

- Do **not** commit directly to `main` (protected)
- Branch → small commits → push → PR → merge
- Next Flyway migration: **V10**
