package apuri.com.br.apurilib.model;

import java.util.Map;

/**
 * Created by paulo.junior on 03/07/2016.
 */
public interface  IApuriChatRoomMembers {

    Map<String,Boolean> getMembersMap();
    void setMembersMap(Map<String,Boolean> map);
}
