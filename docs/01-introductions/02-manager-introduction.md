# SICore Framework Introduction for Managers

This document explains the benefits of this framework for managers considering adoption in their development teams.

---

## 1. What This Framework Can Do

### Develop Screen Features and API Features with the Same Mechanism

```
Browser              Server
  │                   │
  │------------------>│
  │　　　　　　  Web Service
  │<------------------│

Server               Server
  │                   │
  │------------------>│
  │　　　　　　  API Service
  │<------------------│
```

- Operations from screens and API calls can be handled by **the same program**.
- Easy integration with mobile apps and external systems.

---

## 2. Why Development is Efficient

### 2.1 Prototypes Become Production Directly

| Traditional Development | This Framework |
|------------------------|----------------|
| Create design mockup. | Create design mockup. |
| Rebuild for programming. | **Use as-is.** |
| Test and fix. | Test and fix. |

**Effect**: No conversion work needed from design to program.

### 2.2 Fewer Configuration Files

| Traditional Development | This Framework |
|------------------------|----------------|
| Multiple config files required. | Minimal configuration needed. |
| URL configuration required. | No URL configuration (automatic). |
| Complex DB connection settings. | Only DB connection settings needed. |

**Effect**: Reduces troubles caused by configuration errors.

### 2.3 Unified Coding Style

- The framework enforces coding conventions.
- Anyone writes code with the same structure.
- Easy handover and maintenance.

---

## 3. Compatibility with AI

### Designed for Easy AI Code Generation

This framework is designed for **AI (generative AI) and beginner developers**.

| Feature | Benefit for AI |
|---------|----------------|
| Clear rules. | Generates code without confusion. |
| Unified patterns. | Easy to reference past examples. |
| Few config files. | Less likely to cause config errors. |
| Built-in error prevention. | Less likely to introduce bugs. |

- Simply instruct AI to "create a search screen" and it generates working code.
- Provide existing samples and AI automatically creates similar features.

---

## 4. Stability and Reliability

### 4.1 No External Library Dependencies
External library dependencies are minimized. No Tomcat required.

| Item | Description |
|------|-------------|
| Technologies Used | Standard Java, HTML, JavaScript, and CSS. |
| External Dependencies | Limited to special processing such as DB, Excel, and PDF only. |
| Update Risk | Low (minimal impact from external library updates). |

**Effect**: 
- Easy security patch response.
- Long-term stable operation possible.

### 4.2 Built-in Bug Prevention

The framework includes the following safeguards:

- **NULL prevention**: Prevents unexpected errors from empty values.
- **Type checking**: Enables early detection of data type errors.
- **Duplicate checking**: Prevents duplicate registrations with the same name.
- **Auto rollback**: Prevents data inconsistency on errors.

### 4.3 Custom CSS (No External Frameworks)

Design components needed for screen display are also developed in-house.

- No external CSS frameworks like Bootstrap required.
- Minimal CSS only (single file).
- Responsive design included (PC, tablet, smartphone).
- Low learning cost (few design elements to remember).

**Effect**:
- No need to handle external framework version upgrades.
- Easy to maintain design consistency.
- Fast page load speed.

---

## 5. Effects on Development Costs

### Work That Can Be Reduced

| Task | Traditional | This Framework |
|------|-------------|----------------|
| Screen design conversion | Required | Not required |
| URL configuration | Required | Not required |
| Data conversion code | Required | Not required |
| Error handling implementation | Required | Minimal |
| Framework learning | Long period | Short period |

### Estimated Learning Period

| Target | Period |
|--------|--------|
| Programmers | About 1-2 weeks. |
| AI (Generative AI) | Immediate response when samples are provided. |

---

## 6. Application Areas

### Suitable Fields

- Business systems (data registration, search, update, deletion).
- Admin screens.
- API provision (external system integration).
- Microservice architecture.

### Particularly Effective Cases

- Small to medium-scale system development.
- Short-term development.
- Development and maintenance with small teams.
- AI-assisted development.

---

## 7. Summary

| Item | Benefits |
|------|----------|
| **Development Efficiency** | Prototypes can be used directly, reducing configuration work. |
| **Maintainability** | Unified code makes handover easy. |
| **Stability** | No external dependencies, built-in bug prevention. |
| **AI Utilization** | Clear rules make it easy for AI to generate code. |
| **Cost** | Shortened learning period, reduced work hours. |

---

## References

- [Introduction for Programmers](../01-introductions/01-programmer-introduction.md)
