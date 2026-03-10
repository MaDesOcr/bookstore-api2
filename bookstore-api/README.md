# bookstore-api — Projet support Tests d'Intégration

## Prérequis
- Java 17+
- Maven 3.8+
- Docker Desktop (démarré)

## Démarrage rapide

```bash
# Tests unitaires uniquement
mvn test

# Tests d'intégration (Docker requis)
mvn verify

# Lancer l'application (PostgreSQL via Docker Compose)
docker compose up -d
mvn spring-boot:run
```

## Structure des tests

| Classe | Type | Annotation | Scope |
|---|---|---|---|
| `BookRepositoryIT` | @DataJpaTest | @Testcontainers | Couche JPA uniquement |
| `BookServiceIT` | @SpringBootTest | @Testcontainers | Service + JPA |
| `BookApiIT` | @SpringBootTest | @Testcontainers | HTTP complet |

## API REST

| Méthode | Endpoint | Description |
|---|---|---|
| GET | /api/books | Liste tous les livres |
| GET | /api/books/{id} | Détail d'un livre |
| GET | /api/books/category/{cat} | Par catégorie |
| GET | /api/books/search?q= | Recherche textuelle |
| POST | /api/books | Créer un livre |
| PUT | /api/books/{id} | Modifier un livre |
| DELETE | /api/books/{id} | Supprimer un livre |
| POST | /api/books/{id}/purchase?quantity= | Acheter (décrémente stock) |
