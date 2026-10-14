# Documentation Guide

Documentation is part of the feature. A feature without docs is **not done** (see the definition of done in `BUILD_GUIDE.md`).

---

## 1. Why we document

1. **It's how five people build in isolation.** Your API doc is the contract other people rely on. If it's wrong or missing, someone else's work breaks.
2. **Future you will forget.** In two weeks you won't remember why you chose a status code. Your notes will.
3. **Reviewers need it.** A PR with docs is far easier to review and learn from.
4. **The lead will host this app.** Whoever deploys it needs to know exactly what it does and how to run it.
5. **It proves you understand.** If you can write it clearly, you understand it. If you can't, you've found the gap.
6. **It's portfolio material.** A documented project is something you can show an employer.

---

## 2. What to document

You write four things for your feature:

| What | Where | Purpose |
|------|-------|---------|
| **API doc** | `docs/api/<feature>.md` | Every endpoint, its input and output shapes |
| **Decision notes** | `docs/decisions/<feature>.md` | Why you made the important choices |
| **Swagger annotations** | In your controller | Keeps the live Swagger UI readable |
| **Code comments** | In your code | Explain the non-obvious *why* |

The lead owns the main `README.md` (setup and run instructions). If you notice something wrong in it, open an issue.

### Contract first, then code

Write your API doc **before** you write the controller. Then:
- Build to match it.
- If reality changes while you build, update the doc **in the same PR**.
- If the change affects the request or response shape, tell the group and wait for the lead's OK first, because others may depend on it.

---

## 3. How to document an API endpoint

Use this template for **every** endpoint.

````markdown
### POST /api/posts

**Purpose:** Create a new post as the current user.
**Auth:** Requires `X-User-Id` header (temporary).

**Request body**
```json
{ "title": "Why I like Spring", "body": "Dependency injection finally clicked..." }
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| title | string | yes | 1 to 100 characters |
| body | string | yes | 1 to 5000 characters |

**Success: 201 Created**
```json
{
  "id": 6,
  "title": "Why I like Spring",
  "body": "Dependency injection finally clicked...",
  "author": { "id": 3, "username": "ada", "displayName": "Ada L." },
  "createdAt": "2026-10-20T09:30:00Z",
  "updatedAt": "2026-10-20T09:30:00Z"
}
```

**Errors**

| Status | When | Example message |
|--------|------|-----------------|
| 400 | Validation fails | "Request validation failed" (with `fieldErrors`) |
| 401 | `X-User-Id` missing | "No user identified" |

**Notes:** The author is always the current user. The client never sends an author.
````

### Rules for good API docs

- **Show real JSON examples**, not just descriptions. Copy them from an actual Swagger response.
- **Document the errors**, not only the success case. Errors are where most bugs hide.
- **Say who is allowed** (anyone, logged-in user, author only).
- **Keep the standard shapes consistent.** Errors, pages, and `UserSummary` are defined in `BUILD_GUIDE.md`. Link to them instead of redefining them.
- **Match the real behavior.** Test each documented example in Swagger before you submit your PR.

### Suggested file layout

```
docs/
├── api/
│   ├── auth.md
│   ├── profile.md
│   ├── post.md
│   ├── comment.md
│   └── follow.md
└── decisions/
    ├── auth.md
    ├── profile.md
    ├── post.md
    ├── comment.md
    └── follow.md
```

Start each API file with a one-paragraph summary of the feature, then list its endpoints.

---

## 4. How to document decisions

A decision note is a short record of **a choice you made and why**. It's how you prove you can account for every decision.

### When to write one

Whenever someone could reasonably ask "why did you do it that way?" Typical triggers:
- Choosing a relationship type or where to put it
- Choosing a status code for an edge case
- Choosing where a rule lives (controller or service)
- Choosing a DTO shape or a validation limit
- Rejecting an easier or more obvious option

### Template

```markdown
## <Short title of the decision>

**Context:** What problem or question came up?
**Options:** What could we have done? (at least two)
**Decision:** What did you choose?
**Why:** The reasons.
**Trade-off:** What do we give up with this choice?
```

### Example

```markdown
## Comment points to Post, not the other way around

**Context:** A comment belongs to a post. We could store the link on either side.
**Options:** (1) `@ManyToOne Post` on Comment only. (2) A `List<Comment>` on Post as well.
**Decision:** Option 1, with the link on Comment only.
**Why:** The Posts feature belongs to someone else. Adding a list to `Post` would mean
editing their entity, and the two of us would conflict. It also keeps the relationship simple.
**Trade-off:** We can't do `post.getComments()`. We use `CommentRepository` instead.
```

Keep each note to a few lines. Three to six good notes per feature is plenty. **Write them in your own words**, because you will be asked about them.

---

## 5. Swagger annotations

Swagger UI (`/swagger-ui.html`) is generated from your code. A little annotation makes it much more useful:

```java
@Operation(summary = "Create a post", description = "Creates a post authored by the current user")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Post created"),
    @ApiResponse(responseCode = "400", description = "Validation failed")
})
@PostMapping
public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request) { ... }
```

Swagger shows what the code **does**. Your markdown doc states what it's **supposed** to do. If they disagree, you have found a bug in the code or the doc. Fix whichever is wrong.

---

## 6. Code comments

- Comment the **why**, not the **what**. The code already says what it does.

```java
// Bad: increments i
i++;

// Good: Same message for both cases so attackers can't tell which one was wrong
throw new UnauthorizedException("Invalid username or password");
```

- Add a short Javadoc to each public service method: what it does, what it throws.
- Use clear names for classes, methods, and variables, so fewer comments are needed.
- Delete commented-out code. Git remembers it.
- No jokes, venting, or TODOs that never get done. If something needs doing later, open an issue.

---

## 7. When to update your docs

Update your docs **in the same PR** whenever you change:
- an endpoint path, method, or status code
- a request or response field
- a validation rule
- who is allowed to call something
- a decision you previously wrote down

A PR that changes behavior without changing docs will be sent back.

---

## 8. Documentation checklist (add to your PR)

- [ ] `docs/api/<feature>.md` lists every endpoint with request, response, and errors
- [ ] Every JSON example was copied from a real Swagger response
- [ ] Allowed users are stated for each endpoint
- [ ] `docs/decisions/<feature>.md` has notes for the key choices, in my own words
- [ ] Controller methods have Swagger annotations
- [ ] Comments explain the "why", and no commented-out code remains
- [ ] If I changed a contract shape, the lead has approved it and the group knows
