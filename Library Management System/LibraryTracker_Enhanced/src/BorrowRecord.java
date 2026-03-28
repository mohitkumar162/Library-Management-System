import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final int recordId;
    private final String studentUsername;
    private int bookId;
    private final String bookName;
    private final LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null if not yet returned

    public BorrowRecord(int recordId, String studentUsername,
                        int bookId, String bookName,
                        LocalDate borrowDate, LocalDate dueDate) {
        this.recordId        = recordId;
        this.studentUsername = studentUsername;
        this.bookId          = bookId;
        this.bookName        = bookName;
        this.borrowDate      = borrowDate;
        this.dueDate         = dueDate;
        this.returnDate      = null;
    }

    public int getRecordId()          { return recordId; }
    public String getStudentUsername(){ return studentUsername; }
    public int getBookId()            { return bookId; }
    public void setBookId(int bookId)  { this.bookId = bookId; }
    public String getBookName()       { return bookName; }
    public LocalDate getBorrowDate()  { return borrowDate; }
    public LocalDate getDueDate()     { return dueDate; }
    public LocalDate getReturnDate()  { return returnDate; }
    public boolean isReturned()       { return returnDate != null; }

    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    public void markReturned() {
        this.returnDate = LocalDate.now();
    }

    public String getStatus() {
        if (isReturned()) return "Returned (" + returnDate.format(FMT) + ")";
        if (isOverdue())  return "OVERDUE";
        return "Borrowed";
    }
}
