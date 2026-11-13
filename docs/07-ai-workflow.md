# How AI was used

The brief asks candidates to build with agentic AI tools and to show how. This build used Claude Code (an agentic coding assistant in the terminal and desktop app) as a pair. I set the direction, reviewed every step and made the calls; the assistant drafted, ran and verified.

## The prompt that started it

The whole assessment email and brief were pasted in, with one instruction of my own about the UI:

> "For UI I don't want the generic AI slop UI, so take inspiration from the famous similar platforms out there in the market and use a similar design."

## Workflow

1. **Requirements before code.** The first two commits are documents only: the one-page requirements (with explicit out-of-scope items) and the clarifying questions, each with a working assumption. Writing these before any code stopped the AI from inventing scope.
2. **Design decisions written down with trade-offs** (`docs/03-architecture.md`) before the backend existed, so each later commit could be checked against them.
3. **Test-first for the core maths.** The statistics specification (Excel-compatible percentiles, rounding, overflow) was committed failing, then implemented. Those numbers are what an HR manager will reconcile against Excel, so I wanted them specified independently of the code that produces them.
4. **Verify, don't trust.** Every step ran the real thing: the Maven and Vitest suites after each change, real `curl` calls against the seeded API, and the UI driven in a browser (edit a salary → history row appears → compa-ratio updates). The AI's claims were checked against output, not accepted.
5. **A design pass with explicit constraints.** For the UI, a design skill held the AI to a written "direction contract" (pay band as the single visual idiom, plum/amber palette, Operate-mode density, no card grids) and a mechanical anti-pattern detector. A separate reviewer agent, with no access to the build conversation, critiqued screenshots.

## Where the AI got it wrong, and what caught it

These are real examples from this build:

| Issue | Caught by | Fix |
|---|---|---|
| Test asserted twice on `MockMvcTester`, which silently re-sent the POST, so the second assertion saw a 409 | Failing test | Exchange once, assert on the stored result |
| Server-side field errors (e.g. duplicate email) never rendered in the form, because errors were applied outside Angular's change detection | Component test written for that behaviour | Error display driven by signals; a server error clears once the field is edited |
| Angular 22 scaffold needs a newer Node than the machine had | CLI error | Chose Angular 21 rather than changing the developer's toolchain; recorded in the README prerequisites |
| `npm install` crashed in npm's peer-dependency resolver | Install failure | `legacy-peer-deps` in `frontend/.npmrc`, so CI and Docker behave the same |
| A first draft of the profile fetched the same row twice | Review of the diff | Single query; the USD conversion moved onto `Country` |

## What I would tell another engineer

- Give the AI the **product constraints in writing** (currencies never mixed, history never overwritten). It follows written principles far better than implied ones.
- Make it **prove things by running them**. Most of the bugs above passed a read-through and failed on execution.
- Keep **commits small and reviewable** so a wrong turn costs one commit, not an afternoon.
