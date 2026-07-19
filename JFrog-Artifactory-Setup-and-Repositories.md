# JFrog Artifactory Setup & Maven Configuration Guide

## Overview
This document outlines the architecture and configuration required to connect the **Enterprise Resource Management (ERM)** Spring Boot project to our JFrog Artifactory Cloud instance. It covers repository setup, global Maven authentication, and project-level distribution management.

---

## 1. JFrog Repository Architecture
Following JFrog's best practices and standardized naming conventions (`<projectKey>-<technology>-<maturity>-<locator>`), we utilize a streamlined 4-repository architecture.

### Local Repositories (Physical Storage)
Local repositories are the physical storage buckets where our own proprietary, compiled `.jar` files are uploaded and saved.
*   **`erm-maven-snapshot-local`**: Stores unstable, day-to-day work-in-progress builds (e.g., `1.0.0-SNAPSHOT`).
*   **`erm-maven-release-local`**: Stores finalized, polished, and production-ready builds (e.g., `1.0.0`).

### Remote Repository (Proxy & Caching)
Remote repositories do not host our code. Instead, they act as a smart proxy to external public registries.
*   **`erm-maven-release-remote`**: Points directly to public Maven Central. When our project needs external libraries (like the Spring Framework), it pulls them through this proxy so JFrog can cache them for faster, secure future access.

### Virtual Repository (Single Entry Point)
Virtual repositories do not physically store files. Instead, they aggregate multiple repositories (local and remote) into a single URL.
*   **`erm-maven`**: Bundles `erm-maven-snapshot-local`, `erm-maven-release-local`, and `erm-maven-release-remote` together. Developers only need to point their configuration files to this one URL to download everything they need.

---

## 2. Global Maven Setup (`settings.xml`)
The `~/.m2/settings.xml` file is configured locally on the developer's machine. It serves two purposes:
1.  **Authentication:** Securely storing the JFrog encrypted identity token.
2.  **Resolution:** Forcing Maven to download all external dependencies and plugins through the `erm-maven` virtual repository rather than the public internet.

**File Location:** `~/.m2/settings.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xsi:schemaLocation="[http://maven.apache.org/SETTINGS/1.2.0](http://maven.apache.org/SETTINGS/1.2.0) [https://maven.apache.org/xsd/settings-1.2.0.xsd](https://maven.apache.org/xsd/settings-1.2.0.xsd)" xmlns="[http://maven.apache.org/SETTINGS/1.2.0](http://maven.apache.org/SETTINGS/1.2.0)"
          xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)">

    <!-- SERVERS: Contains authentication credentials for your JFrog registries -->
    <servers>
        <server>
            <id>central</id>
            <username>harsh.choudhary@salesforce.com</username>
            <password>cmVmdGtuOjAxOjE4MTYwMzM4MTU6QWlyTUdHbWR3ODA0a250VHo2V0dRMnBYcDRi</password>
        </server>
        <server>
            <id>snapshots</id>
            <username>harsh.choudhary@salesforce.com</username>
            <password>cmVmdGtuOjAxOjE4MTYwMzM4MTU6QWlyTUdHbWR3ODA0a250VHo2V0dRMnBYcDRi</password>
        </server>
    </servers>

    <!-- PROFILES: Defines custom repository targets and environments -->
    <profiles>
        <profile>
            <id>artifactory</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <name>erm-maven</name>
                    <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven](https://trialzv4zpr.jfrog.io/artifactory/erm-maven)</url>
                    <snapshots><enabled>false</enabled></snapshots>
                </repository>
                <repository>
                    <id>snapshots</id>
                    <name>erm-maven</name>
                    <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven](https://trialzv4zpr.jfrog.io/artifactory/erm-maven)</url>
                    <snapshots />
                </repository>
            </repositories>

            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <name>erm-maven</name>
                    <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven](https://trialzv4zpr.jfrog.io/artifactory/erm-maven)</url>
                    <snapshots><enabled>false</enabled></snapshots>
                </pluginRepository>
                <pluginRepository>
                    <id>snapshots</id>
                    <name>erm-maven</name>
                    <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven](https://trialzv4zpr.jfrog.io/artifactory/erm-maven)</url>
                    <snapshots />
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>

    <!-- ACTIVE PROFILES: Automatically applies the profile to all builds -->
    <activeProfiles>
        <activeProfile>artifactory</activeProfile>
    </activeProfiles>
</settings>
```

---

## 3. Project Configuration (`pom.xml`)
While the `settings.xml` file handles *downloading* from the virtual repository, the project's `pom.xml` must be configured to *upload* directly to the physical **local repositories**.

This is handled by the `<distributionManagement>` block, which links directly to the `<id>` tags in the `settings.xml` to authenticate the upload.

**File Location:** Project Root Directory (`pom.xml`)

```xml
<distributionManagement>
    <!-- Target for official, finalized release builds -->
    <repository>
        <id>central</id>
        <name>JFrog Artifactory Release Repository</name>
        <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven-release-local](https://trialzv4zpr.jfrog.io/artifactory/erm-maven-release-local)</url>
    </repository>

    <!-- Target for unstable, work-in-progress snapshot builds -->
    <snapshotRepository>
        <id>snapshots</id>
        <name>JFrog Artifactory Snapshot Repository</name>
        <url>[https://trialzv4zpr.jfrog.io/artifactory/erm-maven-snapshot-local](https://trialzv4zpr.jfrog.io/artifactory/erm-maven-snapshot-local)</url>
    </snapshotRepository>
</distributionManagement>
```

---

## 4. Deployment Execution
Once the configuration is complete, the project can be pushed to JFrog Artifactory via the terminal.

```bash
mvn clean deploy
```

**How it works:**
1. Maven compiles the Java code and builds the artifact (`.jar`).
2. It reads the version tag in the `pom.xml`. If the version ends in `-SNAPSHOT`, it routes the file to the `<snapshotRepository>`. Otherwise, it goes to `<repository>`.
3. It grabs the matching `id` (`central` or `snapshots`) and pulls the corresponding username/password from `~/.m2/settings.xml`.
4. The `.jar` is successfully published to JFrog.

---

## 5. Adding External Repositories (GitHub, Red Hat, etc.)
By design, a Remote Repository in JFrog acts as a **1-to-1 connection** to an external server (due to varying authentication methods and caching rules). You cannot add multiple URLs to a single remote repository.

To download dependencies from an external source other than Maven Central (e.g., GitHub Packages), follow these steps:

1. **Create a New Remote Repository:**
   Navigate to the JFrog Dashboard -> **Administration** -> **Repositories**. Create a new Maven Remote Repository (e.g., `github-maven-remote`) and input the specific external URL and any required authentication credentials (like a Personal Access Token).
2. **Attach it to the Virtual Repository:**
   Edit the `erm-maven` virtual repository and move the newly created `github-maven-remote` into the list of "Selected Repositories."

**No code changes are required.** The `pom.xml` and `settings.xml` files will continue pointing to the single virtual `erm-maven` URL.

### Resolution Order
When Maven requests a dependency, JFrog searches for it using a top-to-bottom sequence:
1. **Local Repositories:** Checks for internal proprietary code first.
2. **Primary Remote Repository:** Checks Maven Central.
3. **Secondary Remote Repositories:** Checks added sources (like GitHub or Red Hat) sequentially until the artifact is found, cached, and downloaded.