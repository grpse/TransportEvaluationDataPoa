package TransportModels;

import org.json.JSONObject;

public class BusLine {
    public String id, code, name;

    public void parse(JSONObject jsonObject)
    {
        id = jsonObject.getString("id");
        code = jsonObject.getString("codigo");
        name = jsonObject.getString("nome");
    }

    public JSONObject toJSONObject()
    {
        JSONObject busLine = new JSONObject();
        busLine.put("id", id);
        busLine.put("codigo", code);
        busLine.put("nome", name);

        return busLine;
    }
}
