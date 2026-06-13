package com.lambinterpreter.lamb;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.System;
import java.lang.Thread;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals =  new HashMap<>();

    Interpreter(){
        globals.define("clock", new LambCallable(){
            @Override
            public int arity() {return 0;}
    
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double)System.currentTimeMillis()/1000.0;
            }
    
            @Override
            public String toString() {return "<native fn>";}
        });

        globals.define("input", new InputFunction());

        globals.define("countSheep", new LambCallable(){
            @Override
            public int arity(){
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                Object arg = arguments.get(0);

                if(!(arg instanceof Double)){
                    throw new RuntimeError(null, "CountSheep() expects a number.");
                }

                int n = ((Double) arg).intValue();
                try{
                    for(int i = 1; i <= n; i++){
                        System.out.println("🐑 "+ i + " sheep");
                        Thread.sleep(1000);
                    }
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }

                return null;
            }

            @Override
            public String toString(){
                return "<native fn>";
            }
        });

        globals.define("__SHEPHERD__", "Deebyanshu Jha, Keeper of the Flock");
        globals.define("__LAMB__", "Lamb v0.1");
    }

    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement: statements){
                execute(statement);
            }
        }catch(RuntimeError error){
            Lamb.runtimeError(error);
        }
    }

    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth){
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try{
            this.environment = environment;
            for(Stmt statement: statements){
                execute(statement);
            }
        }finally{
            this.environment = previous;
        }
    }



    String stringify(Object object){
        if(object == null) return "nil";

        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt){
        throw new BreakException();
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt){
        throw new ContinueException();
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        Object superclass = null;
        if(stmt.superclass != null){
            superclass = evaluate(stmt.superclass);
            if(!(superclass instanceof LambCallable)){
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if(stmt.superclass != null){
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LambFunction> methods = new HashMap<>();

        for(Stmt.Function method : stmt.methods){
            LambFunction function = new LambFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        LambClass klass = new LambClass(stmt.name.lexeme,(LambClass) superclass, methods);

        if(superclass != null){
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object value = null;
        if(stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        LambFunction function = new LambFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.arguments){
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LambCallable)){
            throw new RuntimeError(expr.paren, "Can only call function and classes");
        }

        LambCallable function = (LambCallable)callee;
        if(function.arity() != -1 && arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr){
        int distance = locals.get(expr);
        LambClass superClass = (LambClass)environment.getAt(distance, "super");
        LambInstance object = (LambInstance)environment.getAt(distance - 1, "this");
        LambFunction method = superClass.findMethod(expr.method.lexeme);
        if(method == null){
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr){
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr){
        Object object = evaluate(expr.object);

        if(object instanceof LambInstance){
            return ((LambInstance) object).get(expr.name);
        }
        
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr){
        Object object = evaluate(expr.object);

        if(!(object instanceof LambInstance)){
            throw new RuntimeError(expr.name, "Only instances have fields");
        }

        Object value = evaluate(expr.value);
        ((LambInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while(isTruthy(evaluate(stmt.condition))){
            try{
                execute(stmt.body);
            }catch(ContinueException e){
                continue;
            }catch(BreakException e){
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        }else if(stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;

    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object value = null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left;
        }else{
            if(!isTruthy(left)) return left; // for and short circuit
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr){
        Integer distance = locals.get(expr);
        if(distance != null){
            return environment.getAt(distance, name.lexeme);
        }else{
            return globals.get(name);
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        
        Integer distance = locals.get(expr);
        if(distance != null){
            environment.assignAt(distance, expr.name.lexeme, value);
        }else{
            globals.assign(expr.name, value);
        }

        return value;
    }
    
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);
        
        switch(expr.operator.type){
            case MINUS :
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }        
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operator must be a number");
    }

    // Lamb follows Ruby’s simple rule: false and nil are falsey, and everything else is truthy. We implement that like so:
    private boolean isTruthy(Object object){
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return  (double)left * (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double){
                    return (double)left + (double)right;
                }
                if(left instanceof String && right instanceof String){
                    return (String)left + (String)right;
                }
                if(left instanceof String && right instanceof Double){
                    String text = String.valueOf(right);
                    if(text.endsWith(".0")){ 
                        text = text.substring(0, text.length() - 2);
                    }
                    return (String)left+ text;
                }
                if(left instanceof Double && right instanceof String){
                    String text = String.valueOf(left);
                    if(text.endsWith(".0")){ 
                        text = text.substring(0, text.length() - 2);
                    }
                    return text + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }

    private boolean isEqual(Object a, Object b){
        if(a == null && b == null) return true;
        if(a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}
