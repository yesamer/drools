# KIE-CI Module

## What is KIE-CI?

Drools KIE-CI (Knowledge Is Everything - Continuous Integration) is a module within the Apache KIE ecosystem designed to bridge the gap between business rule authoring and application deployment. It enables Java applications to automatically load, scan, and deploy business rules (KJARs) directly from Maven repositories.

## Why Use It?

Traditional approach: Rules bundled with application → Rule change requires full redeployment

KIE-CI approach: Rules in separate Maven artifacts → Deploy new rules → Application auto-updates → Zero downtime

## Core Features

### 1. Load KieContainer from Maven

```java
KieServices ks = KieServices.Factory.get();
ReleaseId releaseId = ks.newReleaseId("org.example", "my-rules", "1.0.0");
KieContainer kContainer = ks.newKieContainer(releaseId);
```

### 2. Auto-Update with KieScanner

```java
// Use LATEST or RELEASE for dynamic versions
ReleaseId releaseId = ks.newReleaseId("org.example", "my-rules", "LATEST");
KieContainer kContainer = ks.newKieContainer(releaseId);

KieScanner scanner = ks.newKieScanner(kContainer);
scanner.start(10000L); // Poll every 10 seconds
// Container automatically updates when new versions are deployed
```

### 3. Inspect Module Metadata

```java
KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(releaseId);
Collection<String> packages = metaData.getPackages();
```

## Quick Start

1. **Add dependency:**
```xml
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-ci</artifactId>
</dependency>
```

2. **Build and install your KJAR:**
```bash
mvn clean install
```

3. **Load it in your application:**
```java
KieContainer kContainer = KieServices.Factory.get()
    .newKieContainer(ks.newReleaseId("org.example", "my-rules", "1.0.0"));
```

## Example

See working example: [`drools-examples-api/kiecontainer-from-kierepo`](../drools-examples-api/kiecontainer-from-kierepo/)

## Resources

- [Apache KIE Documentation](https://kie.apache.org/docs/documentation/)
- [KIE-CI Tutorial by Mauricio Salatino](https://www.salaboy.com/2015/10/23/back-to-basics-3-using-drools-kie-ci-continuous-integration/)