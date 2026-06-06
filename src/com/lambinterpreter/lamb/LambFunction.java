package com.lambinterpreter.lamb;

import java.util.List;

class LambFunction implements LambCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LambFunction(Stmt.Function declaration, Environment closure, boolean isInitializer){
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    } 

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        Environment environment = new Environment(closure);
        for(int i = 0; i < declaration.params.size(); i++){
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        try{
            interpreter.executeBlock(declaration.body, environment);
        }catch(Return returnValue){
            if(isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        return null;
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }

    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";
    }

    LambFunction bind(LambInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LambFunction(declaration, environment, isInitializer); 
    }
}
