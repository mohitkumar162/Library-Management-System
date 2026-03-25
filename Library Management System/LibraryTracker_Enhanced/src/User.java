import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { ADMIN, STUDENT }

    private final String username;
    private String passwordHash; // simple SHA-256 hex
    private final Role role;

    public User(String username, String passwordHash, Role role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role   getRole()         { return role; }

    public void setPasswordHash(String h) { this.passwordHash = h; }

    // Simple SHA-256 helper
    public static String hash(String plain) {
        try {
            java.security.MessageDigest md =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return plain; // fallback (should never happen)
        }
    }
}
