package com.example.distrapp.phase1Code;
import java.io.Serializable;


/**
 * CLASS USED FOR OBJECT SERIALIZATION
 */

public class Message<T> implements Serializable {

    private T data;

    private static final long serialVersionUID = -2723363051271966964L;

    public Message(T data){

        this.data=data;
    }


    public T getData(){

        return data;
    }

    public void setData(T data){

        this.data = data;
    }
}
