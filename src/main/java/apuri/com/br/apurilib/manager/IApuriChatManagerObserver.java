package apuri.com.br.apurilib.manager;

import java.util.List;

import apuri.com.br.apurilib.exceptions.ApuriChatException;
import apuri.com.br.apurilib.model.IApuriChatMessage;
import apuri.com.br.apurilib.model.IApuriChatRoom;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 09/07/2016.
 */
public interface IApuriChatManagerObserver {

    <T extends IApuriChatRoom> void onCreateRoom(T rom);
    void onFailToCreateRoom(ApuriChatException e);
    <T extends IApuriChatRoom, M extends IApuriChatMessage> void onMessageSent(T room, M message);
    void onFailToSentMessage(ApuriChatException e);
    <T extends IApuriChatRoom, U extends IApuriUser> void onAddMembersToRoom(List<? extends IApuriUser> user, IApuriChatRoom room);
    void onFailToAddMemberToRoom (ApuriChatException e);
}
