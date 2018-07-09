package TransportModels;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BusSchedule {
    public BusLine line;
    public List<Location> schedule;

    public BusSchedule()
    {
        line = new BusLine();
        schedule = new ArrayList<>();
    }

    public void parse(JSONObject busScheduleObject)
    {
        line.id = busScheduleObject.getString("idlinha");
        line.code = busScheduleObject.getString("codigo");
        line.name = busScheduleObject.getString("nome");

        int locationPosition = 0;
        String locationPositionName = "" + locationPosition;

        while(busScheduleObject.has(locationPositionName))
        {
            JSONObject locationObject = busScheduleObject.getJSONObject(locationPositionName);
            Location latLng = new Location();
            latLng.parse(locationObject);
            schedule.add(latLng);

            locationPositionName = "" + ++locationPosition;
        }
    }

    public JSONObject toJSONObject()
    {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("idlinha", line.id);
        jsonObject.put("codigo", line.code);
        jsonObject.put("nome", line.name);

        for (int locationPosition = 0; locationPosition < schedule.size(); locationPosition++)
        {
            Location location = schedule.get(locationPosition);
            String locationPositionName = "" + locationPosition;
            jsonObject.put(locationPositionName, location.toJSONObject());
        }

        return jsonObject;
    }
}
