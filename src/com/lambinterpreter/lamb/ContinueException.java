package com.lambinterpreter.lamb;

public class ContinueException extends RuntimeException{
    public ContinueException(){
        super(null, null, false, false);
    }
}
