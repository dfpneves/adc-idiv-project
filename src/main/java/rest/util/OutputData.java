package rest.util;

import com.google.gson.JsonObject;

public class OutputData {


    public OutputData(){}

    public JsonObject getOut(String status, String msg){

        JsonObject outJson = new JsonObject();

        JsonObject dataJson = new JsonObject();

        outJson.addProperty("status", status);
        dataJson.addProperty("message", msg);
        outJson.add("data", dataJson);
        return outJson;
    }

    public JsonObject getOut(JsonObject data){
        JsonObject outJson = new JsonObject();

        outJson.addProperty("status", "success");
        outJson.add("data", data);
        return outJson;
    }

    public JsonObject getOutError(ErrorRecord err){

        JsonObject outJson = new JsonObject();

        JsonObject dataJson = new JsonObject();

        outJson.addProperty("status", err.code());
        dataJson.addProperty("message", err.msg());
        outJson.add("data", dataJson);
        return outJson;
    }
}
