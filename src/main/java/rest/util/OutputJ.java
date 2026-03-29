package rest.util;

public class OutputJ<T> {

    public String status;
    public T data;

    public OutputJ() {}

    public OutputJ(T data, String status){
        this.status = status;
        this.data = data;
    }
}
