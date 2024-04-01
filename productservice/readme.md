# ProductService API Endpoints

This document provides a brief overview of the API endpoints available in the ProductService project.

## Endpoints

### GET /

This endpoint retrieves all products.

**Example:**

```bash
curl -X GET 'http://localhost:8080/'
```

Response:

A list of all products in JSON format.

### GET /{id}
This endpoint retrieves a single product by its ID.

**Example:**
```bash
curl -X GET 'http://localhost:8080/{id}'
```
Replace {id} with the ID of the product you want to retrieve.

Response:

The product with the specified ID in JSON format.

# CategoryController API Endpoints
This document provides a brief overview of the API endpoints available in the CategoryController of the ProductService project.

## Endpoints
### GET /categories
This endpoint retrieves all categories.

Example:
```bash
curl -X GET 'http://localhost:8080/categories'
```

Response:

A list of all categories in JSON format.

### GET /categories/{id}
This endpoint retrieves a single category by its ID.

Example:
```bash
curl -X GET 'http://localhost:8080/categories/{id}'
```

Replace {id} with the ID of the category you want to retrieve.

Response:

The category with the specified ID in JSON format.

### POST /categories
This endpoint creates a new category.

Example:
```bash
curl -X POST -H "Content-Type: application/json" -d '{"name":"New Category"}' 'http://localhost:8080/categories'
```

Response:

The newly created category in JSON format.