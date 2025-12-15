# SICore Framework Development Guidelines

You are an assistant for business application development using the SICore Framework.
Follow the guidelines below when generating, completing, or suggesting code.

## Document Reading Rules
- When reading documents with read_file, skip sections enclosed by `<!-- AI_SKIP_START -->` and `<!-- AI_SKIP_END -->` markers.
- Sections enclosed by these markers contain supplementary information such as benefits and design philosophy, which are not required for AI code generation.
- Read and understand only the sections not enclosed by markers (rules, specifications, and patterns).

## Pre-Code Generation Steps

Before generating, completing, or suggesting code, always execute the following steps.

### Common Step (Always Execute First)

Open `docs/02-develop-standards/21-event-coding-pattern.md` with read_file and read the coding patterns for similar processing.

### Language-Specific Steps

After completing the common step, execute the following based on the target language.

#### When Creating HTML
1. Read similar HTML files under `pages/app/exmodule/` with read_file.
2. Read `docs/31-ai-api-references/02-css-doc.md` with read_file.
3. For new file creation, read `docs/02-develop-standards/01-web-page-structure.md` with read_file.

#### When Creating JavaScript
1. Read `docs/31-ai-api-references/01-js-doc.md` with read_file.
2. If unclear, read JavaScript files under `pages/app/exmodule/` referenced in `21-event-coding-pattern.md` with read_file.
3. For new file creation, read `docs/02-develop-standards/01-web-page-structure.md` with read_file.

#### When Creating Java
1. Read `docs/31-ai-api-references/11-java-doc.md` with read_file.
2. If unclear, read Java files under `src/com/example/app/service/exmodule/` referenced in `21-event-coding-pattern.md` with read_file.
3. For new file creation, read `docs/02-develop-standards/11-web-service-structure.md` with read_file.

## Strictly Prohibited
- Generating code before completing the "Pre-Code Generation Steps"
- Using methods, HTML attributes, or CSS classes that do not exist in the API references

## Post-Code Generation Steps

### Coding Rules Check
After code generation is complete, read the following coding rules with read_file and verify there are no rule violations.

**When Creating HTML:**
- `docs/03-coding-rules/01-html-css-coding-rule.md`

**When Creating JavaScript:**
- `docs/03-coding-rules/02-javascript-coding-rule.md`

**When Creating Java:**
- `docs/03-coding-rules/11-java-coding-rule.md`
- `docs/03-coding-rules/12-sql-coding-rule.md`

### Additional Checks When Creating Java
When creating Java, execute the following steps after code generation.

1. Check for compile errors and fix them if found.
2. If fixing errors takes too long, stop the fix and prompt for manual correction.

## Sample Code
- HTML/JS: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB Definitions/Test Data: `example_db/example_data_create.sql`

## Code Review
- Do not point out comments within methods or TODO comments.
- If there is misuse of framework APIs, provide the correct usage.
- If there is code that violates rules, provide a fix suggestion.
- If there are JSDoc/JavaDoc that violate rules, provide a fix suggestion.
- If there are unclear or incorrect descriptions in JavaDoc/JSDoc, provide a fix suggestion. (Noun phrases are allowed for class names and method names at the beginning of JavaDoc/JSDoc)
