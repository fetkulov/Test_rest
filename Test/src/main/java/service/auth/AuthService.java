package service.auth;

import entities.UserAccount;

/**
 * Created by IFet on 23.12.15.
 */
public interface AuthService {

    Boolean login(String userEmail, String passwHash);

    String passwdReset(String email);

    UserAccount resetPasswd(String email, String resetClause, String newPasswd);

    UserAccount changePasswd(String email, String oldPasswd, String newPasswd);
}
