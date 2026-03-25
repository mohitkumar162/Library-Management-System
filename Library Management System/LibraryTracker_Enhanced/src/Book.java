import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private String name;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String name, String author, String genre, int totalCopies) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public int getId() { return id; }
    public String getBookName() { return name; }
    public String getAuthorName() { return author; }
    public String getGenre() { return genre; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setName(String name) { this.name = name; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }

    public void setTotalCopies(int total) {
        int diff = total - this.totalCopies;
        this.totalCopies = total;
        this.availableCopies = Math.max(0, this.availableCopies + diff);
    }

    public boolean isAvailable() { return availableCopies > 0; }

    public boolean borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public boolean returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return id + ". " + name + " by " + author
                + " | Copies: " + availableCopies + "/" + totalCopies;
    }
}
