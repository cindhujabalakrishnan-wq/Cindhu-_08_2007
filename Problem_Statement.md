# Problem Statement

## Background

Individuals and small businesses hold multiple insurance policies (life, health, auto, home, travel, business) across different carriers. Policy details live in scattered PDFs, emails, and agent records, so renewals are missed, premium dues slip, and there is no single view of cover, spend, or documents.

## Problem

There is no simple self-service system where a customer can:

1. Maintain a single inventory of all insurance policies with cover periods, premiums, nominees, and status.
2. Track premium payments and dues per policy.
3. Get timely renewal reminders before a policy expires.
4. Store policy documents in one place linked to the right policy.
5. Let administrators oversee users, policies, renewals, and security-sensitive actions.

Manual spreadsheets and agent-dependent follow-ups lead to lapsed policies, missed payments, lost documents, and zero auditability.

## Goals

- Customer self-service: register/login, manage policies, payments, renewals, documents, notifications, and profile.
- Back-office administration: dashboard stats, user/policy/renewal oversight, audit trail.
- Automation: daily scan for expiring policies producing renewal requests + notifications.
- Document intelligence: extract usable data from uploaded policy PDFs instead of pure manual entry.
- Deployable, documented product: hosted backend + frontend, cloud MySQL, CI, environment-driven config, API docs.

## Non-Goals (MVP)

- Online payment gateway integration (payments are recorded, not processed).
- Claim filing and settlement workflows.
- Native mobile apps.

## Success Criteria

- A customer can go from registration to holding an active policy with payments, documents, and renewal reminders without admin help.
- An admin can view platform stats, manage users/policies/renewals, and inspect the audit log.
- The stack runs locally (MySQL 8 + `mvnw spring-boot:run` + `npm run dev`) and deploys to Render + Vercel from `main`.
