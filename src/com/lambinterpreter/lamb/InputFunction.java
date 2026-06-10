package com.lambinterpreter.lamb;

import java.util.List;
import java.util.Scanner;

public class InputFunction implements LambCallable{
    private static final Scanner stdin = new Scanner(System.in);

    @Override
    public int arity(){
        return -1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
         if (arguments.size() > 1) {
            throw new RuntimeException(
                "input() accepts at most one argument."
            );
        }
        if(!arguments.isEmpty()){
            System.out.print(
                interpreter.stringify(arguments.get(0))
            );
        }

        return stdin.nextLine();
    }

    @Override
    public String toString(){
        return "<native fn>";
    }
}
