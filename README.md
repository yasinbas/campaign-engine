Flexible Campaign Engine
A robust and flexible campaign engine built with Spring Boot. This project provides a standalone service to create, manage, and evaluate complex retail campaigns like 'Buy X, Get Y', percentage-based discounts, and more. It is designed to be easily integrated with any external POS, ERP, or CRM system through a clean REST API.

This project is being developed as a step-by-step, collaborative coding exercise.

✨ Key Features
REST API for listing active campaigns and evaluating shopping carts.

Dynamic rule evaluation engine for calculating discounts.

Database-driven campaign management with PostgreSQL.

Simple web-based Admin Panel for listing and creating new campaigns.

Auto-generated, interactive API documentation via SpringDoc (Swagger UI).

Support for various campaign types, including:

Basket Total Percentage Discount

Buy X, Pay Y (Logic to be implemented)

🛠️ Tech Stack
Backend: Spring Boot 3

Language: Java 17

Database: PostgreSQL

Data Access: Spring Data JPA / Hibernate

API Documentation: SpringDoc OpenAPI 3.0 (Swagger UI)

View Layer (Admin): Thymeleaf

Build Tool: Apache Maven

🚀 Getting Started
Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

Prerequisites
You need to have the following software installed on your system:

Java JDK 17 or later.

Apache Maven.

Git.

Docker (Recommended for running the database).

Installation & Running
Clone the repository:

Bash

git clone https://github.com/yasinbas/campaign-engine.git
cd campaign-engine


Start the PostgreSQL Database:
The easiest way to get a database instance running is by using Docker. Run the following command in your terminal:

Bash

docker run --name campaign-db -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=campaign_engine_db -p 5432:5432 -d postgres
Configure the Application:
Open the src/main/resources/application.properties file. Ensure the database credentials match your setup. The default configuration is set up to work with the Docker command above.

Run the Application:
You can run the application directly from your IDE (e.g., IntelliJ IDEA) by running the CampaignEngineApplication class, or you can use the Maven wrapper:

Bash

./mvnw spring-boot:run
The application will start on http://localhost:8080.

kullanım
Once the application is running, you can start interacting with its features.

📝 API Endpoints
The best way to explore and test the API is through the interactive Swagger UI.

Swagger UI URL: http://localhost:8080/swagger-ui.html

Key Endpoints:

GET /api/v1/campaigns/active

Returns a list of all currently active campaigns in the database.

POST /api/v1/campaigns/evaluate

Evaluates a given shopping cart against all active campaigns and returns the original total, final total, and a list of applied discounts.

Sample Request Body:

JSON

{
"items": [
{
"productId": "SKU-001",
"quantity": 2,
"unitPrice": 200.00
},
{
"productId": "SKU-002",
"quantity": 1,
"unitPrice": 150.50
}
]
}
🖥️ Admin Panel
A simple web-based admin panel is available for managing campaigns.

Admin Panel URL: http://localhost:8080/admin/campaigns

From here, you can view all campaigns in the system and add new ones using a web form.

🗺️ Roadmap
This project is under active development. Future enhancements include:

[ ] Implementing more campaign types in the evaluation engine (e.g., BUY_X_PAY_Y).

[ ] Adding "Edit" and "Delete" functionality to the Admin Panel.

[ ] Writing comprehensive unit and integration tests with JUnit, Mockito, and Testcontainers.

[ ] Securing the API and Admin Panel with Spring Security.

[ ] Implementing a Webhook system for notifying external systems of campaign changes.

📄 License
This project is licensed under the MIT License.
