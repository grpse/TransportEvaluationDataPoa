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

            latLng.latitude = Double.parseDouble(locationObject.getString("lat"));
            latLng.longitude = Double.parseDouble(locationObject.getString("lng"));

            schedule.add(latLng);

            locationPositionName = "" + ++locationPosition;
        }
    }
}
