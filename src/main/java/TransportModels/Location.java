package TransportModels;

import org.json.JSONObject;

public class Location {
    public double latitude, longitude;

    public String toString()
    {
        return ""+latitude + "," + longitude;
    }

    public void parse(JSONObject jsonObject)
    {
        latitude = Double.parseDouble(jsonObject.getString("lat"));
        longitude = Double.parseDouble(jsonObject.getString("lng"));
    }

    public JSONObject toJSONObject()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lat", ""+latitude);
        jsonObject.put("lng", ""+longitude);
        return jsonObject;
    }
}
