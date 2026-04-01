# 📚 Library Management System

## New Features Added

### 🔐 Login System
- Both Admin and Students must log in with username + password
- Passwords are stored as SHA-256 hashes (never plain text)
- Default Admin credentials: **username:** `admin`  **password:** `admin123`
- Students can self-register from the login screen

### 👥 Multiple Students, One Admin
- One fixed admin account (can change password in code or add a settings dialog)
- Multiple students can register and each has their own borrow history

### 📅 Due Dates on Borrowing
- Students must enter a due date (format: `dd-MM-yyyy`) when borrowing
- Overdue books are flagged and visible in the Admin "Overdue" tab

### 📦 Book Copies / Quantity
- Each book has a configurable number of copies
- Multiple students can borrow the same title simultaneously
- Available/Total counts shown in the book catalogue

### 📜 Borrow History
- Full history log: who borrowed what, when, due date, return date
- Admin sees ALL history across all students
- Students see only their own history
- Overdue rows highlighted in red, returned rows in green

### ✏️ Edit Book Details (Admin)
- Select any book in the catalogue and click "Edit Selected"
- Update title, author, genre, and number of copies

### 🗑 Delete Book (Admin)
- Select any book and click "Delete Selected"
- Confirmation dialog before permanent deletion

---

## Project Structure
```
src/
├── Book.java           — Book entity (title, author, genre, copies)
├── BorrowRecord.java   — Borrow/return log entry with due dates
├── User.java           — User entity with role (ADMIN / STUDENT)
├── Library.java        — Data layer (all business logic + file persistence)
└── LibraryApp.java     — Swing UI (login, admin panel, student panel)
```

## Data Files (auto-created on first run)
- `books.dat`   — serialized book catalogue
- `users.dat`   — serialized user accounts
- `history.dat` — serialized borrow history

## How to Compile & Run
```bash
cd src
javac *.java
java LibraryApp
```

Requires Java 11 or higher.
