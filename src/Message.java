import java.io.Serializable;

/**
 * Class used for object Serialization
 * @param <T> Generic type
 */
public class Message<T> implements Serializable {

    private T data;
    private static final long serialVersionUID = -2723363051271966964L;

    // Constructor
    public Message(T data){

        this.data=data;
    }

    // Getter
    public T getData(){

        return data;
    }

    // Setter
    public void setData(T data){

        this.data = data;
    }
}
