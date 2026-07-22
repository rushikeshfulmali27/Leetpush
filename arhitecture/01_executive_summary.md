# 1. Executive Summary

[← Back to Table of Contents](./00_table_of_contents.md)

---

## 1.1 Project Overview

LeetHub AI is a full-stack platform comprising a **Chrome browser extension**, a **React web dashboard**, and a **Spring Boot backend** that together automate the capture, synchronization, analysis, and organization of competitive programming solutions. The system detects successful LeetCode submissions in real time, extracts rich metadata, generates AI-powered explanations via OpenAI/Gemini APIs, and pushes structured solutions to a user's GitHub repository — all without manual intervention.

## 1.2 Problem Statement

Developers preparing for technical interviews solve hundreds of problems across platforms but lack:

- **Automated archival** — Solutions are lost in browser history or scattered across local files.
- **Structured learning** — No systematic way to review approaches, complexities, and patterns.
- **Portfolio visibility** — GitHub contribution graphs don't reflect LeetCode effort.
- **Progress analytics** — LeetCode's native analytics are shallow and non-exportable.

## 1.3 Objectives

| # | Objective | Success Metric |
|---|-----------|----------------|
| O1 | Automatic GitHub sync on accepted submission | < 5 s end-to-end latency |
| O2 | AI-generated explanations for every solution | 95%+ generation success rate |
| O3 | Rich analytics dashboard | Feature parity with GitHub Contributions heatmap |
| O4 | Searchable knowledge base | Sub-200 ms full-text search |
| O5 | Multi-platform extensibility | Plugin architecture supporting ≥ 4 platforms by v2 |

## 1.4 Stakeholders

| Role | Concern |
|------|---------|
| End Users (Developers) | Seamless sync, useful AI notes, privacy |
| Open Source Contributors | Clean architecture, contribution guidelines |
| Startup CTOs / Investors | Scalability, cost efficiency, moat |
| Hackathon Judges | Innovation, technical depth, completeness |

---

[Next: Functional Requirements →](./02_functional_requirements.md)
