package apuri.com.br.apurilib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import apuri.com.br.apurilib.model.IApuriUser;
import apuri.com.br.apurilib.model.IApuriUserData;

/**
 * Created by paulo.junior on 01/07/2016.
 */
@SuppressWarnings("unused")
public final class UserManager implements  FirebaseAuth.AuthStateListener {

    public static final String USER_DATA_ENTRY = "userData";
    private static UserManager instance;
    private FirebaseAuth auth;
    private List<IUserManagerObserver> observers;
    private IApuriUser currentUser;
    private Context context;

    private UserManager(Context context){
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(this);
        observers = new ArrayList<>();
        this.context = context;
    }

    public static void configure(Context context){
        if(context == null)
            throw new InvalidParameterException("Parameter context can not be null");
        if(instance == null){
            instance = new UserManager(context);
        }else
            throw new IllegalStateException("Already configured");
    }

    public static UserManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call configure first");
        }
        return instance;
    }

    public void createUserWithPassword(final IApuriUser user, final String password){
        auth.createUserWithEmailAndPassword(user.getEmail(),password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                setCurrentUser(user,authResult);
                updateUserProfile(user,authResult);
                if(user.getUserData() != null)
                    createUserData(authResult.getUser().getUid(), user.getUserData());
                notifyUserCreated(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                notifyFailUserCreation(e);
            }
        });
    }

    private void setCurrentUser(IApuriUser user, AuthResult authResult) {
        this.currentUser = user;
        this.currentUser.setUid(authResult.getUser().getUid());
        this.context.getSharedPreferences("conf",Context.MODE_APPEND)
                .edit().putBoolean("hasUser",true).apply();
    }

    public boolean hasUser(){
        SharedPreferences preferences = context.getSharedPreferences("conf",Context.MODE_APPEND);
        return preferences.getBoolean("hasUser",false);

    }

    public <T extends  IApuriUser> T getUser(Class<T> clazz){
        return (T)this.currentUser;
    }


    public void addObserver(IUserManagerObserver observer){
        if(observer == null)
            return;
        if(!this.observers.contains(observer))
            this.observers.add(observer);
    }

    public void removeObserver(IUserManagerObserver observer){
        if (observer != null) {
            this.observers.remove(observer);
        }
    }

    private void notifyFailUserCreation(Exception e) {
        for(IUserManagerObserver observer : this.observers)
            observer.onFailCreateUser(e);
    }

    private void notifyUserCreated(IApuriUser user) {
        for(IUserManagerObserver observer : this.observers)
            observer.onCreateUser(user);
    }

    private void updateUserProfile(IApuriUser user, AuthResult authResult) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().
                setDisplayName(user.getName()).build();
        authResult.getUser().updateProfile(request);
    }

    private void createUserData(String uid,IApuriUserData userData) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USER_DATA_ENTRY);
        reference.child(uid).setValue(userData);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

    }

    public void updateUserData(IApuriUserData userData) {

    }

    public interface IUserManagerObserver{
         <T extends  IApuriUser> void onCreateUser(T user);
         void onFailCreateUser(Exception e);
         <T extends  IApuriUser> void onUserLogin(T user);
    }

    public abstract  static class UserManagerObserverImpl implements  IUserManagerObserver{
        public <T extends  IApuriUser> void onCreateUser(T user){};
        public void onFailCreateUser(Exception e){};
        public <T extends  IApuriUser> void onUserLogin(T user){};
    }
}
