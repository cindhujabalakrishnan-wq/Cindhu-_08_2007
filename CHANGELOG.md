# Changelog

All notable changes to the Insurance Policy Management Platform are documented here.

## Day 11 — MVP

- JWT authentication (register, login, current user) with customer/admin roles and seeded admin
- Policy catalogue: insurance companies and policy types CRUD
- Customer policies: create, list/search, dashboard summary, update, delete
- Premium payments per policy with payment summary
- Renewals: upcoming/expiring lists, request + complete flow, daily renewal scheduler with notifications
- Policy document upload/download/delete backed by local file storage
- Notifications (list, unread count, mark read / read-all)
- Customer profile and self-service user endpoints
- Admin dashboard stats, user/policy/renewal browsers, audit logs
- MySQL 8 + Flyway migrations, Swagger UI (`/swagger-ui.html`)
- React + Vite frontend (dev on `:5173`, API proxy to backend `:8080`)
- CI: backend `mvn verify`, frontend `npm run build`; deploys to Render + Vercel

## Day 41 — Full Product

- Hardening across auth, validation, and error handling (`GlobalExceptionHandler`)
- Pagination-ready list APIs (`PageResponse`) and normalized frontend list handling
- File storage abstraction (`FileStorageService`) with configurable path and max size
- Mail integration for transactional emails (SMTP via env config)
- CORS + frontend-URL configuration for hosted deployments
- Audit logging coverage for security-sensitive operations
- Production profile (`application-prod.yml`) with strict required env vars
- Docker image (`backend/Dockerfile`, Temurin 17 JRE) and Render blueprint (`render.yaml`)
- Root `.env.example`, `.gitignore`, full README with API table and deployment guide

## Day 60 — Enhancement

- Heuristic PDF extraction for policy documents (`HeuristicPolicyDocumentExtractionService` on PDFBox)
- `POST /api/v1/policies/{policyId}/documents/extract-preview` endpoint returning extracted text + suggested policy fields without persisting
- Extracted text stored on `policy_documents.extracted_text` at upload time
- DBML ER diagram, mermaid architecture and class diagrams under `docs/diagrams/`
- `Enhancement_Proposal.md` documenting the heuristic extraction approach, API/DB changes, testing, and deployment
- Upload UX improvements in the frontend document flow
