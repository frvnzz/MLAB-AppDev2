# Project Decisions

## Project Overview

**App:** Purrsistence  
A gamified time management and habit tracking app where users stay focused, track progress, and earn rewards (cats, items). Includes social features and weekly statistics.

**Problem:**  
Users struggle with consistency due to high effort, low engagement, and lack of motivation in existing apps.

---

## Technology Stack

- **Language:** Kotlin  
- **Backend / Services:** Firebase  
- **Reasoning:** Kotlin is taught at university → team familiarity. Firebase provides fast setup, auth, and realtime features without needing a custom backend.

---

## Repository Setup

- **Platform:** GitHub  
- **Structure:** Single repository (monorepo)  
- **Reasoning:** Simpler setup, easier collaboration for a small team.

---

## Git Workflow

- **Branches:**  
  - `main` (stable)  
  - `feature/*`, `fix/*`, `chore/*`  
- **Strategy:** Branch from `main`, PR-based workflow  
- **Rules:** No direct pushes to `main`, no force pushes

---

## Further Contributing Guidelines

- Small, focused commits  
- Use **Conventional Commits**  
- PR required for all changes  
- Reviews optional
- Authors resolve comments before merge

---

## Project Management

- **Tool:** Jira  
- **Methodology:** Scrum (required)  
- **Practices:** Sprints, backlog, task breakdown into tickets  
- **Reasoning:** Structured workflow required by course, supports planning and team coordination

---

## Architecture

---

## Frontend / Backend Split

---

## Data Storage

---

## Domain Model

---

## Trade-offs

- **+ Fast development (Firebase)**  
- **+ Familiar tech (Kotlin)**  
- **– Less backend control**  
- **– Firebase lock-in**
