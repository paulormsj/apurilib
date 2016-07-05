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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apuri.com.br.apurilib.exceptions.ApuriUserException;
import apuri.com.br.apurilib.model.IApuriChatMessage;
import apuri.com.br.apurilib.model.IApuriChatRom;
import apuri.com.br.apurilib.model.IApuriChatRoomMembers;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 03/07/2016.
 */
public class ApuriChatManager {

    public static final String CHATS_ENTRY = "chats";
    public static final String USERS_CHATS_ENTRY = "users_" + CHATS_ENTRY;
    public static final String CHATS_MESSAGES_ENTRY = CHATS_ENTRY + "_messages";

    private List<String> userChatRooms;


    public void createChat(IApuriChatRom rom, final IApuriChatRoomMembers members, IApuriChatMessage message) {
        if (rom == null)
            throw new InvalidParameterException("Parameter 'rom' can not be null");
        if (members == null)
            throw new InvalidParameterException("Parameter 'members' can not be null");
        if (message == null)
            throw new InvalidParameterException("Parameter 'message' can not be null");

        validateChatRom(rom);

        ApuriUserManager userManager = null;

        try{
            userManager = ApuriUserManager.getInstance();
        }catch (Exception e){

        }
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        //DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats");
        final String chatKey = db.getReference(CHATS_ENTRY).push().getKey();
        Map<String, Object> creationMap = new HashMap<>();
        creationMap.put("/" + CHATS_ENTRY + "/" +chatKey,rom);
        for(String memberId : members.getMembersMap().keySet())
            creationMap.put("/"+USERS_CHATS_ENTRY+"/"+memberId+"/"+chatKey,true);
        String messageKey = db.getReferenceFromUrl("/" + CHATS_MESSAGES_ENTRY + "/" +chatKey).push().getKey();
        creationMap.put("/" + CHATS_MESSAGES_ENTRY + "/" +chatKey+"/"+messageKey,message);

        db.getReference().updateChildren(creationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    private void validateChatRom(IApuriChatRom rom) {

    }

    public void sendMessage(String message, IApuriChatRom rom){

        if(userChatRooms.contains(rom.getKey())){
            proceedWithMessage(message,rom);
        }else{
            checkPermissionToSendMessage(message,rom);
        }


    }

    private void checkPermissionToSendMessage(final String message,final IApuriChatRom rom) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS_CHATS_ENTRY);
        ref.child(ApuriUserManager.getInstance().getUser().getUid()).child(rom.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null && (Boolean)dataSnapshot.getValue()){
                    proceedWithMessage(message,rom);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void proceedWithMessage(String message, IApuriChatRom rom) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CHATS_MESSAGES_ENTRY);
        ref.child(rom.getKey()).push().child(message);
    }

    public void addMembersToChat(IApuriChatRom rom, List<IApuriUser> members){
        IApuriUser user = ApuriUserManager.getInstance().getUser();
        if(rom.getOwnerId().equals(user.getUid())){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS_CHATS_ENTRY);
            Map<String, Object> creationMap = new HashMap<>();
            for (IApuriUser newMember: members) {
                creationMap.put("/"+newMember.getUid()+"/"+rom.getKey(),true);
            }
            ref.updateChildren(creationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

}
