package rest.util;

public class Operation<T> {

    public T input;
    public TokenData token;

    public Operation() { };

    public Operation(T input, TokenData token){
        this.input = input;
        this.token = token;
    };
}
