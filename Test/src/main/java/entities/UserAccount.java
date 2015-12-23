package entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for user account
 */
@XmlRootElement
public class UserAccount {

    private String userEmail;
    private String passwd;
    private String oldPasswd;
    private String resetClause;

    public UserAccount() {

    }

    public String getUserEmail() {
        return userEmail;
    }

    @XmlElement
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPasswd() {
        return passwd;
    }

    @XmlElement
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getOldPasswd() {
        return oldPasswd;
    }

    @XmlElement
    public void setOldPasswd(String oldPasswd) {
        this.oldPasswd = oldPasswd;
    }

    public String getResetClause() {
        return resetClause;
    }

    @XmlElement
    public void setResetClause(String resetClause) {
        this.resetClause = resetClause;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "resetClause='" + resetClause + '\'' +
                ", passwd='" + passwd + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
