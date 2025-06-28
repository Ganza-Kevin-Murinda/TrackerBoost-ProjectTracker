# ⚡ TrackerBoost – Performance-Optimized Project Tracker System

A continuation of the **BuildMaster Edition**, this lab elevates the secure Spring Boot-based Project Tracker System with **performance profiling, JVM tuning, caching, DTO optimization**, and **real-time observability** using Spring Boot Actuator.

---

## 📈 Key Enhancements in TrackerBoost

- 🔍 **JMeter Load Testing** for real-world traffic simulation
- 🔥 **JVM Profiling** with JProfiler/VisualVM
- 🧠 **Memory & GC Optimization** (G1GC, ZGC)
- ⚡ **DTO Mapping** using MapStruct/Manual techniques
- 🧰 **Caching Layer** (SimpleCache / Caffeine / Redis)
- 📊 **Observability** with Spring Boot Actuator + custom metrics

---

## 🚀 What’s New in This Version

### 🧪 Performance Profiling & JVM Analysis

| Tool       | What It Measured                                  |
|------------|---------------------------------------------------|
| 🔧 JMeter  | API throughput, latency percentiles, error rates  |
| 🧠 JProfiler | Heap usage, CPU hotspots, GC pauses              |
| ⚙️ GC Flags | Custom GC tuning using G1GC, ZGC, etc.           |

### 🧠 JVM Optimization Highlights

| Area              | Optimization                                  |
|-------------------|-----------------------------------------------|
| Object Allocation | Reduced object churn in services              |
| Memory Footprint  | Tuned `Xmx`, GC algorithms                    |
| GC Configuration  | Applied `-XX:+UseG1GC`, enabled heap dumps    |

### 🧱 DTO Mapping Strategy

| Endpoint            | Optimization                                |
|---------------------|---------------------------------------------|
| GET `/projects`     | `ProjectListDTO` (id, name, status only)    |
| GET `/tasks`        | `TaskSummaryDTO` (title, dueDate, status)   |
| Tool Used           | `MapStruct` and utility mappers             |

### 🔥 Caching Improvements

- Configured cache for read-heavy APIs using:
  - `@Cacheable`, TTLs, eviction
  - SimpleCache (dev) → Redis/Caffeine (prod-ready)
- Benchmarked hit/miss ratios

### 📡 Actuator + Monitoring

| Endpoint                | Purpose                           |
|-------------------------|-----------------------------------|
| `/actuator/health`      | Application health check          |
| `/actuator/metrics`     | JVM + app metrics                 |
| `/actuator/heapdump`    | On-demand heap diagnostics        |
| Custom Metrics          | Task counts, cache hits, etc.     |
| Optional Integration    | Prometheus + Grafana              |

---

## 💡 Optimized Architecture Highlights

- 🔄 Lightweight API payloads (no heavy JPA graphs)
- 🧰 Centralized error handling with performance-friendly stack traces
- ⚙️ Clean mapper classes: manual + MapStruct combo
- 🗃 Efficient fetch strategies (Lazy loading, pagination)

---

## 🔧 Profiling & Benchmark Results

| Test                   | Before Optimization | After Optimization |
|------------------------|---------------------|---------------------|
| GET `/projects` (avg)  | 450ms               | **170ms**           |
| Heap Usage             | 300MB avg           | **180MB avg**       |
| GC Pause (G1GC)        | ~150ms              | **~50ms**           |
| CPU Load (load test)   | ~85%                | **~60%**            |

---

## 🗂️ Folder Structure (Updated)

```bash
src
├── main
│   ├── java/com/trackerboost
│   │   ├── config                # Actuator, Cache & GC config
│   │   ├── controller
│   │   ├── dto                   # Optimized DTOs
│   │   ├── mapper                # MapStruct + Manual Mappers
│   │   ├── profiling             # Profiling helpers / monitoring setup
│   │   ├── service
│   │   ├── exception             # Global handler
│   └── resources
│       ├── application.yml       # GC flags, cache config, actuator endpoints
│       └── jmeter                # Test plans
```
## 🧪 JMeter CLI (Sample)

```bash
jmeter -n -t jmeter/project-loadtest.jmx -l reports/results.jtl
```
---

## 🎯 How to Run (Optimized)

```bash
# Clone the lab
git clone https://github.com/Ganza-Kevin-Murinda/TrackerBoost-ProjectTracker.git
cd projecttracker

# Run with custom JVM flags
JAVA_OPTS="-Xmx512m -Xms512m -XX:+UseG1GC" ./mvnw spring-boot:run
```
---

## 🔍 Tools Used

- 🧪 JMeter & JProfiler
- 📦 MapStruct
- ⚙️ Redis
- 🧩 Spring Boot Actuator

## 👨‍💻 Author
> Ganza Kevin Murinda

#### ©️ Profiling & Performance Optimization for NovaTech’s Project Tracker – Lab work 5