# 🖋️ Blog Application Backend

A robust, containerized RESTful API for a community blogging platform. Built with **Spring Boot**, secured with **JWT**, documented using **Swagger** and fully dockerized. 

The application features advanced data handling like **Reddit**, ensuring discussions remain even when users or posts are deleted.

**[Live Demo on Render](https://blog-application-backend-nllp.onrender.com/blog-application/index.html)**

## ✨ Key Features

### 🔄 Logic & Data Handling
* **Persistent Threading (Reddit-Style):** To keep conversations readable even after a user leaves, I implemented "Graceful Deletion." Instead of a hard DELETE, the database updates the content to [removed] while keeping the comment tree intact.
* **Smart Engagement:** I simplified the feature by automatically liking a post upon creation—because if you wrote it, you probably like it. I also implemented a Like Toggle to ensure users can only like a piece of content once.
* **Toggle Likes:** Users can toggle likes on posts and comments (ensuring one like per user).

### 🔍 Search & Discovery
* **Fuzzy Search:** Users can find posts using partial keywords (e.g., searching "Java" finds "Spring Boot a powerful Java Framework").
* **Pagination:** API endpoints support `pageNo` and `pageSize` which divides the data into pages so thats when a user fetches a post its not overloaded. All fetch requests are paginated by default to prevent payload bloat. Users can control this via ?pageNo=0&pageSize=10 parameters.
* **User Profiles:** Public endpoints to fetch all posts by a specific user. But a user's liked post and comments along with his created comments are private.

### 🛡️ Security & Access
* **JWT Authentication:** Stateless security protecting write operations (Create/Update/Delete).
* **Public Read Access:** Unauthenticated users can view authenticate(register), look at people's post, and thier profiles.

## 🛠 Tech Stack

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

---

## 🚀 Quick Start

Run the entire application (App + Database) instantly.

**1. Clone the repository**
```bash
git clone https://github.com/Yearis/Blog-Application-Backend.git
cd Blog-Application-Backend
```

**2. Configure Environment**
Create a .env file in the root directory (refer to .env.example) and add your database and JWT secrets.

**3. Build & Run**
```bash
docker-compose up --build
```
The application will start on port 8080.

### 📚 API Documentation
Once the application is running, you can explore and test all endpoints using the interactive Swagger UI:

[View API Docs](https://blog-application-backend-nllp.onrender.com/blog-application/swagger-ui/index.html)

Created by 👨‍💻 [Yearis](https://github.com/Yearis)
