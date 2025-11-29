# **Book Store Service**

A Spring Boot web application for managing an online book store. This system supports role-based access control (RBAC) for **Clients** and **Employees**, allowing users to browse books, manage carts, and place orders, while employees can manage inventory, users, and order fulfillment.

## **🚀 Features**

### **Public Access**

* **Browse Books:** View a paginated, sortable, and searchable list of books.
* **Book Details:** View detailed information about specific books.
* **Registration:** Create a new Client account.
* **Localization:** Switch between English and Ukrainian languages.

### **Client Role**

* **Shopping Cart:** Add/remove items, view total cost (stored in Cookies for statelessness).
* **Order Placement:** Submit orders for processing.
* **Order History:** View, search, and sort personal order history.
* **Profile Management:** Update name and change password.
* **Account Deletion:** Delete own account (cascades to delete order history).

### **Employee Role**

* **Book Management:** Add, Edit, and Delete books.
* **Client Management:**
  * View, **search**, and **sort** the list of registered clients.
  * **Top up** client balances (add funds).
  * **Block/Unblock** users to restrict access (immediately terminates active sessions).
* **Order Management:**
  * View, **search**, and **sort** all orders in the system.
  * **Confirm** orders (deducts balance from client).
  * **Cancel** pending orders.
* **Profile Management:** Update name, phone, **birthdate**, and change password.

## **🛠️ Technology Stack**

* **Java 17**
* **Spring Boot 3.x** (Web, Data JPA, Security, Validation)
* **Thymeleaf** (Server-side templating)
* **MySQL** (Production database) & **H2** (Test/Dev database)
* **Lombok** (Boilerplate reduction)
* **ModelMapper** (DTO mapping)
* **Tailwind CSS** (Styling)

## **⚙️ Configuration & Profiles**

The application is configured with two profiles:

### **1\. Default (Dev/Test)**

* **Database:** H2 (In-Memory).
* **Data Init:** Automatically loads sample data from src/main/resources/sql/data-h2.sql.
* **Run:** Just start the application normally.

### **2\. Production (prod)**

* **Database:** MySQL.
* **Data Init:** Uses src/main/resources/sql/data-mysql.sql.
* **Setup:** Requires a MySQL server running on port 3306 with a database named book\_store\_db.
* **Run:**
> java \-jar target/book-store-service.jar \--spring.profiles.active=prod

## **🔑 Default Credentials**

The application comes pre-loaded with sample data.
**Note:** Each user has a specific password defined in the SQL initialization scripts.
**Common Examples:**

| Role | Email | Password |
| :---- | :---- | :---- |
| **Employee** | john.doe@email.com | pass123 |
| **Client** | client1@example.com | password123 |

For the full list of users and their specific passwords (e.g. abc456, qwerty789), please refer to the SQL files:

* src/main/resources/sql/data-h2.sql
* src/main/resources/sql/data-mysql.sql

## **🏗️ Project Structure**

* **config/**: Security configurations (SecurityConfig, JwtAuthenticationFilter, WebConfig for i18n).
* **controller/**: MVC controllers handling web requests.
* **service/**: Business logic.
* **dto/**: Data Transfer Objects for API/View communication.
* **model/**: JPA Entities.
* **repo/**: Spring Data JPA repositories.

## **🧪 Testing**

The project includes a comprehensive test suite:

* **Unit Tests (@WebMvcTest):** Verifies controller logic in isolation.
* **Security Integration Tests (@SpringBootTest):** Verifies security rules (access control, redirects, CSRF).
* **Service Tests:** Verifies business logic using Mockito.

To run tests:
> mvn test
