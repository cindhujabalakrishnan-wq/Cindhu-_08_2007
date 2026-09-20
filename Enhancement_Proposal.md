# Enhancement Proposal — Heuristic PDF Extraction for Policy Documents

## 1. Problem

Policy documents arrive as PDFs, but the platform only stores them as opaque files. Every policy field (policy number, carrier, product, cover dates, premium, sum insured, nominee) must be re-typed by hand from the PDF, which is slow and error-prone.

## 2. Limitation

The current document flow (`POST /api/v1/policies/{policyId}/documents`) persists the file and metadata only. There is no text extraction, no field suggestion, and no way to preview what the system can read from a PDF before committing to an upload — so users cannot verify or correct machine-read data.

## 3. Proposal — Heuristic PDF Extraction

Add a heuristic (regex + layout-pattern) extraction layer over Apache PDFBox that:

1. Extracts raw text from an uploaded PDF (`extracted_text`).
2. Applies carrier-agnostic heuristics (labelled-field regexes, date/amount patterns, policy-number shapes) to suggest policy fields.
3. Exposes a preview endpoint so the UI shows extracted text + suggested fields and the user confirms before saving.

No ML model or external service is required; rules are versioned in code and tuned per carrier template over time.

## 4. Technical Approach

- Dependency: Apache PDFBox `2.0.31` (already in `backend/pom.xml`).
- New service: `HeuristicPolicyDocumentExtractionService` implementing `PolicyDocumentExtractionService`:
  - `extractText(InputStream)` → raw text via PDFBox `PDFTextStripper`.
  - `preview(...)` → `{ extractedText, suggestions }` where suggestions cover `policyNumber`, `policyName`, `category`, `startDate`, `expiryDate`, `premiumAmount`, `coverageAmount`, `nomineeName`, insurer name/code hints.
- Heuristics: case-insensitive label regexes (`policy\s*(no|number|id)\s*[:#]?\s*(\S+)`), date patterns (`dd/MM/yyyy`, `yyyy-MM-dd`, `dd-MMM-yyyy`), currency amounts near `premium|sum insured|coverage` labels, plus normalization (whitespace collapse, unicode dash handling).
- `PolicyDocumentService` calls extraction at upload time and stores the raw text; the preview path never persists.
- DTO: `ExtractionPreviewResponse { extractedText, suggestions: Map<String, Object>, warnings: List<String> }`.

## 5. Architecture Changes

- New service beans in `service/` (`PolicyDocumentExtractionService` interface + `HeuristicPolicyDocumentExtractionService` impl); no new infrastructure.
- `PolicyDocumentController` gains the preview route; `PolicyDocumentService` gains an extraction dependency alongside `FileStorageService`.
- Mermaid: `React → Spring (PolicyDocumentController → HeuristicPolicyDocumentExtractionService → PDFBox) → MySQL + local file storage`.
- Carrier-specific rule packs can later be added as strategy beans behind the same interface (e.g. `LicExtractionRules`) without touching controllers.

## 6. API Changes

| Method | Endpoint | Change |
|---|---|---|
| POST | `/api/v1/policies/{policyId}/documents/extract-preview` | NEW — multipart PDF in, `{ extractedText, suggestions, warnings }` out, nothing persisted |
| POST | `/api/v1/policies/{policyId}/documents` | EXTENDED — response `DocumentResponse` now includes extracted-text availability |
| GET | `/api/v1/policies/{policyId}/documents/{id}/download` | unchanged |

Auth: same JWT Bearer scheme. Swagger annotations added for the new endpoint (`/swagger-ui.html`).

## 7. DB Changes

- `policy_documents.extracted_text` (`TEXT`, nullable) — raw extracted text stored at upload time.
- No new tables; no FK changes. Flyway migration adds the column:
  ```sql
  ALTER TABLE policy_documents ADD COLUMN extracted_text TEXT NULL;
  ```
- Cardinality unchanged: `policy_documents.policy_id → insurance_policies.id` (N:1), `policy_documents.uploaded_by_id → users.id` (N:1, nullable).

## 8. Testing

- Unit tests for `HeuristicPolicyDocumentExtractionService` with sample PDFs/text fixtures: policy-number formats, date variants, amount parsing, empty/scanned-PDF (no text layer → warning) cases.
- Controller tests: preview returns suggestions without creating rows; upload stores `extracted_text`.
- Regression: full `mvn verify` suite (H2) must stay green; CI fails on any test failure.
- Manual QA: upload real carrier PDFs (LIC, HDFC-Ergo style) and verify suggestion accuracy + warning behavior.

## 9. Deployment

- No new env vars, services, or storage; PDFBox ships inside the existing jar.
- Memory: text stripping is streaming-friendly; `APP_FILE_MAX_SIZE` (`10MB` default) bounds input size.
- Rollout: backend deploys to Render via existing `render.yaml` build (`./mvnw -B clean package -DskipTests`); frontend document UI calls the preview endpoint with `VITE_API_BASE_URL` unchanged.
- Rollback: safe — the column is nullable and the preview endpoint is additive; old clients ignore it.
