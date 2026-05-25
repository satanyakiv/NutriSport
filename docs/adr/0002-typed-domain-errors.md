# 0002 — Typed domain errors with Either / AppError / DomainResult

Status: Accepted (2026-04-01)

## Context

Operations that can fail (network, database, auth) need a uniform way to surface failure. Throwing exceptions across layer boundaries makes failure modes invisible at call sites, and nullable returns conflate "absent" with "failed". A KMP project also can't lean on JVM-only exception machinery.

## Decision

Model fallible results as a sealed `Either<L, R>` (`Left` = failure, `Right` = success), with a typed `AppError` hierarchy (`Network`, `NotFound`, `Unauthorized`, `Unknown`) and the alias `DomainResult<T> = Either<AppError, T>`. Repositories return `DomainResult<T>` (or `Flow<DomainResult<T>>`) — never callbacks, never raw exceptions. The presentation layer wraps results in `UiState<T>` (`Idle` / `Loading` / `Content`). All three live in `:domain` (`shared/util/`).

## Consequences

- Failure is part of every signature; call sites must `fold` over success/error.
- Errors are logged before being wrapped into `Either.Left(AppError.*)`; exceptions are never swallowed silently.
- One mental model from data layer to UI, documented in [`error-handling.md`](../../.claude/rules/error-handling.md).

## Alternatives considered

- **kotlin.Result.** JVM-biased, encourages `getOrThrow`, no typed error domain. Rejected.
- **Arrow `Either`.** Full-featured but a heavy dependency for what a small sealed class covers. Rejected for footprint.
