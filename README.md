# SICore Framework

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

SICore Framework is a lightweight Java framework designed to support **"programming beginners"** and **"AI-powered code generation"**.

Unlike heavyweight frameworks, SICore eliminates annotations and complex configurations, adopting a simple and easy-to-understand architecture.

> ⚠️ **Note**: This project is under development. Some parts are incomplete, but the basic features are available for you to try.

## 🚀 Features

### 1. Simple, Lightweight & Clear
- **JSON-Centric Design**: Uses only JSON for communication between browser and server. No template engine is used; HTML is treated as static files.
- **Minimal Dependencies**: Minimizes dependencies on external libraries. No Tomcat required. (Runs on standard JDK APIs only)
- **URL = Class Name**: No routing configuration needed. URLs are directly mapped to the Java classes to be executed.
  - URL: `/services/ordermng/OrderListSearch`
  - Class: `com.example.app.service.ordermng.OrderListSearch`
- **Annotation-Free**: Eliminates annotations that tend to obscure processing details. This makes code execution flow easier to trace.

### 2. Robust Data Handling
- **Io Class**: The `Io` class, which extends `Map<String, String>`, provides NULL-safe and type-safe data operations.
- **Bug Prevention**: Duplicate key checks and existence checks prevent simple mistakes.

### 3. Prototype-Driven
- **HTML Reuse**: Developers can use HTML mockups created by web designers directly as production code.
- **Custom CSS Framework**: Provides a custom CSS framework that enables responsive design with minimal CSS class declarations.

### 4. AI-Native Development
GitHub Copilot and other AI coding assistants can easily generate high-quality code with this framework.
- **AI Guidelines**: `.github/copilot-instructions.md` enables AI to accurately understand the framework conventions.
- **Token Optimization**: In addition to human-oriented documentation, we provide concise documentation specifically for AI. Also, in documentation shared with humans, wrapping AI-unnecessary sections with `<!-- AI_SKIP_START -->` markers reduces the token count that AI reads.
- **Standardized Patterns**: Unified patterns for screens and logic enable AI to generate code with high accuracy.

## 📂 Directory Structure

```
[project root]/
├── docs/                      # Documentation
│   ├── 01-introductions/     # Overview
│   ├── 02-develop-standards/ # Development standards & patterns
│   ├── 03-coding-rules/      # Coding rules
│   ├── 11-api-references/    # API references
│   ├── 21-ai-guides/         # AI Prompt Guide
│   └── 31-ai-api-references/ # AI API references
├── pages/                     # Frontend (HTML/JavaScript)
│   ├── app/                  # Example screens
│   └── lib/                  # Framework core (JavaScript/CSS)
├── src/                       # Backend (Java)
│   ├── com/example/app/      # Example code
│   └── com/onpg/             # Framework core (Java)
└── ai-test-prompts/           # AI test prompts
```

## 📖 Documentation

Refer to the following documentation before starting development.

### Introduction & Overview
- [Introduction for Programmers](docs/01-introductions/01-programmer-introduction.md)
- [Introduction for Managers](docs/01-introductions/02-manager-introduction.md)

### Development Standards
- [Web Page Structure Standards ((HTML/JavaScript/CSS)](docs/02-develop-standards/01-web-page-structure.md)
- [Web Service Structure Standards (Java)](docs/02-develop-standards/11-web-service-structure.md)
- [Event-Based Coding Patterns](docs/02-develop-standards/21-event-coding-pattern.md)

### Coding Rules
- [HTML/CSS Coding Rules](docs/03-coding-rules/01-html-css-coding-rule.md)
- [JavaScript Coding Rules](docs/03-coding-rules/02-javascript-coding-rule.md)
- [Java Coding Rules](docs/03-coding-rules/11-java-coding-rule.md)
- [SQL Coding Rules](docs/03-coding-rules/12-sql-coding-rule.md)

### API References
- JSDoc: `docs/11-api-references/01-jsdoc/`
- CSSDoc: `docs/11-api-references/02-cssdoc/`
- JavaDoc: `docs/11-api-references/11-javadoc/`

### AI Prompt Guides
- [AI Prompt Guide (for Business Screen Development)](docs/21-ai-guides/01-ai-prompt-guide.md)
- [AI Prompt Guide (for Debugging and Fixes)](docs/21-ai-guides/02-ai-debug-guide.md)

---

## 🖥️ How to View Example Screens

### 1. Download the Project

Download the project from GitHub.

1. Click the "Code" button on the GitHub repository page.
2. Select "Download ZIP".
3. Extract the downloaded ZIP file to any folder.

### 2. Open the Project in VS Code

1. Launch VS Code.
2. Select "File" → "Open Folder" and choose the folder where you extracted the ZIP file.

### 3. Start the Server

1. Open `src/com/onepg/web/StandaloneServerStarter.java`.
2. Press the `F5` key, or select "Run" → "Start Debugging".
3. Wait until the startup completion message appears in the console.

### 4. Access the Example Screens

Access the following URL in your browser.

```
http://localhost:8000/pages/
```

A list of example screens is displayed. Click on each screen link to verify its operation.

### 5. Sample Code
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB Definitions/Test Data: `example_db/example_data_create.sql`, `example_db/data/example.dbf`

---


## 🤖 Getting Started with AI Development

Follow these steps to develop with AI tools such as GitHub Copilot:

1. **Create Requirements**: Write the requirements for the feature you want to create in a markdown file.
2. **Instruct AI**: Specify the markdown file and instruct the AI to generate code. The AI follows `.github/copilot-instructions.md`, reads the necessary documentation, and generates code according to the requirements.
3. **Verify**: Test the generated code and report any issues to the AI. The AI identifies the cause and fixes the code.

### 🧪 Try It Now

Use the sample requirements included in the repository to experience AI-powered coding.
Enter the following prompt in Copilot Chat:

> Generate screen functionality based on the requirements in `ai-test-prompts/order-prompt.md`.

- The AI reads the requirements document and automatically generates the necessary HTML, JavaScript, and Java code.
- ⚠️ Use an AI agent that strictly follows instructions. (As of December 2025, Claude Opus 4.5 is recommended.)
- 🚫 Highly creative AI agents may not be suitable for this task.

---
## 📜 License

### Bundled Software

This project includes the following third-party software:

| Software | License | Description |
|-|-|-|
| [SQLite](https://www.sqlite.org/) (`sqlite3.exe`) | Public Domain | SQLite database engine |
| [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc) (`sqlite-jdbc-3.50.2.0.jar`) | Apache License 2.0 | JDBC driver for SQLite |

SQLite is in the public domain with no restrictions on use, modification, or redistribution.
SQLite JDBC Driver is distributed under the Apache License 2.0. See the `licenses/` folder for the full license text.

---
© 2025 onepg.com

