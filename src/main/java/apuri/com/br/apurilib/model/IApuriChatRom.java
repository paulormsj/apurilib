package apuri.com.br.apurilib.model;

/**
 * Created by paulo.junior on 03/07/2016.
 */
public interface IApuriChatRom {
    String getRoomName();
    void setRoomName(String name);

    String getOwnerId();
    void setOwnerId(String ownerId);

    void setKey(String key);
    String getKey();
}
