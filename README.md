# ShedJukeBox2

Self-hosted party jukebox: MP3s on a CIFS/SMB share, indexed into PostgreSQL, served and played out entirely by Tomcat. Guest phone UI + browser playout page from one WAR. Spotify dependency removed.

**Version:** 0.12.1

## Stack

- Maven WAR (Jakarta Servlet 6.0 / Tomcat 10–11)
- PostgreSQL 16 (driver in Tomcat `lib/`, `provided` scope)
- Vanilla JS, PBKDF2 admin auth
- Config via environment variables (`setenv.sh`)

## Quick start

```bash
# Build
mvn -DskipTests package

# WAR is target/shedjukebox2.war — deploy to Tomcat webapps/

# Required env (e.g. in $CATALINA_HOME/bin/setenv.sh):
export JUKE_DB_URL=jdbc:postgresql://localhost:5432/jukebox
export JUKE_DB_USER=jukebox
export JUKE_DB_PASS=secret
export JUKE_MUSIC_ROOT=/mnt/music
export JUKE_ART_CACHE_DIR=/var/cache/shedjukebox/art
export JUKE_BASE_URL=https://jukebox.example.com
```

Copy the PostgreSQL JDBC driver into `$CATALINA_HOME/lib/`.

Default admin: **admin** / **admin** (rehashed on first login — change immediately).

## Music library

Primary path: host CIFS mount at `JUKE_MUSIC_ROOT`. Tomcat reads files via `java.nio.file`.

## Licence

Private / internal use unless otherwise stated.
