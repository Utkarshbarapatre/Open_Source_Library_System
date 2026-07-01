import org.springframework.security.crypto.bcrypt.BCrypt;

public class HashGen {
    public static void main(String[] args) {
        String salt = BCrypt.gensalt(10);
        System.out.println("ADMIN: " + BCrypt.hashpw("Admin123!", salt));
        String salt2 = BCrypt.gensalt(10);
        System.out.println("USER:  " + BCrypt.hashpw("Password123!", salt2));
    }
}
