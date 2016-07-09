package apuri.com.br.apurilib.manager;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apuri.com.br.apurilib.model.IApuriChatMessage;
import apuri.com.br.apurilib.model.IApuriChatRoom;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 03/07/2016.
 */
public class ApuriChatManager implements IApuriChatManager {

    public static final String CHATS_ENTRY = "chats";
    public static final String USERS_CHATS_ENTRY = "users_" + CHATS_ENTRY;
    public static final String CHATS_MESSAGES_ENTRY = CHATS_ENTRY + "_messages";

    private List<IApuriChatManagerObserver> observers;

    private List<String> userChatRooms;

    protected  ApuriChatManager(){
        observers = new ArrayList<>();
    }


    @Override
    public void createChat(final IApuriChatRoom room, final List<? extends IApuriUser> members, final IApuriChatMessage message) {
        if (room == null)
            throw new InvalidParameterException("Parameter 'room' can not be null");
        if (members == null)
            throw new InvalidParameterException("Parameter 'members' can not be null");
        if (message == null)
            throw new InvalidParameterException("Parameter 'message' can not be null");

        validateChatRoom(room);

        IApuriUserManager userManager = null;

        userManager = IApuriUserManager.Factory.getInstance();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final String chatKey = db.getReference(CHATS_ENTRY).push().getKey();

        Map<String, Object> creationMap = new HashMap<>();
        creationMap.put("/" + CHATS_ENTRY + "/" +chatKey,room);
        for(IApuriUser member: members)
            creationMap.put("/"+USERS_CHATS_ENTRY+"/"+member.getUid()+"/"+chatKey,true);
        String messageKey = db.getReferenceFromUrl("/" + CHATS_MESSAGES_ENTRY + "/" +chatKey).push().getKey();
        creationMap.put("/" + CHATS_MESSAGES_ENTRY + "/" +chatKey+"/"+messageKey,message);

        db.getReference().updateChildren(creationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                room.setKey(chatKey);
                notifyOnCreateChatRoom(room);
                notifyOnMessageSent(room,message);
                notifyOnAddMemberToChat(room,members);
            }
        });
    }

    private void notifyOnAddMemberToChat(IApuriChatRoom room, List<? extends IApuriUser> members) {
        for(IApuriChatManagerObserver observer: observers){
            observer.onAddMembersToRoom(members,room);
        }
    }

    private void notifyOnMessageSent(IApuriChatRoom room, IApuriChatMessage message) {
        for(IApuriChatManagerObserver observer : observers)
            observer.onMessageSent(room,message);
    }

    private void notifyOnCreateChatRoom(IApuriChatRoom room) {
        for(IApuriChatManagerObserver observer : observers)
            observer.onCreateRoom(room);
    }

    private void validateChatRoom(IApuriChatRoom room) {

    }

    @Override
    public void sendMessage(IApuriChatMessage message, IApuriChatRoom room){


        if(userChatRooms.contains(room.getKey())){
            proceedWithMessage(message,room);
        }else{
            checkPermissionToSendMessage(message,room);
        }


    }

    private void checkPermissionToSendMessage(final IApuriChatMessage message,final IApuriChatRoom room) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS_CHATS_ENTRY);
        ref.child(IApuriUserManager.Factory.getInstance().getUser().getUid()).child(room.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null && (Boolean)dataSnapshot.getValue()){
                    proceedWithMessage(message,room);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void proceedWithMessage(IApuriChatMessage message, IApuriChatRoom room) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CHATS_MESSAGES_ENTRY);
        ref.child(room.getKey()).push().setValue(message);
        notifyOnMessageSent(room,message);
    }

    @Override
    public void addMembersToChat(final IApuriChatRoom room, final List<? extends IApuriUser> members){
        IApuriUser user = IApuriUserManager.Factory.getInstance().getUser();
        if(room.getOwnerId().equals(user.getUid())){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS_CHATS_ENTRY);
            Map<String, Object> creationMap = new HashMap<>();
            for (IApuriUser newMember: members) {
                creationMap.put("/"+newMember.getUid()+"/"+room.getKey(),true);
            }
            ref.updateChildren(creationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    notifyOnAddMemberToChat(room,members);
                }
            });
        }
    }

}
