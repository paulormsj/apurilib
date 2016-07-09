package apuri.com.br.apurilib;

import android.content.Context;

/**
 * Created by paulo.junior on 05/07/2016.
 */
public class ApuriLib {

    private static Context context;


    public static void configure (Context context){
        ApuriLib.context = context;
    }

    public static Context getContext(){
        if(ApuriLib.context == null)
            throw new IllegalStateException("ApuriLib not configured. Call 'configure' first");
        return context;
    }


}
