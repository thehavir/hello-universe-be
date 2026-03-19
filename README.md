# Hello Universe - BE

A Spring Boot application functioning as a proxy for the [NASA Astronomy Picture of the Day (APOD) API](https://api.nasa.gov/). It includes full archive caching with an enhanced and curated dataset, including generated missing thumbnails for videos and other improvements.

## Technical Overview

**Architecture**: Clean Architecture  
**Language**: Kotlin 2.2.21 (Java 21)  
**Framework**: Spring Boot 4.0.3  
**Database**: H2 (dev) / PostgreSQL (prod)  
**Migrations**: Flyway

## Project Structure

```
src/main/kotlin/dev/havir/hellouniverse/
├── application/               # Application use cases
│
├── configuration/             # Spring bean wiring
│
├── domain/                    # Domain models and interfaces
│
├── infrastructure/
│   ├── nasaapi/               # NASA API gateway & HTTP client
│   ├── persistence/           # JPA repositories & entities
│   └── scheduler/             # Scheduled jobs (Today APOD fetch)
│
├── presentation/              # REST controllers
│
test/src/                      # Unit and integration tests
```

## API Schema

### `GET /apods`

Returns a list of APODs for the given date range from the local cache.

**Query Parameters**

| Parameter    | Type         | Required | Description                    |
|--------------|--------------|----------|--------------------------------|
| `start_date` | `YYYY-MM-DD` | ✅        | Start of the date range        |
| `end_date`   | `YYYY-MM-DD` | ✅        | End of the date range          |

**Response** `200 OK`

```json
[
  {
    "date": "2024-01-15",
    "title": "string",
    "explanation": "string",
    "media_type": "IMAGE | VIDEO | OTHER",
    "url": "string | null",
    "hd_url": "string | null",
    "thumbnail_url": "string | null",
    "copyright": "string | null",
    "service_version": "string"
  }
]
```

## Getting Started

### Environment variables

| Variable        | Description                                      |
|-----------------|--------------------------------------------------|
| `NASA_API_KEY`  | Your NASA API key (get one at api.nasa.gov)      |
| `DB_PASSWORD`   | PostgreSQL password (prod profile only)          |

### Install local PostgreSQL

**macOS**:
```bash
brew install postgresql@18
brew services start postgresql@18
brew services list
brew install --cask dbeaver-community
```

**Linux**:
```bash
sudo apt install postgresql
su postgres
```

### Initialize user and database

```sql
# Prepare user
psql postgres
CREATE ROLE hellouniverse_db_user WITH LOGIN PASSWORD 'your_db_password';
ALTER ROLE hellouniverse_db_user WITH LOGIN;
ALTER ROLE hellouniverse_db_user CREATEDB;
\q

# Create DB
psql postgres -U hellouniverse_db_user -h 127.0.0.1
CREATE DATABASE hellouniverse_db;
GRANT ALL PRIVILEGES ON DATABASE hellouniverse_db TO hellouniverse_db_user;
\q

# Drop DB in order to create DB again
DROP DATABASE hellouniverse_db;
```

### Run migration scripts

```bash
# Verify
./mvnw flyway:info \
  -Dflyway.url=jdbc:postgresql://127.0.0.1:5432/hellouniverse_db \
  -Dflyway.user=hellouniverse_db_user \
  -Dflyway.password='your_db_password'

# Migrate
./mvnw flyway:migrate \
  -Dflyway.url=jdbc:postgresql://127.0.0.1:5432/hellouniverse_db \
  -Dflyway.user=hellouniverse_db_user \
  -Dflyway.password='your_db_password'
```

## Run

```bash
# dev profile uses H2 in-memory database — no external setup needed
NASA_API_KEY='your_nasa_api_key' ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# prod profile connects to local PostgreSQL
DB_PASSWORD='your_db_password' NASA_API_KEY='your_nasa_api_key' ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Test

```bash
./mvnw test
```

## Acknowledgment

Special thanks to [@akaiser](https://github.com/akaiser) for his help and guidance to build this project!
