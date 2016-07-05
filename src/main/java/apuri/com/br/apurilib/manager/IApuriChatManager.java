package apuri.com.br.apurilib.manager;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import apuri.com.br.apurilib.model.IApuriChatMessage;
import apuri.com.br.apurilib.model.IApuriChatRom;
import apuri.com.br.apurilib.model.IApuriChatRoomMembers;
import apuri.com.br.apurilib.model.IApuriUser;

/**
 * Created by paulo.junior on 05/07/2016.
 */
public interface IApuriChatManager {

    void createChat(IApuriChatRom rom, IApuriChatRoomMembers members, IApuriChatMessage message);

    void sendMessage(String message, IApuriChatRom rom);

    void addMembersToChat(IApuriChatRom rom, List<IApuriUser> members);

    public final class Factory{
        private static IApuriChatManager instance;
        private static Class<? extends  IApuriChatManager> clazz;


        public static IApuriChatManager getInstance() {
            if(instance == null){
                if(clazz == null)
                    instance = new ApuriChatManager();
                else
                    try {
                        instance = clazz.newInstance();
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }
            }
            return instance;
        }

        public static void setCustomClass(Class<? extends  IApuriChatManager> clazz){
            Factory.clazz = clazz;
        }
    }
}
