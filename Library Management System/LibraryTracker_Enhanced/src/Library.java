import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Library {

    // ── persistence files ──────────────────────────────────────────────────
    private static final String BOOKS_FILE   = "books.dat";
    private static final String USERS_FILE   = "users.dat";
    private static final String HISTORY_FILE = "history.dat";

    // ── in-memory state ────────────────────────────────────────────────────
    private HashMap<Integer, Book>         books;
    private HashMap<String, User>          users;     // username → User
    private ArrayList<BorrowRecord>        history;

    private int bookIdCounter   = 1;
    private int recordIdCounter = 1;

    // ── constructor ────────────────────────────────────────────────────────
    public Library() {
        loadBooks();
        loadUsers();
        loadHistory();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  USER management
    // ══════════════════════════════════════════════════════════════════════

    /** Returns the authenticated User or null if credentials wrong. */
    public User login(String username, String password) {
        User u = users.get(username.trim().toLowerCase());
        if (u == null) return null;
        return u.getPasswordHash().equals(User.hash(password)) ? u : null;
    }

    /** Register a new student. Returns false if username taken. */
    public boolean registerStudent(String username, String password) {
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) return false;
        users.put(key, new User(key, User.hash(password), User.Role.STUDENT));
        saveUsers();
        return true;
    }
    public void deleteUser(String username) {
        users.remove(username.toLowerCase());
        saveUsers();
    }

    public Collection<User> getAllUsers() { return users.values(); }

    // ══════════════════════════════════════════════════════════════════════
    //  BOOK management
    // ══════════════════════════════════════════════════════════════════════

    public boolean addBook(String name, String author, String genre, int copies) {
        // bookIdCounter is always the next free slot (maintained by load + changeBookId + delete)
        books.put(bookIdCounter, new Book(bookIdCounter, name, author, genre, copies));
        bookIdCounter++;
        // Advance past any already-occupied IDs (shouldn't happen normally, but be safe)
        while (books.containsKey(bookIdCounter)) bookIdCounter++;
        saveBooks();
        return true;
    }

    public boolean deleteBook(int id) {
        if (!books.containsKey(id)) return false;
        books.remove(id);
        saveBooks();
        return true;
    }

    public boolean editBook(int id, String name, String author, String genre, int copies) {
        Book b = books.get(id);
        if (b == null) return false;
        b.setName(name);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setTotalCopies(copies);
        saveBooks();
        return true;
    }

    /**
     * Change a book's ID. Updates the books map key, the Book object itself,
     * and all BorrowRecord references so nothing is left pointing to the old ID.
     * Returns false if oldId doesn't exist or newId is already taken.
     */
    public boolean changeBookId(int oldId, int newId) {
        if (!books.containsKey(oldId)) return false;
        if (oldId == newId) return true;
        if (books.containsKey(newId)) return false; // new ID already in use

        Book b = books.remove(oldId);
        b.setId(newId);
        books.put(newId, b);

        // Update every borrow-history record that referenced the old ID
        for (BorrowRecord r : history) {
            if (r.getBookId() == oldId) r.setBookId(newId);
        }

        // Recalculate the next free counter
        bookIdCounter = 1;
        while (books.containsKey(bookIdCounter)) bookIdCounter++;

        saveBooks();
        saveHistory();
        return true;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public Book getBook(int id) { return books.get(id); }

    // ══════════════════════════════════════════════════════════════════════
    //  BORROW / RETURN
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Borrow a book for a student with a specific due date.
     * Returns:  0 = success
     *          -1 = book not found
     *          -2 = no copies available
     *          -3 = student already has an active borrow of this book
     */
    public int borrowBook(String studentUsername, int bookId, LocalDate dueDate) {
        Book b = books.get(bookId);
        if (b == null) return -1;

        // check student doesn't already have an active borrow for this book
        boolean alreadyBorrowed = history.stream()
                .anyMatch(r -> r.getStudentUsername().equals(studentUsername)
                            && r.getBookId() == bookId
                            && !r.isReturned());
        if (alreadyBorrowed) return -3;

        if (!b.borrowCopy()) return -2;

        history.add(new BorrowRecord(
                recordIdCounter++, studentUsername,
                bookId, b.getBookName(),
                LocalDate.now(), dueDate));
        saveBooks();
        saveHistory();
        return 0;
    }

    /**
     * Return a book.
     * Returns true on success, false if no active borrow found.
     */
    public boolean returnBook(String studentUsername, int bookId) {
        Optional<BorrowRecord> rec = history.stream()
                .filter(r -> r.getStudentUsername().equals(studentUsername)
                          && r.getBookId() == bookId
                          && !r.isReturned())
                .findFirst();

        if (rec.isEmpty()) return false;

        rec.get().markReturned();
        books.get(bookId).returnCopy();
        saveBooks();
        saveHistory();
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HISTORY queries
    // ══════════════════════════════════════════════════════════════════════

    public List<BorrowRecord> getAllHistory() {
        return new ArrayList<>(history);
    }

    public List<BorrowRecord> getHistoryForStudent(String username) {
        return history.stream()
                .filter(r -> r.getStudentUsername().equals(username))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getActiveborrows() {
        return history.stream()
                .filter(r -> !r.isReturned())
                .collect(Collectors.toList());
    }

    public void deleteRecord(int recordId) {
        history.removeIf(r -> r.getRecordId() == recordId);
        saveHistory();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadBooks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(BOOKS_FILE))) {
            books = (HashMap<Integer, Book>) in.readObject();
            in.readInt(); // discard saved counter — always recalculate below
        } catch (Exception e) {
            books = new HashMap<>();
        }
        // Always recalculate: find smallest unused ID so gaps left by deletes are filled
        bookIdCounter = 1;
        while (books.containsKey(bookIdCounter)) bookIdCounter++;
    }

    private void saveBooks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE))) {
            out.writeObject(books);
            out.writeInt(bookIdCounter);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (HashMap<String, User>) in.readObject();
        } catch (Exception e) {
            users = new HashMap<>();
            // seed default admin
            users.put("admin", new User("admin", User.hash("admin123"), User.Role.ADMIN));
        }
        saveUsers(); // ensure file exists
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            out.writeObject(users);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private void loadHistory() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(HISTORY_FILE))) {
            history         = (ArrayList<BorrowRecord>) in.readObject();
            recordIdCounter = in.readInt();
        } catch (Exception e) {
            history = new ArrayList<>();
            recordIdCounter = 1;
        }
    }

    private void saveHistory() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            out.writeObject(history);
            out.writeInt(recordIdCounter);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
