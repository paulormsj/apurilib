package apuri.com.br.apurilib.model;

/**
 * Created by paulo.junior on 03/07/2016.
 */
public interface IApuriChatMessage {

    long getTimestamp();
    void setTimestamp(long timestamp);

    String getText();
    void setText(String text);

    String getUserUid();
    void setUserUid(String userUid);
}
