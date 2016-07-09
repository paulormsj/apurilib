package apuri.com.br.apurilib.manager;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.auth.FirebaseAuth;

import apuri.com.br.apurilib.model.IApuriUser;
import apuri.com.br.apurilib.model.IApuriUserData;

/**
 * Created by paulo.junior on 05/07/2016.
 */
public interface IApuriUserManager {
    void createUserWithPassword(IApuriUser user, String password);

    void loginUserWithEmailAndPassword(String email, String password,Class<? extends  IApuriUser> userClass);

    boolean hasUser();

    <T extends  IApuriUser> T getUser(Class<T> clazz);

    IApuriUser  getUser();

    void addObserver(IApuriUserManagerObserver observer);

    void removeObserver(IApuriUserManagerObserver observer);

    void updateUserData(IApuriUserData userData);

    public final class Factory{

        private static Class<? extends IApuriUserManager> clazz;
        private static IApuriUserManager  instance;
        public static IApuriUserManager getInstance()  {
            if(instance == null) {
                if (Factory.clazz == null)
                    instance = new ApuriUserManager(FirebaseAuth.getInstance());
                else
                    try {
                        instance = clazz.newInstance();
                    } catch (Exception e) { //InstantiationException | IllegalAccessException
                        throw new RuntimeExecutionException(e);
                    }
            }
            return instance;
        }

        public static void setCustomClass(Class<? extends  IApuriUserManager> clazz){
            Factory.clazz = clazz;
        }

    }
}
