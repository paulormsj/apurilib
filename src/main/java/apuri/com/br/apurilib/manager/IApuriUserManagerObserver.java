package apuri.com.br.apurilib.manager;

import apuri.com.br.apurilib.exceptions.ApuriUserException;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 09/07/2016.
 */
public interface IApuriUserManagerObserver {
    <T extends IApuriUser> void onCreateUser(T user);
    void onFailCreateUser(ApuriUserException e);
    <T extends  IApuriUser> void onUserLogin(T user);
    <T extends  IApuriUser> void onFetchUserData(T user);
    void onFailUserLogin(ApuriUserException e);

}
