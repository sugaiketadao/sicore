# SICore Framework - Introduction for Managers

This document explains the benefits of the framework for managers considering adoption by their development team.

---

## 1. What This Framework Can Do

### Develop Screen Functionality and API Functionality with Same Mechanism

```
Browser              Server
  │                   │
  │------------------>│
  │           Web service processing
  │<------------------│

Server               Server
  │                   │
  │------------------>│
  │           API service processing
  │<------------------│
```

- Operations from screens and API calls can be handled by **the same program**.
- Easy integration with mobile apps and external systems.

---

## 2. Reasons for High Development Efficiency

### 2.1 Prototype Becomes Production Directly

| Traditional Development | This Framework |
|-----------|------------------|
| Create design screen. | Create design screen. |
| Recreate for programming. | **Can use as is.** |
| Verify operation and fix. | Verify operation and fix. |

**Effect**: No conversion work required from design to program.

### 2.2 Few Configuration Files

| Traditional Development | This Framework |
|-----------|------------------|
| Multiple configuration files required. | Minimal configuration needed. |
| URL configuration required. | No URL configuration required (automatic). |
| DB connection configuration is complex. | Only DB connection configuration needed. |

**Effect**: Fewer troubles due to configuration errors.

### 2.3 Unified Coding Style

- Framework enforces coding style.
- Anyone writes with similar structure.
- Easy handover and maintenance.

---

## 3. Compatibility with AI

### Design That Makes It Easy for AI to Create Programs

This framework is designed to be suitable for **AI (generative AI) and programming beginners**.

| Feature | Benefit for AI |
|------|---------------------|
| Clear rules. | Can generate code without hesitation. |
| Unified patterns. | Easy to refer to past examples. |
| Few configuration files. | Configuration errors are less likely. |
| Error prevention built-in. | Bugs are less likely to enter. |
| Entire framework provided as source code. | Easy to trace code execution flow. |

- Just instruct AI to "create a search screen" to generate working code.
- Provide existing samples to automatically create similar functionality.

---

## 4. Stability & Reliability

### 4.1 No External Library Dependencies
External library dependencies are minimized. Tomcat is not required.

| Item | Content |
|------|------|
| Technology Used | Uses standard Java, HTML, JavaScript, CSS. |
| External Dependencies | Limited to only special processing like DB, Excel, PDF. |
| Update Risk | Low (minimal impact from external library updates). |

**Effect**: 
- Easy security patch response.
- Stable long-term operation possible.

### 4.2 Mechanism That Prevents Bugs

The framework itself has the following measures built-in.

- **NULL protection**: Prevents unexpected errors due to empty values.
- **Type checking**: Can detect data type errors early.
- **Duplication check**: Prevents double registration with same name.
- **Automatic rollback**: Prevents data inconsistency on error.

### 4.3 Original CSS (No External Framework)

Design components needed for screen display are also developed in-house.

- No external CSS framework like Bootstrap required.
- Consists of only minimal necessary CSS (single file only).
- Responsive support ready (PC, tablet, smartphone).
- Low learning cost (fewer design elements to remember).

**Effect**:
- No need to respond to external framework version updates.
- Easy to maintain design consistency.
- Fast page loading speed.

---

## 5. Impact on Development Costs

### Work That Can Be Eliminated

| Work | Traditional | This Framework |
|------|------|------------------|
| Screen design conversion | Required | Not required |
| URL configuration | Required | Not required |
| Data conversion code | Required | Not required |
| Error handling implementation | Required | Minimal |
| Framework learning | Long period | Short period |

### Estimated Learning Period

| Target | Period |
|------|------|
| Programmer | About 1-2 weeks. |
| AI (generative AI) | Can respond immediately when given samples. |

---

## 6. Application Domain

### Strong Areas

- Suitable for business systems (data registration, search, update, deletion).
- Suitable for admin screens.
- Suitable for API provision (external system integration).
- Suitable for microservice architecture.

### Cases with Particularly High Impact

- Small to medium scale system development.
- Short-term development.
- Development and maintenance with small team.
- Development utilizing AI.

---

## 7. Summary

| Item | Benefit |
|------|------|
| **Development Efficiency** | Can use prototype directly and reduce configuration work. |
| **Maintainability** | Code is unified and handover is easy. |
| **Stability** | No external dependencies and bug prevention built-in. |
| **AI Utilization** | Clear rules make it easy for AI to generate code. |
| **Cost** | Can shorten learning period and reduce work effort. |

---

## Related Documents

- [Introduction for Programmers](../01-introductions/01-programmer-introduction.md)
