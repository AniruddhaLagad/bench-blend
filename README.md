# BenchBlend 🎓

**Exam Seating Arrangement System**  
Modern College of Arts, Science and Commerce, Ganeshkhind, Pune - 16 (Autonomous)

---

## What is BenchBlend?

BenchBlend is a production-grade exam seating arrangement system built for Modern College Pune.
It takes two CSV/Excel files as input for each exam day and automatically generates an optimized
seating arrangement for all students while following a strict set of rules.

---

## How It Works

### Input 1 — Exam Schedule CSV

Contains all subjects being examined on a particular date:

| Column | Description |
|---|---|
| Time | Exam time slot (e.g., 10:00 am – 1:00 pm) |
| Subject Code | Unique subject identifier |
| SEM | Semester number |
| Subject Name | Full name of the subject |
| Class | Class identifier (e.g., VI COM, II BBA) |
| Count | Number of students appearing for that subject |
| Version | NEP version (NEP-1 or NEP-2) |

### Input 2 — Room Skeleton CSV

Contains pre-defined blocks with available bench capacity:

| Column | Description |
|---|---|
| Strength | Number of benches in that block |
| Block No | Block number (sequential) |
| Room No | Room identifier (e.g., A-2, L-1) |

---

## Seating Rules

### 1. Bench Isolation Rule
- Students of different subjects/classes must **never share the same bench**
- In **double mode**: Left side (L) of a bench = one subject, Right side (R) = another subject
- Example: `A2L01 to A2L33` → PHP students, `A2R01 to A2R33` → Industrial Economics students
- Same subject students must **never sit on the same bench** as another subject

### 2. Sequential Block Filling
- Blocks are filled sequentially — no jumping around
- A subject's students can spill across multiple blocks and rooms but only **consecutively**
- Example:
  - Block 1 (strength 33): Subject A uses 3 benches → Subject B uses 19 benches → Subject C uses 11 benches (block full)
  - Block 2 (strength 33): Subject C continues with remaining 7 students → next subject fills remaining 26 benches

### 3. Seating Modes

#### Single Mode
- 1 student per bench
- Used when total bench strength is sufficient for all students

#### Double Mode
- 2 students per bench (L-side and R-side)
- L-side → one subject, R-side → another subject (never same subject on both sides)
- Used when bench strength is insufficient for single mode
- Admin can toggle between Single and Double mode from the UI
- System shows available/free blocks dynamically

### 4. Same Time Slot Preference
- Subjects with the same time slot are preferably placed in the same rooms
- This frees up rooms at the earliest possible time
- Mixed time slot rooms are only used when no same-time-slot pair is available to fill a room

### 5. Daily Rotation Rule (No Repeat Rule)
- Within the same exam session (e.g., March–April 2026), a class must **never get the same block/room** on two different exam days
- This ensures fairness — students do not sit in the same spot every day
- The system tracks block usage per class per session in the database
- Resets automatically for the next exam session (e.g., Oct–Nov 2026)

### 6. Optimization Strategy
- Largest subject first (e.g., PHP with 133 students gets allocated before smaller subjects)
- Minimizes block fragmentation
- No side effects on other rules

### 7. Multiple Subjects per Class
- If a class has exams on multiple days (e.g., 5 subjects over 5 days), each day the class must get a **different block/room**
- No two subjects of the same class share the same block within a session

### 8. Output
- Filled skeleton in the same format as Input 2
- One row per block-subject assignment
- If a subject spans 3 blocks → 3 rows in output
- Export available as both **CSV** and **Excel (.xlsx)**

---

## Bench Numbering Pattern

Every bench in every room follows this pattern:

```
{RoomCode}L{BenchNumber}   → Left side
{RoomCode}R{BenchNumber}   → Right side
```

Examples:
- `A2L01`, `A2L02` ... `A2L33` → Left side benches in Room A-2
- `A2R01`, `A2R02` ... `A2R33` → Right side benches in Room A-2
- `L1L01`, `L1R01` → Room L-1 benches

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.x (Java 21) |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15 |
| Auth | Spring Security + JWT |
| Migrations | Flyway (.sql files) |
| Frontend | Next.js 14 (App Router, TypeScript) |
| Styling | Tailwind CSS |
| HTTP Client | Axios |
| Forms | React Hook Form + Zod |
| State | Zustand |
| Build Tools | Maven (backend), npm (frontend) |

---

## Project Structure

```
bench-blend/
├── bench-blend-backend/
│   ├── src/main/java/com/benchblend/
│   │   ├── algorithm/       # Core seating arrangement algorithm
│   │   ├── config/          # Security, JWT, CORS configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Request and Response DTOs
│   │   ├── exception/       # Global exception handling
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data JPA repositories
│   │   └── service/         # Business logic layer
│   └── src/main/resources/
│       ├── db/migration/    # Flyway SQL migration files
│       └── application.properties
├── bench-blend-frontend/
│   └── src/
│       ├── app/             # Next.js App Router pages
│       ├── components/      # Reusable UI components
│       │   └── ui/          # Base UI elements
│       ├── hooks/           # Custom React hooks
│       ├── lib/             # Axios instance and utilities
│       ├── store/           # Zustand stores
│       └── types/           # TypeScript type definitions
└── README.md
```

---

## Database Migrations

All schema changes are tracked via Flyway migration files located at:

```
bench-blend-backend/src/main/resources/db/migration/
```

Migration file naming convention:

```
V1__init_schema.sql
V2__add_some_feature.sql
V3__alter_some_table.sql
```

Never edit an existing migration file. Always create a new one for any schema change.

---

## Exam Session Scope

- One exam session = one semester exam period (e.g., March–April 2026)
- Block rotation history is tracked per session in the database
- A new session resets all history — classes can reuse blocks from previous sessions
- Session is identified by name, start date, and end date

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Node.js 22+
- PostgreSQL 15

### Backend Setup

```bash
cd bench-blend-backend
mvn clean install
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`

### Frontend Setup

```bash
cd bench-blend-frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:3000`

### Environment Variables

**Backend:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bench_blend
spring.datasource.username=benchblend_admin
spring.datasource.password=your_password
app.jwt.secret=your_jwt_secret
```

**Frontend:** `.env.local`

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## Branching Strategy

| Branch | Purpose |
|---|---|
| `main` | Production-ready code only |
| `dev` | Active development branch |
| `feature/*` | Individual feature branches (e.g., `feature/seating-algorithm`) |
| `fix/*` | Bug fix branches |

## Commit Convention

```
feat: add seating algorithm
fix: correct bench numbering logic
chore: update dependencies
docs: update README
refactor: clean up service layer
```

---

## College Info

**Progressive Education Society's**  
Modern College of Arts, Science and Commerce  
Ganeshkhind, Pune - 411016 (Autonomous)

---