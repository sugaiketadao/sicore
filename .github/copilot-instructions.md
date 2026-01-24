# SICore Framework Development Guidelines

You are an assistant for business application development using the SICore framework.
Follow the guidelines below to perform code generation, completion, and suggestions.

## Document Reading Rules
- When reading documents with read_file, skip sections enclosed between `<!-- AI_SKIP_START -->` and `<!-- AI_SKIP_END -->`.
- These marked sections contain supplementary information such as benefits and design philosophy, which are not necessary for AI code generation.
- Read and understand only the unmarked sections (rules, specifications, patterns).

## Steps Before Code Generation

When generating, completing, or suggesting code, always execute the following steps.

### Common Steps (Execute First)

Open `docs/02-develop-standards/21-event-coding-pattern.md` with read_file and read coding patterns for similar processing.
For batch processing, open `docs/02-develop-standards/12-batch-structure.md` with read_file.


### Language-Specific Steps

After completing common steps, execute the following according to the target to be created.

#### When Creating HTML
1. Read similar HTML files under `pages/app/exmodule/` with read_file.
2. Read `docs/31-ai-api-references/02-css-doc.md` with read_file.
3. For new creation, read `docs/02-develop-standards/01-web-page-structure.md` with read_file.

#### When Creating JavaScript
1. Read `docs/31-ai-api-references/01-js-doc.md` with read_file.
2. If there are unclear points, read JavaScript files under `pages/app/exmodule/` listed in `21-event-coding-pattern.md` with read_file.
3. For new creation, read `docs/02-develop-standards/01-web-page-structure.md` with read_file.

#### When Creating Java
1. Read `docs/31-ai-api-references/11-java-doc.md` with read_file.
2. If there are unclear points, read Java files under `src/com/example/app/service/exmodule/` listed in `21-event-coding-pattern.md` with read_file.
For batch processing, read Java files under `src/com/example/app/bat/exmodule/` listed in `22-batch-coding-pattern.md` with read_file.
3. For new creation, read `docs/02-develop-standards/11-web-service-structure.md` listed in `21-event-coding-pattern.md` with read_file.
For batch processing, read `docs/02-develop-standards/12-batch-structure.md` listed in `22-batch-coding-pattern.md` with read_file.

## Absolute Prohibitions
- Generating code before completing "Steps Before Code Generation"
- Using methods, constants, Enums, HTML attributes, or CSS classes that do not exist in the API reference

## Steps After Code Generation

### Coding Rule Check
After completing code generation, read the following coding rules with read_file and check for rule violations.

**When Creating HTML:**
- `docs/03-coding-rules/01-html-css-coding-rule.md`

**When Creating JavaScript:**
- `docs/03-coding-rules/02-javascript-coding-rule.md`

**When Creating Java:**
- `docs/03-coding-rules/11-java-coding-rule.md`
- `docs/03-coding-rules/12-sql-coding-rule.md`

### Additional Check for Java Creation
When creating Java, execute the following steps after code generation.

1. Check for compilation errors and fix them if any.
2. If error resolution takes time, stop fixing and prompt for manual correction.

## Sample Code
- HTML/JS: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB Definition/Test Data: `example_db/example_data_create.sql`

## Code Review
- No need to point out comments inside methods or TODO comments.
- If there is misuse of framework APIs, present the correct usage.
- If there is code that violates rules, present a correction proposal.
