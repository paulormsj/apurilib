package apuri.com.br.apurilib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.test.mock.MockContext;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import apuri.com.br.apurilib.ApuriLib;
import apuri.com.br.apurilib.exceptions.ApuriUserException;
import apuri.com.br.apurilib.model.IApuriUser;
import apuri.com.br.apurilib.model.IApuriUserData;

/**
 * Created by paulo.junior on 01/07/2016.
 */
@SuppressWarnings("unused")
public final class ApuriUserManager implements  FirebaseAuth.AuthStateListener, IApuriUserManager {

    public static final String USER_DATA_ENTRY = "userData";
    private static ApuriUserManager instance;
    private FirebaseAuth auth;
    private List<IApuriUserManagerObserver> observers;
    private IApuriUser currentUser;


    protected ApuriUserManager(FirebaseAuth auth){
        this.auth = auth;
        observers = new ArrayList<>();

    }

    void checkValidUserAndPassword(IApuriUser user, String password){
        if(user == null)
            throw new InvalidParameterException("User can not be null");
        if(password == null || password.equals(""))
            throw new InvalidParameterException("Can not create an user without password");
        if(user.getEmail() == null || user.getEmail().equals(""))
            throw new InvalidParameterException("User must have an email");
    }

    @Override
    public void createUserWithPassword(final IApuriUser user, final String password){

        checkValidUserAndPassword(user,password);

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
                ApuriUserException customException = null;
                if(e instanceof FirebaseAuthWeakPasswordException)
                    customException = new ApuriUserException(ApuriUserException.Code.PASSWORD_TO_SHORT,e);
                else if (e instanceof FirebaseAuthUserCollisionException)
                    customException = new ApuriUserException(ApuriUserException.Code.USER_ALREADY_EXIST, e);
                else if (e instanceof  FirebaseAuthInvalidCredentialsException)
                    customException = new ApuriUserException(ApuriUserException.Code.INVALID_CRENDENTIALS, e);
                else
                    customException = new ApuriUserException(ApuriUserException.Code.UNKNOWN,e);
                notifyFailUserCreation(customException);
            }
        });
    }

    @Override
    public void loginUserWithEmailAndPassword(final String email, String password,final Class<? extends  IApuriUser> userClass){
        if(email == null || email.isEmpty())
            throw new InvalidParameterException("Email can not be empty or null");
        if(password == null || password.isEmpty())
            throw new InvalidParameterException("Password can not be empty or null");

        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                try {
                    IApuriUser mUser = userClass.newInstance();
                    mUser.setName(authResult.getUser().getDisplayName());
                    mUser.setEmail(email);
                    setCurrentUser(mUser,authResult);
                    notifyUserLogin();
                    fetchCurrentUserData();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fetchCurrentUserData() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USER_DATA_ENTRY);
        reference.equalTo("uid",currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ApuriUserManager.this.currentUser.setUserData(dataSnapshot.getValue(currentUser.getUserData().getClass()));
                notifyFetchUserData();
                reference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void notifyFetchUserData() {
        for(IApuriUserManagerObserver observer:this.observers)
            observer.onFetchUserData(this.currentUser);
    }

    private void notifyUserLogin() {
        for(IApuriUserManagerObserver observer:this.observers)
            observer.onUserLogin(this.currentUser);
    }


    private void setCurrentUser(IApuriUser user, AuthResult authResult) {
        this.currentUser = user;
        this.currentUser.setUid(authResult.getUser().getUid());
        ApuriLib.getContext().getSharedPreferences("conf",Context.MODE_APPEND)
                .edit().putBoolean("hasUser",true).apply();
    }

    @Override
    public boolean hasUser(){
        SharedPreferences preferences = ApuriLib.getContext().getSharedPreferences("conf",Context.MODE_APPEND);
        return preferences.getBoolean("hasUser",false);

    }

    @Override
    public <T extends  IApuriUser> T getUser(Class<T> clazz){
        return (T)this.currentUser;
    }

    @Override
    public IApuriUser  getUser(){
        return this.currentUser;
    }


    @Override
    public void addObserver(IApuriUserManagerObserver observer){
        if(observer == null)
            return;
        if(!this.observers.contains(observer))
            this.observers.add(observer);
    }

    @Override
    public void removeObserver(IApuriUserManagerObserver observer){
        if (observer != null) {
            this.observers.remove(observer);
        }
    }

    private void notifyFailUserCreation(ApuriUserException e) {
        for(IApuriUserManagerObserver observer : this.observers)
            observer.onFailCreateUser(e);
    }

    private void notifyUserCreated(IApuriUser user) {
        for(IApuriUserManagerObserver observer : this.observers)
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

    @Override
    public void updateUserData(IApuriUserData userData) {

    }

    public interface IApuriUserManagerObserver {
         <T extends  IApuriUser> void onCreateUser(T user);
         void onFailCreateUser(ApuriUserException e);
         <T extends  IApuriUser> void onUserLogin(T user);
        <T extends  IApuriUser> void onFetchUserData(T user);
         void onFailUserLogin(ApuriUserException e);

    }

}
