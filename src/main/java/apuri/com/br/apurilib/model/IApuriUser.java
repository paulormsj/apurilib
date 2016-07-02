package apuri.com.br.apurilib.model;

/**
 * Created by paulo.junior on 01/07/2016.
 */
public interface IApuriUser {
    String getEmail();
    String getName();
    String getUid();
    void setUid(String uid);
    void setName(String name);
    void setEmail(String email);

    IApuriUserData getUserData();
}
