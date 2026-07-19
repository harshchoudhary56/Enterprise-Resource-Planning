# Maven & JFrog Artifactory Architecture: School ERP

> A complete guide to understanding repository types and configuring Maven for secure downloading and deployment for the School ERP system.

---

## Part 1: JFrog Artifactory Repository Types

When setting up an enterprise build environment, Artifactory uses three distinct types of repositories to manage the flow of code. Understanding the difference among them is critical for setting up a clean architecture.

---

### 1. Local Repository — *The Private Vault*

A local repository is a **physical storage location** hosted directly on your Artifactory server.

| Property | Detail |
|----------|--------|
| **Purpose** | Stores proprietary artifacts (`.jar`, `.war`) created by your team |
| **Usage** | You **write/deploy** to Local Repositories |

**Your Setup:**
- `school-erp-maven-snapshot-local` → active development
- `school-erp-maven-release-local` → stable releases

---

### 2. Remote Repository — *The Smart Cache*

A remote repository acts as a **caching proxy** for public repositories (like Maven Central).

| Property | Detail |
|----------|--------|
| **Purpose** | Downloads dependencies once and caches them for faster future builds |
| **Usage** | You **read/download** via proxy |

---

### 3. Virtual Repository — *The Single Doorway*

A virtual repository is a **logical grouping** of multiple repositories under one URL.

| Property | Detail |
|----------|--------|
| **Purpose** | Provides a single endpoint instead of multiple repository URLs |
| **Usage** | You **read/download** from Virtual Repositories |

> ❌ You **cannot** deploy to Virtual Repositories.

**Your Setup:**
- `school-erp-maven-virtual` → combines local + remote repos

---

## Part 2: Maven Configuration Tags

Maven uses two main configuration files:

| File | Scope |
|------|-------|
| `settings.xml` | User-level config |
| `pom.xml` | Project-level config |

---

### 1. `<server>` — *The Authentication*

- **Lives in:** `settings.xml`
- **Purpose:** Stores credentials (username + password/token)
- **How it works:** Uses `<id>` to match repository credentials — acts like a secure keychain

```xml
<server>
    <id>school-erp-auth</id>
    <username>service-school-erp</username>
    <password>${env.ARTIFACTORY_TOKEN}</password>
</server>
```

---

### 2. `<repositories>` — *The Building Blocks*

- **Lives in:** `settings.xml` (inside a `<profile>`) or `pom.xml`
- **Purpose:** Defines where Maven downloads **project dependencies** from

> **Best Practice:** Use a Virtual Repository URL to unify private artifacts and public dependencies under one endpoint.

---

### 3. `<pluginRepositories>` — *The Build Tools*

- **Lives in:** `settings.xml` (inside a `<profile>`) or `pom.xml`
- **Purpose:** Defines where Maven downloads **plugins** from

| Tag | Downloads |
|-----|-----------|
| `<repositories>` | Dependencies |
| `<pluginRepositories>` | Build tools / plugins |

> 👉 Both usually point to the **same Virtual Repository**.

---

### 4. `<distributionManagement>` — *The Upload Destination*

- **Lives in:** `pom.xml` (preferably parent POM)
- **Purpose:** Defines where artifacts are uploaded when running `mvn deploy`

**Key Points:**
- Separate snapshot and release repositories
- Must point to **Local Repositories** (not virtual)
- `<id>` must match `<server>` in `settings.xml`

```xml
<distributionManagement>
    <snapshotRepository>
        <id>school-erp-auth</id>
        <url>https://trialhyf4hd.jfrog.io/artifactory/school-erp-maven-snapshot-local</url>
    </snapshotRepository>
    <repository>
        <id>school-erp-auth</id>
        <url>https://trialhyf4hd.jfrog.io/artifactory/school-erp-maven-release-local</url>
    </repository>
</distributionManagement>
```

---

### 5. `<activeProfiles>` — *The On-Switch*

- **Lives in:** `settings.xml`
- **Purpose:** Activates Maven profiles automatically

> ⚠️ **Best Practice — Multiple Environments**
>
> If you have multiple environments (e.g., Corporate / Personal):
>
> ❌ Do **NOT** activate both profiles at the same time  
> ✅ Activate via CLI instead:
>
> ```bash
> mvn clean deploy -Pschool-erp-profile
> ```
>
> **Why?** Avoids dependency conflicts, prevents credential leakage, and maintains environment isolation.

---

## Final Architecture Flow

```
Maven reads settings.xml
  └─► Activates profile
        └─► Downloads dependencies → Virtual Repo
              └─► Virtual Repo checks: Local Repo + Remote Repo
                    └─► Build completes
                          └─► mvn deploy → uploads to Local Repo
```

---

## Summary

| Component | Role |
|-----------|------|
| **Local Repo** | Store your artifacts |
| **Remote Repo** | Cache public dependencies |
| **Virtual Repo** | Single access point |
| `repositories` | Download dependencies |
| `pluginRepositories` | Download plugins |
| `distributionManagement` | Upload artifacts |
| `server` | Authentication |

## References:

https://www.youtube.com/watch?v=vyEgGsC_YQU