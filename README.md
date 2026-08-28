<div align="center">

# 🧭 Lostoria — Lost & Found Portal

**A Modern Full-Stack Platform to Report, Track, and Recover Lost Belongings**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Stateless_Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Vite](https://img.shields.io/badge/Vite-Bundler-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)

</div>

---

## 📌 Overview

**Lostoria** is a centralized, full-stack Lost and Found portal built to simplify the recovery of lost belongings on campuses and in public communities. It provides a secure, reactive platform for users to report lost items, log found items with photos, search through active postings in real time, and claim ownership.

The project features a **Spring Boot 3** REST API backend with **JWT-based stateless authentication**, combined with a responsive, modern **React 18** frontend served directly from a unified Spring Boot runtime.

---

## ✨ Key Features

- 🔐 **Stateless JWT Authentication**: Secure user registration and login with bcrypt password hashing and token-based API authorization.
- 📢 **Item Reporting with Photo Uploads**: Multipart form handling (`multipart/form-data`) with in-memory image compression and binary storage.
- ⚡ **Real-Time Client-Side Search & Filter**: Instantly search items by title, description, or location, and toggle between *All Items*, *Lost Items*, and *Found Items*.
- 🖼️ **Public Media Streaming**: Publicly accessible image streaming endpoints allowing item photos to load seamlessly across clients.
- 📦 **Single-Port Full-Stack Deployment**: React frontend bundle is packaged into Spring Boot's static assets, enabling full-stack deployment on a single port (`8080`).
- 🌐 **Full CORS Support**: Pre-configured cross-origin resource sharing for flexible multi-client integrations (web, mobile).

---

## 🏗️ Architecture & Technology Stack

```mermaid
flowchart TD
    subgraph Client["Frontend Layer (React 18 + Vite)"]
        UI["Interactive UI & Feed"]
        Search["Real-Time Search & Filters"]
        Forms["Multipart Photo Upload Forms"]
        AuthModal["JWT Auth Management"]
    end

    subgraph Backend["Backend Layer (Spring Boot 3.4.5)"]
        SecFilter["JWT Filter & SecurityFilterChain"]
        Controllers["REST Controllers (/api/*)"]
        Services["Service Business Logic & Image Compression"]
        Repo["Spring Data JPA Repositories"]
    end

    subgraph Data["Database Layer"]
        Postgres[("PostgreSQL Database (Supabase / Local)")]
    end

    Client -->|HTTP / JSON & Multipart| SecFilter
    SecFilter --> Controllers
    Controllers --> Services
    Services --> Repo
    Repo --> Postgres
