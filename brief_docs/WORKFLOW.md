# Working Together: GitHub and Work Ethic

This is how we work as a team. The tools matter less than the habits, so read both halves.

---

## 1. Why these rules exist

Five people changing one codebase will break each other's work unless we agree on a few simple habits. These rules protect **`main`**, the one branch that must always run. They also train you in the exact workflow professional teams use.

---

## 2. The golden rules

1. **Never push directly to `main`.** Every change goes through a pull request.
2. **`main` must always start.** If your merge would stop the app from running, it is not ready.
3. **One small branch per task.** Not one giant branch per feature.
4. **Sync with `main` every day** before you start working.
5. **Never edit another person's package** without asking (see `BUILD_GUIDE.md`).
6. **Never commit secrets** (passwords, keys, `application-local.properties`).
7. **Never force push**, and never rewrite history on a shared branch.
8. **You must be able to explain every line you commit.**

---

## 3. Branching

- `main` is protected. Nobody commits to it directly.
- Create a branch for each small task, named `feature/<area>-<what>`:

```
feature/post-create-endpoint
feature/comment-delete-rules
feature/follow-discover-query
fix/auth-duplicate-email-message
docs/profile-api-contract
```

- Lowercase, hyphens, short but specific.
- Delete the branch after it is merged.

```bash
git checkout main
git pull origin main
git checkout -b feature/post-create-endpoint
```

---

## 4. Commits

Commit small and often. A good commit does **one thing** and the project still starts after it.

**Message format:** `<area>: <what you did>` in the present tense.

```
post: add create endpoint
post: validate title length
comment: return 404 when post is missing
docs: add follow API contract
```

Bad messages: `update`, `fix`, `stuff`, `final`, `final2`.

Never commit: `target/`, `.idea/`, `.env` files, or `application-local.properties`. The `.gitignore` already covers these. If you ever see them in `git status`, stop and ask.

---

## 5. Your daily routine

**Start of every work session: bring `main` into your branch**
```bash
git checkout main
git pull origin main
git checkout feature/your-branch
git merge main
```
If there are conflicts, see section 7. Resolve them now, while they are small.

**During work:** commit small and often.

**End of every work session: push your branch**
```bash
git push origin feature/your-branch
```
This backs up your work and lets others see it.

---

## 6. Pull requests

### Opening one

1. Push your branch.
2. On GitHub, click **Compare and pull request**.
3. Fill in the template completely (below). An empty template means the PR is not ready.
4. Request your assigned reviewer.

### The PR template

```markdown
## What does this PR do?
(One or two sentences.)

## How to test it
(Steps in Swagger: which endpoint, which X-User-Id, what to expect.)

## Explain it back
(In YOUR OWN words: what does this code do and why does it work?)

## Decisions
(Any choice a reviewer might question, and why you made it.)

## Checklist
- [ ] App starts on a clean database
- [ ] I tested the happy path and the error cases
- [ ] I only changed files in my own package, tests, and docs
- [ ] I updated docs/api and docs/decisions if needed
- [ ] I can explain every line in this PR
```

### Keep PRs small

Aim for **under 300 changed lines**, ideally one endpoint. Small PRs are reviewed faster, break less, and are easier to learn from.

### Reviews

- Every PR needs **one approval** from a teammate (assigned across skill levels by the lead).
- Reviewers respond within **24 hours**. Authors reply to comments within 24 hours.
- **Review checklist:**
  - Does it match the contract in `BUILD_GUIDE.md`?
  - Do validation and error cases return the standard shapes?
  - Is any logic sitting in the controller that belongs in the service?
  - Are authorization rules enforced (403 where needed)?
  - Is there anything you cannot understand? Ask. That is a valid review comment.
- **Tone:** ask questions instead of giving orders. "Why did you choose X here?" teaches both people. "This is wrong" teaches nobody.

### Merging

After one approval and a clean start of the app, the **author** clicks **Squash and merge** and deletes the branch. Authors merge their own PRs because ownership means finishing the job.

---

## 7. Merge conflicts

### What they are

A conflict happens when two people change the **same lines** of the same file. Git can't decide which version wins, so it asks you. It is **normal**, not a mistake.

### How to resolve one

1. Run `git merge main`. Git reports conflicts and lists the files.
2. Open each conflicted file. You will see:
```
<<<<<<< HEAD
your version of the lines
=======
main's version of the lines
>>>>>>> main
```
3. Decide what the final code should be. Often that is **both** changes combined. Delete all three marker lines.
4. Run the app and test your endpoints.
5. Then:
```bash
git add <the files you fixed>
git commit
git push origin feature/your-branch
```

### Habits that prevent most conflicts

- Sync with `main` daily.
- Keep PRs small and merge them quickly.
- Stay inside your own package.
- If you must touch a shared file, tell the group first.

**Not sure how to resolve it?** Don't guess on shared code. Ask for help. You will practice a safe conflict together in Session 1.

---

## 8. If something goes wrong

| Problem | What to do |
|---------|------------|
| You pushed a broken change to `main` | Tell the group immediately. On GitHub, open the merged PR and click **Revert**. Then fix it properly on a new branch. |
| You committed a password or secret | Tell the lead **immediately**. Deleting it in a new commit is not enough, because it stays in history. The password must be changed. |
| You committed on the wrong branch | Don't panic and don't push. Ask for help before you try to undo it. |
| You lost your changes | Ask before running any `git reset` or `git checkout .`. Those can destroy work permanently. |
| The app won't start after a pull | Post the error in the group chat, and try dropping and recreating your local database (see `BUILD_GUIDE.md`). |

Mistakes are expected. **Hiding a mistake** is the only real failure.

---

## 9. Work ethic

### Own your feature
You are the person who knows your feature best. Own the quality, the docs, and the follow-through, and not only the code.

### Daily update
Post three lines in the group chat each day you work:

```
Done: finished POST /api/posts with validation
Doing: GET feed with pagination
Blocked: nothing (or: not sure how to sort by createdAt desc)
```

### The 30-minute rule
Stuck? Try on your own for **30 minutes**, genuinely, not staring at it. Then ask. Don't wait hours, and don't ask in 30 seconds. While waiting for help, switch to another slice of your feature.

### How to ask for help
Use this template so people can actually help you:

```
What I'm trying to do:
What I expected:
What actually happened (exact error message):
What I've already tried:
Link to my branch:
```

### Finish before you polish
Get the happy path working first, then validation, then error cases, then docs, then tidy-up. A working simple version beats a perfect unfinished one.

### Be honest
Say "I don't understand this part" and "I'm behind" early. A team can fix a problem it knows about.

### Respect each other's code and time
No sneaky edits to other people's files. Answer review requests quickly. Give feedback kindly. Celebrate other people's merged PRs.

### Be able to account for every decision
Every choice (a relationship type, a DTO shape, a status code) should have a reason you can state. Write the important ones in `docs/decisions/` (see `DOCUMENTATION.md`).

---

## 10. Using AI properly

You are allowed and encouraged to use AI to learn and build outside sessions. The rule is **"explain it back."**

### Do
- **Try first.** Write your entity and endpoint skeleton yourself before asking AI for anything.
- Use AI to **review** your code, **explain** an error, **unstick** you, or **explain a concept** you don't get.
- Ask good questions:
  - "Explain why this query returns duplicates."
  - "Review my service and point out problems. Don't rewrite it."
  - "What are two ways to do this and the trade-offs?"
- Understand the answer before you use it. Then type or adapt it yourself.

### Don't
- Paste the whole feature description and commit whatever comes back.
- Commit code you can't explain line by line.
- Let AI write your PR description or decision notes. Those must be **your** words.
- Change shared files or the contract because an AI suggested it.

### How we check
- Every PR has an **Explain it back** section in your own words.
- In each session, the lead picks **random PRs** and asks the author to walk through the code live. Be ready to answer: why this line, what happens if it fails, what would break if you removed it?

If you can explain it, it's yours. If you can't, you haven't finished learning it yet, and that's what the next 30 minutes are for.
