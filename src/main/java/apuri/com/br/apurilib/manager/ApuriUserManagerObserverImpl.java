package apuri.com.br.apurilib.manager;

import apuri.com.br.apurilib.exceptions.ApuriUserException;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 02/07/2016.
 */
public abstract class ApuriUserManagerObserverImpl implements ApuriUserManager.IApuriUserManagerObserver {

    public <T extends IApuriUser> void onCreateUser(T user) {
    }

    ;


    public void onFailCreateUser(ApuriUserException e) {
    }

    ;

    public <T extends IApuriUser> void onUserLogin(T user) {
    }

    ;

    public void onFailUserLogin(ApuriUserException e) {
    }

    public <T extends  IApuriUser> void onFetchUserData(T user){}


}
