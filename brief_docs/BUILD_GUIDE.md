# NIIT Blog: Build Guide

This guide explains what we are building, how the pieces fit together, and exactly how you build **your** feature without waiting for anyone else.

**Stack:** Java 17+, Spring Boot 3.x, Maven, Spring Data JPA, PostgreSQL (local), Swagger UI for testing.
**Scope:** backend REST API only. No frontend in this phase.

---

## 1. The big picture

NIIT Blog is a small social blogging API. Users register, write posts, comment on each other's posts, and follow each other.

Five features, five owners:

| # | Feature | Package | Owner |
|---|---------|---------|-------|
| A | Auth (register, login) | `auth` | ________ |
| B | Profiles | `profile` | ________ |
| C | Posts and feed | `post` | ________ |
| D | Comments | `comment` | ________ |
| E | Follow and discover | `follow` | ________ |

### Project layout

```
com.niit.blog
├── common/     shared kit (lead only): errors, PageResponse, UserSummary, CurrentUserProvider
├── user/       User entity + UserRepository (lead only)
├── config/     Swagger config, password encoder, seeders (lead only)
├── auth/       Feature A
├── profile/    Feature B
├── post/       Feature C
├── comment/    Feature D
└── follow/     Feature E
```

Inside your feature package, keep it flat and predictable:

```
post/
├── Post.java               (entity)
├── PostRepository.java
├── PostService.java
├── PostController.java
└── dto/
    ├── PostRequest.java
    └── PostResponse.java
```

### How a request flows

```
Client -> Controller -> Service -> Repository -> PostgreSQL
            (DTO in)    (rules)     (JPA)
Client <- Controller <- Service <- Repository
            (DTO out)
```

- **Controller:** HTTP only. Validates input (`@Valid`), calls the service, returns a DTO.
- **Service:** the real rules (who may do what, what counts as a duplicate).
- **Repository:** talks to the database, nothing else.
- **DTOs:** the shape of JSON going in and out. Never return an entity directly.

---

## 2. What the lead provides before you start

You should never be waiting on a teammate, so the lead prepares a **shared starting point** in Session 1. It contains:

- The Spring Boot project, `pom.xml`, `.gitignore`, and `application.properties` (with an `application-local.properties.example` for DB credentials).
- **`User` entity and `UserRepository`**, with these fields: `id`, `username`, `email`, `passwordHash`, `displayName`, `bio`, `createdAt`. Mapped to a table named `users` (because `user` is reserved in PostgreSQL).
- **A starter `Post` entity and `PostRepository`** with `id`, `title`, `body`, `author`, `createdAt`. The Posts owner extends this; everyone else can rely on it from day one.
- **Seed data:** 8 sample users (ids 1 to 8) and 5 sample posts, loaded automatically on startup.
- **Shared kit in `common/`:**
  - `ErrorResponse` and the exceptions `BadRequestException` (400), `UnauthorizedException` (401), `ForbiddenException` (403), `NotFoundException` (404), `ConflictException` (409), plus a `GlobalExceptionHandler`.
  - `PageResponse<T>` for paginated lists.
  - `UserSummary` (`id`, `username`, `displayName`).
  - `CurrentUserProvider.requireUserId()` (see section 5).
- A `PasswordEncoder` bean (BCrypt) for hashing passwords.
- Swagger UI at `http://localhost:8080/swagger-ui.html`.
- Docs templates and the pull request template.

**Frozen fields:** the starter fields above may be **added to, never renamed or removed**. Other people's code depends on them.

---

## 3. Rules for building in isolation

These nine rules are what make five people able to work in parallel.

1. **Contract first.** The endpoints and JSON shapes in section 6 are the agreement. You build to the contract, not to a teammate's code. If you think the contract is wrong, raise it (rule 9) instead of quietly changing it.
2. **Own your package, touch only your package.** You may edit files in your feature package, your tests, and your docs. You may **not** edit another feature's package, or the lead-only areas (`common/`, `user/`, `config/`, `pom.xml`, `application.properties`). Need a change there? Open an issue and tag the lead.
3. **Use the starting kit instead of waiting.** Users 1 to 8 and five posts already exist. Test against those from your first hour.
4. **Relationships go one way: child to parent.** `Comment -> Post`, `Comment -> User`, `Post -> User`, `Follow -> User`. Never add `List<Comment>` to `Post` or `List<Post>` to `User`. Those would force you to edit someone else's entity, which breaks rule 2.
5. **Test with Swagger and the `X-User-Id` header.** To act as user 3, send `X-User-Id: 3`. No login needed, no teammate's code needed.
6. **Sync with `main` every day.** Before you start working, bring `main` into your branch (see `WORKFLOW.md`). Small daily syncs prevent big painful conflicts.
7. **Build in thin slices.** One endpoint per branch and pull request. A tiny merged slice beats a big unmerged feature every time.
8. **Never be idle.** Work through your feature in this order, and when one step is stuck, move to the next: create -> read -> list -> update -> delete -> validation -> error cases -> docs -> tests.
9. **Contract changes go through the lead.** If you must change a request/response shape, post in the group chat first, update your `docs/api/` file in the same PR, and wait for the lead's OK. Other people may be relying on it.

### Database drift

Everyone runs their own local PostgreSQL with `spring.jpa.hibernate.ddl-auto=update`. Hibernate adds new tables and columns but never removes them. If your local database gets confusing after pulling `main` (for example, a column you removed is still there), **drop your local database, recreate it, and restart the app**. The seed data reloads automatically. This is normal, not a disaster.

---

## 4. When you feel blocked

| Situation | What to do |
|-----------|------------|
| "I need `Post` but the Posts owner hasn't merged yet" | Use the starter `Post` and the seeded posts. They exist from day one. |
| "I need to know who the logged-in user is" | Call `CurrentUserProvider.requireUserId()`. It reads `X-User-Id` for now. |
| "I need a dependency in `pom.xml`" | Open an issue and tag the lead. Do not edit `pom.xml` yourself. |
| "My teammate's code broke the app after I pulled `main`" | Tell the group chat right away with the error. Keep working on your own branch. The lead will sort `main`. |
| "I don't understand an error" | Follow the **30-minute rule** in `WORKFLOW.md`: try for 30 minutes, then ask with the help template. Move to another slice while you wait. |
| "I finished early" | Write your tests and docs, then review a teammate's PR. |

---

## 5. Shared conventions

### Current user (temporary)

Until the security lesson, the "logged-in user" is whoever the `X-User-Id` header says. **Never read this header directly in your code.** Always call `CurrentUserProvider.requireUserId()`. When we add real authentication, only that one class changes and your feature keeps working untouched.

### Basics

- Base path: `/api`. JSON uses `camelCase`. IDs are `Long`.
- Timestamps are ISO-8601 in UTC, e.g. `2026-10-20T09:30:00Z` (use `Instant`).
- Pagination query params: `page` (starts at 0) and `size` (default 10, max 50).

### Status codes

| Code | Use it when |
|------|-------------|
| 200 | Successful read or update |
| 201 | Something was created |
| 204 | Successful delete (no body) |
| 400 | Invalid input or a broken rule (e.g. following yourself) |
| 401 | No user identified, or wrong login details |
| 403 | You are identified but not allowed (e.g. editing someone else's post) |
| 404 | The thing doesn't exist |
| 409 | Conflict with existing data (duplicate username, already following) |

### Standard shapes

**Error**
```json
{ "status": 404, "error": "NOT_FOUND", "message": "Post 12 not found" }
```

**Validation error**
```json
{
  "status": 400,
  "error": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "fieldErrors": { "title": "must not be blank" }
}
```

**Page**
```json
{ "content": [ ], "page": 0, "size": 10, "totalElements": 42, "totalPages": 5 }
```

**UserSummary**
```json
{ "id": 3, "username": "ada", "displayName": "Ada L." }
```

---

## 6. Feature guides

Each guide has the same parts: what it does, what you own, the data, the endpoints, the rules, the dependencies, how to work in isolation, the questions you must be able to answer, and your done checklist.

---

### Feature A: Auth (register and login)

**What it does:** lets someone create an account and log in.
**You own:** `com.niit.blog.auth`
**Learning goals:** DTO validation, service-layer rules, duplicate detection, password hashing, choosing correct status codes.

**Data:** you use the lead's `User` entity. You do not change it.

**Endpoints**

`POST /api/auth/register`
```json
// request
{ "username": "ada", "email": "ada@example.com", "password": "secret123" }
// 201 response
{ "id": 9, "username": "ada", "email": "ada@example.com", "createdAt": "2026-10-20T09:30:00Z" }
```

`POST /api/auth/login`
```json
// request
{ "username": "ada", "password": "secret123" }
// 200 response
{ "id": 9, "username": "ada", "displayName": "ada" }
```
In the security lesson, the login response will gain a `token` field. Your service logic stays the same.

**Rules**
- `username`: 3 to 20 characters, letters, digits, underscore. Stored lowercase. Unique.
- `email`: valid format, unique.
- `password`: at least 8 characters. Hash it with the `PasswordEncoder` bean. Never store or return the plain password or the hash.
- On register, set `displayName` equal to `username` and `bio` to an empty string.
- Duplicate username or email -> `409` with a clear message.
- Wrong username or wrong password -> `401` with the **same** message for both ("Invalid username or password"), so an attacker can't tell which one was wrong.

**Depends on:** `User`, `UserRepository`, `PasswordEncoder` (all provided).
**Others depend on you:** nobody blocks on you. Seeded users 1 to 8 exist already.

**Work in isolation:** register a new user through Swagger, then log in as them. Seeded users all use the password `password123`.

**Explain-it-back questions:** Why hash instead of encrypt? Why one error message for both login failures? Why is the email check in the service and not only in the database?

**Done checklist:** both endpoints behave exactly as above; all error cases tested; docs in `docs/api/auth.md`; decision notes written; PR explained.

---

### Feature B: Profiles

**What it does:** shows a user's public profile and lets users update their own.
**You own:** `com.niit.blog.profile`
**Learning goals:** reading and updating an existing entity, DTO mapping, ownership rules.

**Data:** you use `User.displayName` and `User.bio`. You do not change the entity.

**Endpoints**

`GET /api/profiles/{username}`
```json
// 200
{ "id": 3, "username": "ada", "displayName": "Ada L.", "bio": "Writing about Java." }
```

`GET /api/profiles/me` returns the same shape for the user in `X-User-Id`.

`PUT /api/profiles/me`
```json
// request
{ "displayName": "Ada Lovelace", "bio": "Writing about Java and math." }
// 200 response: the updated profile (same shape as GET)
```

**Rules**
- `displayName`: 1 to 50 characters, not blank.
- `bio`: up to 160 characters (empty is allowed).
- Unknown username -> `404`.
- You can only update your own profile. The "me" endpoints make this automatic, because there is no way to name someone else.
- The profile response contains **no** post or follower counts in this version. That avoids depending on other features. Counts are added later (section 7).

**Depends on:** `User`, `UserRepository`.
**Others depend on you:** nobody.

**Work in isolation:** use users 1 to 8 with different `X-User-Id` values.

**Explain-it-back questions:** Why `/me` instead of `/profiles/{id}` for updates? Why do we use a separate response DTO instead of returning `User`?

**Done checklist:** three endpoints work; validation errors return the standard shape; docs in `docs/api/profile.md`; decision notes written.

---

### Feature C: Posts and feed

**What it does:** create, read, edit, and delete posts, and show a global feed.
**You own:** `com.niit.blog.post`
**Learning goals:** your first `@ManyToOne`, pagination and sorting, author-only authorization.

**Data:** extend the starter `Post` entity.

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | generated |
| `title` | String | 1 to 100 chars |
| `body` | String | 1 to 5000 chars, use `TEXT` |
| `author` | `@ManyToOne User` | required |
| `createdAt` | Instant | set on create |
| `updatedAt` | Instant | set on every edit |

**Endpoints**

`POST /api/posts`
```json
// request
{ "title": "Why I like Spring", "body": "Dependency injection finally clicked..." }
// 201 response
{
  "id": 6,
  "title": "Why I like Spring",
  "body": "Dependency injection finally clicked...",
  "author": { "id": 3, "username": "ada", "displayName": "Ada L." },
  "createdAt": "2026-10-20T09:30:00Z",
  "updatedAt": "2026-10-20T09:30:00Z"
}
```

`GET /api/posts?page=0&size=10` returns the **global feed**, newest first, as a Page of the post shape above.

`GET /api/posts/{id}` returns one post.

`PUT /api/posts/{id}` takes `{ "title", "body" }` and returns the updated post. **Author only.**

`DELETE /api/posts/{id}` returns `204`. **Author only.**

**Rules**
- The author is always the user from `CurrentUserProvider`. The client never sends an author.
- Editing or deleting someone else's post -> `403`.
- Unknown post -> `404`.
- Deleting a post also deletes its comments, but that is handled inside the Comments feature's entity mapping. **You do not write any comment code.**

**Depends on:** `User`, `UserRepository`.
**Others depend on you:** Comments needs `Post` and `PostRepository`. Because the starter already exists, nobody waits for you. Just keep the frozen fields.

**Work in isolation:** the 5 seeded posts let you test the feed and pagination immediately.

**Explain-it-back questions:** Why `@ManyToOne` and not a list on `User`? Why is sorting done in the database and not in Java? Why is the author taken from the current user and not from the request body?

**Done checklist:** all five endpoints work; pagination and ordering verified; `403` and `404` cases tested; docs in `docs/api/post.md`; decision notes written.

---

### Feature D: Comments

**What it does:** lets users comment on posts.
**You own:** `com.niit.blog.comment`
**Learning goals:** an entity with two relationships, nested routes, cascading deletes, authorization.

**Data**

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | generated |
| `text` | String | 1 to 1000 chars |
| `post` | `@ManyToOne Post` | required, deleted when the post is deleted |
| `author` | `@ManyToOne User` | required |
| `createdAt` | Instant | set on create |

For "deleted when the post is deleted," look up Hibernate's `@OnDelete(action = OnDeleteAction.CASCADE)` on the `post` relationship. This is the reason the Posts owner never has to know comments exist.

**Endpoints**

`POST /api/posts/{postId}/comments`
```json
// request
{ "text": "Great post!" }
// 201 response
{
  "id": 14,
  "postId": 2,
  "text": "Great post!",
  "author": { "id": 5, "username": "grace", "displayName": "Grace H." },
  "createdAt": "2026-10-20T10:05:00Z"
}
```

`GET /api/posts/{postId}/comments?page=0&size=10` returns a Page of comments, **oldest first**.

`DELETE /api/comments/{id}` returns `204`. Only the **comment's author** may delete it.

**Rules**
- Post doesn't exist -> `404` (use the starter `PostRepository` to check).
- Deleting someone else's comment -> `403`.
- Comments are flat. No replies to comments.

**Depends on:** `Post`, `PostRepository`, `User` (all in the starter kit).
**Others depend on you:** nobody.

**Work in isolation:** the seeded posts (ids 1 to 5) are your test targets from the first hour.

**Explain-it-back questions:** What happens in the database when a post with comments is deleted, and which line of your code decides that? Why only the comment's author may delete? Why check the post exists before saving?

**Done checklist:** three endpoints work; cascade delete verified by deleting a seeded post that has comments; error cases tested; docs in `docs/api/comment.md`; decision notes written.

---

### Feature E: Follow and discover

**What it does:** lets users follow and unfollow each other, see follower lists, and discover people to follow.
**You own:** `com.niit.blog.follow`
**Learning goals:** a self-referencing relationship, unique constraints, custom queries, business rules.

**Data:** a `Follow` entity. We model a user-follows-user relationship as its **own entity** instead of `@ManyToMany`, because it is easier to reason about and lets us store `createdAt`.

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | generated |
| `follower` | `@ManyToOne User` | the person who follows |
| `following` | `@ManyToOne User` | the person being followed |
| `createdAt` | Instant | set on create |

Add a **unique constraint** on the pair (`follower`, `following`) so the database itself refuses duplicates. Map the table as `follows`.

**Endpoints**

`POST /api/users/{username}/follow` returns `201`:
```json
{ "follower": "ada", "following": "grace", "createdAt": "2026-10-20T11:00:00Z" }
```

`DELETE /api/users/{username}/follow` returns `204`.

`GET /api/users/{username}/followers?page=0&size=10` returns a Page of `UserSummary`.

`GET /api/users/{username}/following?page=0&size=10` returns a Page of `UserSummary`.

`GET /api/discover?limit=10` returns a plain list of `UserSummary`:
```json
[ { "id": 6, "username": "linus", "displayName": "Linus T." } ]
```

**Rules**
- Following yourself -> `400`.
- Following someone you already follow -> `409`.
- Unknown username -> `404`.
- Unfollowing someone you don't follow -> `404`.
- **Discover** returns a **random** selection of up to `limit` users, excluding yourself and anyone you already follow. It is not paginated, because random order and pages don't mix.
- Hint for discover: a native `@Query` using `ORDER BY random() LIMIT`, with a `NOT IN` subquery on the follows table. Check your actual table and column names in `psql` before writing it.

**Depends on:** `User`, `UserRepository`.
**Others depend on you:** nobody. Follower counts on profiles come later (section 7).

**Work in isolation:** make users 1 to 8 follow each other through Swagger to build up test data.

**Explain-it-back questions:** Why a `Follow` entity instead of `@ManyToMany`? Why a database unique constraint **and** a service check? Why is discover not paginated?

**Done checklist:** five endpoints work; all rules tested; discover never returns you or people you follow; docs in `docs/api/follow.md`; decision notes written.

---

## 7. After everything is merged

Some nice extras need two features to be finished first, so we do them **together** once both sides are merged, never in isolation:

- Post, follower, and following counts on profiles.
- Comment count on each post.
- A feed of only the people you follow.
- A "posts by this user" endpoint.

These stay on the **later list** along with likes, images, replies, and tags. Nobody starts them early.

---

## 8. Definition of done (every feature)

A feature is done only when **all** of these are true:

- [ ] Every endpoint in the contract works through Swagger.
- [ ] Validation and error cases return the standard shapes.
- [ ] `docs/api/<feature>.md` is complete and matches the real behavior.
- [ ] `docs/decisions/<feature>.md` records your key decisions.
- [ ] Your PR is merged into `main`, and the app starts from a clean database on `main`.
- [ ] You can explain every line you wrote, in your own words.

---

## 9. Proposed rhythm

- **Session 1:** get the app running, learn the PR flow with a guided first PR, practice one safe merge conflict, and a short lesson on JPA relationships. Read your feature guide.
- **Between sessions:** build your feature in thin slices, with a short daily update in the group chat.
- **Session 2:** integration checkpoint. Merge what's ready, fix what's stuck, demo each feature live.
- **Session 3:** the Spring Security and JWT lesson, which replaces the temporary `X-User-Id` header, followed by wrap-up.
