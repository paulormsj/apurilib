package apuri.com.br.apurilib.exceptions;

/**
 * Created by paulo.junior on 02/07/2016.
 */
public final class ApuriUserException extends  Exception{

    public enum Code{
        PASSWORD_TO_SHORT,INVALID_CRENDENTIALS,INVALID_USER,NO_CONNECTION_AVAILABLE, UNKNOWN, USER_ALREADY_EXIST;
    }

    private Code code;
    private Exception e;


    public ApuriUserException(Code cause, Exception source){
        super(source);
        this.code = cause;
    }

    public Code getCode(){
        return code;
    }

    public Exception getSourceException(){
        return e;
    }

}
