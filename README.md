# SICore Framework

English | [Japanese](https://github.com/sugaiketadao/sicore-ja)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

The SICore framework is a lightweight Java framework designed to support **"programming beginners"** and **"code generation by AI"**.

In contrast to feature-rich frameworks, it eliminates annotations and complex configurations, adopting a simple and understandable architecture.

> ⚠️ **Note**: This project is under development. Some parts are incomplete, but basic features are available for trial.

> 📝 **About Translation**: All English text in this project (including documentation and code comments) is translated by AI. Please understand that there may be awkward or difficult-to-understand expressions.

## 🚀 Features

### 1. Simple & Lightweight & Clear
- **JSON-centric design**: Communication between browser and server uses only JSON. No template engines are used, and HTML is treated as static files.
- **Library-less**: External library dependencies are minimized. Tomcat is not required. (Runs on JDK standard features only)
- **URL = Class name**: No routing configuration required. URL paths are directly mapped to Java class names.
  - URL: `/services/exmodule/ExampleListSearch`
  - Class: `com.example.app.service.exmodule.ExampleListSearch`
- **Annotation-less**: Eliminates annotations that tend to obscure code processing content. This makes code execution flow easier to trace.

### 2. Robust Data Processing
- **Io class**: The `Io` class extends `Map<String, String>` to achieve NULL-safe and type-safe data operations.
- **Bug prevention**: Duplicate key checks and existence check features prevent simple mistakes.

### 3. Prototype-driven
- **HTML reuse**: Developers can use HTML mockups created by web designers directly as production code.
- **Original CSS framework**: Provides an original CSS framework that enables responsive design implementation with minimal CSS class specifications.

### 4. AI-native Development
Designed to make it easy for AI coding assistants such as GitHub Copilot to generate high-quality code.
- **AI guidelines**: `.github/copilot-instructions.md` enables AI to accurately understand the framework conventions.
- **Token optimization**: In addition to human-oriented documentation, provides concise AI-specific documentation. Also, in documentation shared with humans, AI-unnecessary parts are enclosed with `<!-- AI_SKIP_START -->` markers to reduce the amount of tokens AI reads.
- **Standardized patterns**: UI implementation patterns and business logic implementation patterns are unified, enabling AI to generate highly accurate code.
- **Traceable code**: The entire framework is provided as source code, making it easier for AI to trace code execution flow.

## 📂 Directory Structure

```
[project root]/
├── docs/                      # Documentation
│   ├── 01-introductions/     # Overview
│   ├── 02-develop-standards/ # Development standards & patterns
│   ├── 03-coding-rules/      # Coding rules
│   ├── 11-api-references/    # API references
│   ├── 21-ai-guides/         # AI prompt guides
│   └── 31-ai-api-references/ # AI API references
├── pages/                     # Frontend (HTML/JavaScript)
│   ├── app/                  # Sample screens
│   └── lib/                  # Framework core (JavaScript/CSS)
├── src/                       # Backend (Java)
│   ├── com/example/app/      # Sample code
│   └── com/onpg/             # Framework core (Java)
└── ai-test-prompts/           # Test AI prompt guides
```

## 📖 Documentation

Refer to the following documentation before starting development.

### Introduction & Overview
- [Introduction for Programmers](docs/01-introductions/01-programmer-introduction.md)
- [Introduction for Managers](docs/01-introductions/02-manager-introduction.md)

### Development Standards
- [Web Page Structure Standard (HTML/JavaScript/CSS)](docs/02-develop-standards/01-web-page-structure.md)
- [Web Service Structure Standard (Java)](docs/02-develop-standards/11-web-service-structure.md)
- [Batch Processing Structure Standard (Java)](docs/02-develop-standards/12-batch-processing-structure.md)
- [Event Coding Patterns](docs/02-develop-standards/21-event-coding-pattern.md)
- [Batch Processing Coding Patterns](docs/02-develop-standards/22-batch-coding-pattern.md)

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
- [AI Prompt Guide (Business Screen Creation)](docs/21-ai-guides/01-ai-prompt-guide.md)
- [AI Prompt Guide (Debug & Fix)](docs/21-ai-guides/02-ai-debug-guide.md)

---

## 🖥️ How to Verify Sample Screens - VS Code
⚠️ The following steps assume an environment with VS Code and Java 11 or later installed.

### 1. Download Project

Download the project from GitHub.

1. Click the "Code" button on the GitHub repository page.
2. Select "Download ZIP".
3. Extract the downloaded ZIP file to any folder.

### 2. Open Project in VS Code

1. Launch VS Code.
2. Select the folder where the ZIP file was extracted via "File" → "Open Folder".
3. When the VS Code "Do you trust the authors?" dialog box appears, select "Yes".

### 3. Start Server

1. Select `src/com/onepg/web/StandaloneServerStarter.java`.
2. Press the `F5` key or select "Debug Java" from the right-click menu.
3. Wait until the startup completion message is displayed in the console.

### 4. Access Sample Screens

Access the following URL in your browser.

```
http://localhost:8000/pages/
```

- A list of sample screens is displayed. You can check the operation by clicking the link for each screen.
- To stop the server, run `src/com/onepg/web/StandaloneServerStopper.java`.

## 5. Sample Code
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB definitions/Test data: `example_db/example_data_create.sql`, `example_db/data/example.dbf`

---

## 🤖 Getting Started with AI Development

The development procedure using AI tools such as GitHub Copilot is as follows.

1. **Create requirements**: Describe the requirements for the feature you want to create in an md file.
2. **Instruct AI**: Specify the md file and instruct AI to code. AI follows `.github/copilot-instructions.md`, reads the necessary documentation, and generates code according to the requirements.
3. **Verify operation**: Verify the operation of the generated code, and if there are any issues, inform AI of the content. AI identifies the cause and fixes the code.

### 🧪 Try It Now

You can experience actual AI coding using the sample requirements included in the repository.
Open the project in VS Code (if you haven't already, refer to "How to Verify Sample Screens" above), and enter the following prompt in Copilot Chat.

> Generate screen functionality with the requirements in `ai-test-prompts/order-prompt.md`.

- AI reads the requirements definition document and automatically generates the necessary HTML, JavaScript, and Java code.
- After generation, refer to [AI Prompt Guide (Debug & Fix)](docs/21-ai-guides/02-ai-debug-guide.md).
- ⚠️ Use an AI agent that strictly adheres to instructions. (As of December 2025, Claude Opus 4.5 is recommended)
- 🚫 Highly creative AI agents may not be suitable for this task.

---
## 💬 Contribution

Currently, this project is under development, so **pull requests are not accepted**. However, bug reports and opinions/requests are welcome! Please create from [Issue](../../issues).

## 💖 Sponsor

If you like this project, please consider supporting on [GitHub Sponsors](https://github.com/sponsors/sugaiketadao). Your support will be used to secure time for coding and documentation creation, and to maintain the development environment and AI tools.

⭐ Just starring us is a great encouragement!

[![Sponsor](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/sugaiketadao)

---
## 📜 License

### Bundled Software

This project includes the following third-party software.

| Software | License | Description |
|-|-|-|
| [SQLite](https://www.sqlite.org/) (`sqlite3.exe`) | Public Domain | SQLite database engine |
| [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc) (`sqlite-jdbc-3.50.2.0.jar`) | Apache License 2.0 | JDBC driver for SQLite |

SQLite is in the public domain and has no restrictions on use, modification, or redistribution.
SQLite JDBC Driver is distributed under Apache License 2.0. Refer to the `licenses/` folder for the full license text.

---
© 2025 sugaiketadao (onepg.com)

