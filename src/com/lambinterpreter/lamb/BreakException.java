package com.lambinterpreter.lamb;

class BreakException extends RuntimeException{
    BreakException(){
        super(null, null, false, false);
    }
}
