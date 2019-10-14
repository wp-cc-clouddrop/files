# README

## Buiding with Docker

0. Make sure docker is installed
1. Repackage spring-boot app for standalone: `mvn package spring-boot:repackage`
2. Build: `docker build -t clouddrop-files --rm=true .`
3. Run: `docker run -p 8080:8080 clouddrop-files`