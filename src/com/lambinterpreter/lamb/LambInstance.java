package com.lambinterpreter.lamb;

import java.util.HashMap;
import java.util.Map;

class LambInstance {
    private LambClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LambInstance(LambClass klass){
        this.klass = klass;
    }

    Object get(Token name){
        if(fields.containsKey(name.lexeme)){
            return fields.get(name.lexeme);
        }

        LambFunction method = klass.findMethod(name.lexeme);
        if(method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefiend property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value){
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
