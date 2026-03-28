import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LibraryApp extends JFrame {

    // ── palette ────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(15, 20, 35);
    private static final Color PANEL_BG  = new Color(24, 32, 52);
    private static final Color CARD_BG   = new Color(32, 44, 70);
    private static final Color ACCENT    = new Color(99, 179, 237);
    private static final Color ACCENT2   = new Color(72, 199, 142);
    private static final Color DANGER    = new Color(245, 101, 101);
    private static final Color WARNING   = new Color(237, 187, 85);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color SUBTEXT   = new Color(148, 163, 184);
    private static final Font  MONO      = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font  HEADER    = new Font("SansSerif", Font.BOLD, 22);
    private static final Font  LABEL     = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  BODY      = new Font("SansSerif", Font.PLAIN, 13);

    private final Library library;
    private User currentUser;

    // Shared book table models so all tabs stay in sync
    private DefaultTableModel addBookModel;
    private DefaultTableModel allBooksModel;

    // ──────────────────────────────────────────────────────────────────────
    public LibraryApp() {
        library = new Library();
        applyGlobalTheme();
        showLoginScreen();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GLOBAL THEME
    // ══════════════════════════════════════════════════════════════════════
    private void applyGlobalTheme() {
        UIManager.put("Panel.background",           PANEL_BG);
        UIManager.put("OptionPane.background",       PANEL_BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background",           CARD_BG);
        UIManager.put("Button.foreground",           TEXT);
        UIManager.put("Button.font",                 LABEL);
        UIManager.put("TextField.background",        CARD_BG);
        UIManager.put("TextField.foreground",        TEXT);
        UIManager.put("TextField.caretForeground",   ACCENT);
        UIManager.put("TextField.font",              BODY);
        UIManager.put("PasswordField.background",    CARD_BG);
        UIManager.put("PasswordField.foreground",    TEXT);
        UIManager.put("PasswordField.caretForeground", ACCENT);
        UIManager.put("Table.background",            CARD_BG);
        UIManager.put("Table.foreground",            TEXT);
        UIManager.put("Table.gridColor",             new Color(45, 60, 90));
        UIManager.put("Table.selectionBackground",   ACCENT.darker());
        UIManager.put("Table.selectionForeground",   Color.WHITE);
        UIManager.put("Table.font",                  BODY);
        UIManager.put("TableHeader.background",      BG);
        UIManager.put("TableHeader.foreground",      ACCENT);
        UIManager.put("TableHeader.font",            LABEL);
        UIManager.put("ScrollPane.background",       CARD_BG);
        UIManager.put("ScrollBar.background",        PANEL_BG);
        UIManager.put("TabbedPane.background",       PANEL_BG);
        UIManager.put("TabbedPane.foreground",       TEXT);
        UIManager.put("TabbedPane.selected",         CARD_BG);
        UIManager.put("TabbedPane.font",             LABEL);
        UIManager.put("Label.foreground",            TEXT);
        UIManager.put("Label.font",                  BODY);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ══════════════════════════════════════════════════════════════════════
    private void showLoginScreen() {
        JFrame lf = new JFrame("📚 Library Tracker — Login");
        lf.setSize(440, 420);
        lf.setLocationRelativeTo(null);
        lf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lf.getContentPane().setBackground(BG);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.fill      = GridBagConstraints.HORIZONTAL;
        g.insets    = new Insets(6, 0, 6, 0);
        g.gridwidth = 2;
        g.weightx   = 1;

        // Title
        JLabel title = new JLabel("📚 Library Tracker", SwingConstants.CENTER);
        title.setFont(HEADER);
        title.setForeground(ACCENT);
        g.gridy = 0;
        root.add(title, g);

        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setFont(BODY);
        sub.setForeground(SUBTEXT);
        g.gridy = 1;
        root.add(sub, g);

        root.add(Box.createVerticalStrut(10), constraint(g, 2));

        // Username
        root.add(styledLabel("Username"), constraint(g, 3));
        JTextField userField = styledField();
        g.gridy = 4;
        root.add(userField, g);

        // Password
        root.add(styledLabel("Password"), constraint(g, 5));
        JPasswordField passField = new JPasswordField();
        styleTextField(passField);
        g.gridy = 6;
        root.add(passField, g);

        // Login button
        JButton loginBtn = accentButton("Login", ACCENT);
        g.gridy = 7;
        g.insets = new Insets(14, 0, 6, 4);
        root.add(loginBtn, g);

        // Register link
        JLabel regLabel = new JLabel("<html><u>New student? Register here</u></html>",
                SwingConstants.CENTER);
        regLabel.setFont(BODY);
        regLabel.setForeground(ACCENT);
        regLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        g.gridy   = 8;
        g.insets  = new Insets(2, 0, 0, 0);
        root.add(regLabel, g);

        // Hint
        JLabel hint = new JLabel("Default admin — user: admin  pass: admin123",
                SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(SUBTEXT);
        g.gridy = 9;
        root.add(hint, g);

        // Actions
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            User user = library.login(u, p);
            if (user == null) {
                shake(loginBtn);
                JOptionPane.showMessageDialog(lf, "❌ Invalid username or password.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                currentUser = user;
                lf.dispose();
                if (user.getRole() == User.Role.ADMIN) showAdminUI();
                else showStudentUI();
            }
        });

        regLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                showRegisterDialog(lf);
            }
        });

        // Allow Enter key
        passField.addActionListener(e -> loginBtn.doClick());
        userField.addActionListener(e -> passField.requestFocus());

        lf.setContentPane(root);
        lf.setVisible(true);
    }

    private void showRegisterDialog(JFrame parent) {
        JDialog d = new JDialog(parent, "Register Student Account", true);
        d.setSize(420, 320);
        d.setLocationRelativeTo(parent);
        d.getContentPane().setBackground(BG);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.gridwidth = 1;
        g.weightx = 1;

        p.add(styledLabel("New Username"), constraint(g, 0));
        JTextField uField = styledField();
        g.gridy = 1; p.add(uField, g);

        p.add(styledLabel("Password"), constraint(g, 2));
        JPasswordField pField = new JPasswordField(); styleTextField(pField);
        g.gridy = 3; p.add(pField, g);

        p.add(styledLabel("Confirm Password"), constraint(g, 4));
        JPasswordField cField = new JPasswordField(); styleTextField(cField);
        g.gridy = 5; p.add(cField, g);

        JButton reg = accentButton("Create Account", ACCENT2);
        g.gridy = 6; g.insets = new Insets(12, 0, 0, 0); p.add(reg, g);

        reg.addActionListener(e -> {
            String u  = uField.getText().trim();
            String pw = new String(pField.getPassword());
            String cp = new String(cField.getPassword());
            if (u.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(d, "All fields are required.");
                return;
            }
            if (!pw.equals(cp)) {
                JOptionPane.showMessageDialog(d, "Passwords do not match.");
                return;
            }
            if (library.registerStudent(u, pw)) {
                JOptionPane.showMessageDialog(d, "✅ Account created! You can now log in.");
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "❌ Username already taken.");
            }
        });

        d.setContentPane(p);
        d.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMIN UI
    // ══════════════════════════════════════════════════════════════════════
    private void showAdminUI() {
        setTitle("🛠  Admin Panel — " + currentUser.getUsername());
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(PANEL_BG);
        tabs.setForeground(TEXT);

        tabs.addTab("➕ Add Book",        buildAdminAddPanel());
        tabs.addTab("📚 All Books",        buildAdminBooksPanel());
        tabs.addTab("📜 Borrow History",   buildHistoryPanel(null));
        tabs.addTab("👤 Students",         buildStudentsPanel());

        // Logout button in corner
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        JButton logout = smallButton("Logout", DANGER);
        logout.addActionListener(e -> { dispose(); currentUser = null; showLoginScreen(); });
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topBar.setBackground(BG);
        topBar.add(logout);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(tabs,   BorderLayout.CENTER);

        setContentPane(wrapper);
        setVisible(true);
    }

    // ── ADD BOOK ──────────────────────────────────────────────────────────
    private JPanel buildAdminAddPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form card
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(titledBorder("Add New Book"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 8, 6, 8);
        g.weightx = 1;

        JTextField nameF   = styledField();
        JTextField authorF = styledField();
        JTextField genreF  = styledField();
        JSpinner   copySp  = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        styleSpinner(copySp);

        int r = 0;
        addFormRow(form, g, r++, "Book Title *", nameF);
        addFormRow(form, g, r++, "Author *",     authorF);
        addFormRow(form, g, r++, "Genre",         genreF);
        addFormRow(form, g, r++, "No. of Copies", copySp);

        JButton addBtn = accentButton("➕  Add Book", ACCENT2);
        g.gridy = r; g.gridx = 1; g.insets = new Insets(14, 8, 6, 8);
        form.add(addBtn, g);

        // Table below the form — use shared model so All Books tab stays in sync
        addBookModel = makeBookModel();
        JTable table = styledTable(addBookModel);
        JScrollPane scroll = scroll(table, "Current Book Catalogue");

        populateBooks(addBookModel);

        // Refresh button for Add Book tab
        JButton refreshAddBtn = smallButton("🔄 Refresh", SUBTEXT);
        g.gridy = r; g.gridx = 1; g.insets = new Insets(4, 8, 6, 8);
        // We'll add it after addBtn row, so bump r
        g.gridy = r + 1; g.gridx = 1;
        form.add(refreshAddBtn, g);
        refreshAddBtn.addActionListener(e2 -> refreshAllBookTables());

        addBtn.addActionListener(e -> {
            String nm = nameF.getText().trim();
            String au = authorF.getText().trim();
            String ge = genreF.getText().trim();
            int copies = (int) copySp.getValue();
            if (nm.isEmpty() || au.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Author are required.");
                return;
            }
            library.addBook(nm, au, ge.isEmpty() ? "—" : ge, copies);
            nameF.setText(""); authorF.setText(""); genreF.setText(""); copySp.setValue(1);
            refreshAllBookTables();
            showMsg("✅ Book added successfully!");
        });

        root.add(form,   BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    // ── ALL BOOKS (with Edit & Delete) ────────────────────────────────────
    private JPanel buildAdminBooksPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        allBooksModel = makeBookModel();
        JTable table = styledTable(allBooksModel);
        populateBooks(allBooksModel);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btns.setBackground(BG);

        JButton editBtn   = accentButton("✏️  Edit Selected",   ACCENT);
        JButton deleteBtn = accentButton("🗑  Delete Selected", DANGER);
        JButton refreshBtn= smallButton("🔄 Refresh", SUBTEXT);
        btns.add(editBtn); btns.add(deleteBtn); btns.add(refreshBtn);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }
            int id = (int) allBooksModel.getValueAt(row, 0);
            Book b = library.getBook(id);
            if (b == null) return;
            showEditBookDialog(b, () -> refreshAllBookTables());
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }
            int id = (int) allBooksModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete book ID " + id + "? This cannot be undone.", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                library.deleteBook(id);
                refreshAllBookTables();   // ← refresh BOTH tables instantly
                showMsg("🗑 Book deleted.");
            }
        });

        refreshBtn.addActionListener(e -> refreshAllBookTables());

        root.add(btns,          BorderLayout.NORTH);
        root.add(scroll(table, "Book Catalogue"), BorderLayout.CENTER);
        return root;
    }

    private void showEditBookDialog(Book b, Runnable onSave) {
        JDialog d = new JDialog(this, "Edit Book — ID " + b.getId(), true);
        d.setSize(380, 360);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.insets = new Insets(5, 6, 5, 6);

        // ID field — shows current ID, user can change it
        JTextField idF     = styledField(); idF.setText(String.valueOf(b.getId()));
        JTextField nameF   = styledField(); nameF.setText(b.getBookName());
        JTextField authorF = styledField(); authorF.setText(b.getAuthorName());
        JTextField genreF  = styledField(); genreF.setText(b.getGenre());
        JSpinner   copySp  = new JSpinner(new SpinnerNumberModel(b.getTotalCopies(), 1, 999, 1));
        styleSpinner(copySp);

        // Subtle hint label under ID field
        JLabel idHint = new JLabel("Changing ID also updates all borrow history records.");
        idHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        idHint.setForeground(SUBTEXT);

        int r = 0;
        addFormRow(p, g, r++, "Book ID", idF);
        // Insert hint after ID row
        g.gridy = r * 2 - 1; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(0, 8, 4, 8);
        p.add(idHint, g);
        g.gridwidth = 1;

        addFormRow(p, g, r++, "Book Title", nameF);
        addFormRow(p, g, r++, "Author",     authorF);
        addFormRow(p, g, r++, "Genre",      genreF);
        addFormRow(p, g, r++, "Copies",     copySp);

        JButton save = accentButton("Save Changes", ACCENT2);
        g.gridy = r * 2 + 1; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(14, 6, 0, 6);
        p.add(save, g);

        save.addActionListener(e -> {
            String nm = nameF.getText().trim();
            String au = authorF.getText().trim();
            String idTxt = idF.getText().trim();
            if (nm.isEmpty() || au.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Title and Author required.");
                return;
            }
            // Validate and apply ID change if needed
            int newId;
            try {
                newId = Integer.parseInt(idTxt);
                if (newId <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "❗ Book ID must be a positive number.");
                return;
            }
            if (newId != b.getId()) {
                boolean changed = library.changeBookId(b.getId(), newId);
                if (!changed) {
                    JOptionPane.showMessageDialog(d, "❌ ID " + newId + " is already taken by another book.");
                    return;
                }
            }
            // Apply the rest of the edits using the (possibly new) ID
            library.editBook(newId, nm, au, genreF.getText().trim(), (int) copySp.getValue());
            onSave.run();
            showMsg("✅ Book updated.");
            d.dispose();
        });

        d.setContentPane(p);
        d.setVisible(true);
    }

    // ── BORROW HISTORY ────────────────────────────────────────────────────
    private JPanel buildHistoryPanel(String filterStudent) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        String[] cols = {"#", "Student", "Book ID", "Book Name", "Borrowed", "Due Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = styledTable(model);
        populateHistory(model, filterStudent);

        // colour overdue rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = (String) model.getValueAt(row, 6);
                if (!sel) {
                    if ("OVERDUE".equals(status))             c.setBackground(new Color(80, 30, 30));
                    else if (status != null && status.startsWith("Returned")) c.setBackground(new Color(20, 50, 35));
                    else                                       c.setBackground(CARD_BG);
                }
                c.setForeground(TEXT);
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JButton refresh = smallButton("Refresh", SUBTEXT);
        refresh.addActionListener(e -> populateHistory(model, filterStudent));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(BG);
        bar.add(refresh);
        JButton deleteBtn = new JButton("Delete");
        bar.add(deleteBtn);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select record");
                return;
            }

            int recordId = (int) model.getValueAt(row, 0);

            library.deleteRecord(recordId);
            populateHistory(model, filterStudent);
        });

        root.add(bar,                               BorderLayout.NORTH);
        root.add(scroll(table, "Borrow History"),   BorderLayout.CENTER);
        return root;
    }

    private void populateHistory(DefaultTableModel m, String filterStudent) {
        m.setRowCount(0);
        List<BorrowRecord> list = filterStudent == null
                ? library.getAllHistory()
                : library.getHistoryForStudent(filterStudent);
        for (BorrowRecord r : list) {
            m.addRow(new Object[]{
                    r.getRecordId(),
                    r.getStudentUsername(),
                    r.getBookId(),
                    r.getBookName(),
                    r.getBorrowDate().format(BorrowRecord.FMT),
                    r.getDueDate().format(BorrowRecord.FMT),
                    r.getStatus()
            });
        }
    }

    // ── STUDENTS ───────────────────────────────────────────────────────────
    private JPanel buildStudentsPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        String[] cols = {"Username", "Role", "Active Borrows"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        populateStudents(model);

        JButton refresh = smallButton("Refresh", SUBTEXT);
        refresh.addActionListener(e -> populateStudents(model));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); bar.setBackground(BG);
        bar.add(refresh);
        JButton deleteBtn = new JButton("Delete");
        bar.add(deleteBtn);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select student");
                return;
            }

            String username = (String) model.getValueAt(row, 0);

            library.deleteUser(username);
            populateStudents(model);
        });

        root.add(bar,                                  BorderLayout.NORTH);
        root.add(scroll(table, "Registered Students"), BorderLayout.CENTER);
        return root;
    }

    private void populateStudents(DefaultTableModel m) {
        m.setRowCount(0);
        for (User u : library.getAllUsers()) {
            if (u.getRole() == User.Role.STUDENT) {
                long active = library.getHistoryForStudent(u.getUsername())
                        .stream().filter(r -> !r.isReturned()).count();
                m.addRow(new Object[]{ u.getUsername(), "Student", active });
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDENT UI
    // ══════════════════════════════════════════════════════════════════════
    private void showStudentUI() {
        setTitle("🎓 Student Panel — " + currentUser.getUsername());
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📚 Browse & Borrow", buildStudentBorrowPanel());
        tabs.addTab("📜 My History",      buildHistoryPanel(currentUser.getUsername()));

        JButton logout = smallButton("Logout", DANGER);
        logout.addActionListener(e -> { dispose(); currentUser = null; showLoginScreen(); });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topBar.setBackground(BG);
        topBar.add(new JLabel("👤 " + currentUser.getUsername()) {{
            setForeground(SUBTEXT); setFont(BODY);
        }});
        topBar.add(logout);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(tabs,   BorderLayout.CENTER);

        setContentPane(wrapper);
        setVisible(true);
    }

    private JPanel buildStudentBorrowPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Controls
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        ctrl.setBackground(BG);
        ctrl.setBorder(titledBorder("Borrow / Return"));

        JLabel idLabel = styledLabel("Book ID:");
        JTextField idField = new JTextField(5); styleTextField(idField);

        JLabel dueLabel  = styledLabel("Due Date (dd-MM-yyyy):");
        JTextField dueField = new JTextField(10); styleTextField(dueField);
        dueField.setToolTipText("e.g. " + LocalDate.now().plusDays(14).format(BorrowRecord.FMT));

        JButton borrowBtn = accentButton("📥 Borrow", ACCENT2);
        JButton returnBtn = accentButton("📤 Return", ACCENT);
        JButton refreshBtn = smallButton("🔄 Refresh", SUBTEXT);

        ctrl.add(idLabel); ctrl.add(idField);
        ctrl.add(dueLabel); ctrl.add(dueField);
        ctrl.add(borrowBtn); ctrl.add(returnBtn); ctrl.add(refreshBtn);

        // Table
        DefaultTableModel model = makeBookModel();
        populateBooks(model);
        JTable table = styledTable(model);

        // Click row → fill ID
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) idField.setText(String.valueOf(model.getValueAt(row, 0)));
        });

        borrowBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String dueTxt = dueField.getText().trim();
                if (dueTxt.isEmpty()) { showMsg("Please enter a due date."); return; }
                LocalDate due = LocalDate.parse(dueTxt, BorrowRecord.FMT);
                if (!due.isAfter(LocalDate.now())) { showMsg("Due date must be in the future."); return; }
                int result = library.borrowBook(currentUser.getUsername(), id, due);
                switch (result) {
                    case  0 -> { showMsg("✅ Book borrowed until " + due.format(BorrowRecord.FMT) + "!"); populateBooks(model); }
                    case -1 -> showMsg("❌ Book ID not found.");
                    case -2 -> showMsg("❌ No copies available right now.");
                    case -3 -> showMsg("❌ You already have this book borrowed.");
                }
            } catch (NumberFormatException ex)   { showMsg("❗ Enter a valid numeric Book ID."); }
              catch (DateTimeParseException ex)   { showMsg("❗ Date must be in dd-MM-yyyy format."); }
        });

        returnBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (library.returnBook(currentUser.getUsername(), id))
                     showMsg("✅ Book returned successfully!");
                else showMsg("❌ No active borrow found for this book.");
                populateBooks(model);
            } catch (NumberFormatException ex) { showMsg("❗ Enter a valid numeric Book ID."); }
        });

        refreshBtn.addActionListener(e -> populateBooks(model));

        root.add(ctrl,                                 BorderLayout.NORTH);
        root.add(scroll(table, "Available Books"),     BorderLayout.CENTER);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS — models & population
    // ══════════════════════════════════════════════════════════════════════
    private DefaultTableModel makeBookModel() {
        return new DefaultTableModel(
                new String[]{"ID", "Title", "Author", "Genre", "Available", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /** Refreshes both the Add Book tab catalogue and the All Books tab table. */
    private void refreshAllBookTables() {
        if (addBookModel != null) populateBooks(addBookModel);
        if (allBooksModel != null) populateBooks(allBooksModel);
    }

    private void populateBooks(DefaultTableModel m) {
        m.setRowCount(0);
        for (Book b : library.getAllBooks()) {
            m.addRow(new Object[]{
                    b.getId(), b.getBookName(), b.getAuthorName(),
                    b.getGenre(), b.getAvailableCopies(), b.getTotalCopies()
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UI FACTORY HELPERS
    // ══════════════════════════════════════════════════════════════════════
    private JTextField styledField() {
        JTextField f = new JTextField(); styleTextField(f); return f;
    }

    private void styleTextField(JTextComponent f) {
        f.setBackground(CARD_BG);
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setFont(BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 120), 1),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(CARD_BG);
        sp.setForeground(TEXT);
        sp.setFont(BODY);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setBackground(CARD_BG);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setForeground(TEXT);
    }

    private JLabel styledLabel(String txt) {
        JLabel l = new JLabel(txt); l.setFont(LABEL); l.setForeground(SUBTEXT); return l;
    }

    private JButton accentButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(LABEL);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter(), 1),
                new EmptyBorder(7, 16, 7, 16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton smallButton(String text, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(CARD_BG);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD_BG);
        t.setForeground(TEXT);
        t.setFont(BODY);
        t.setRowHeight(26);
        t.setGridColor(new Color(45, 60, 90));
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(ACCENT);
        t.getTableHeader().setFont(LABEL);
        t.setSelectionBackground(ACCENT.darker());
        t.setSelectionForeground(Color.WHITE);
        return t;
    }

    private JScrollPane scroll(JTable t, String title) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.setBorder(titledBorder(title));
        return sp;
    }

    private Border titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 70, 110), 1), title);
        tb.setTitleColor(ACCENT);
        tb.setTitleFont(LABEL);
        return tb;
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;     g.gridx = 0; g.weightx = 0.3;
        g.insets = new Insets(4, 8, 0, 8);
        p.add(styledLabel(label), g);
        g.gridy = row * 2 + 1; g.gridx = 0; g.weightx = 1; g.gridwidth = 2;
        g.insets = new Insets(2, 8, 6, 8);
        p.add(field, g);
        g.gridwidth = 1;
    }

    private GridBagConstraints constraint(GridBagConstraints g, int row) {
        g.gridy = row; return g;
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    // Simple horizontal shake animation
    private void shake(JComponent c) {
        Point origin = c.getLocation();
        Timer t = new Timer(30, null);
        int[] offsets = {-6, 6, -4, 4, -2, 2, 0};
        int[] count = {0};
        t.addActionListener(e -> {
            if (count[0] < offsets.length) {
                c.setLocation(origin.x + offsets[count[0]++], origin.y);
            } else {
                c.setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }

    // ══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryApp::new);
    }
}
