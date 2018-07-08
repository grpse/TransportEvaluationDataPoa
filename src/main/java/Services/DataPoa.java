package Services;

import TransportModels.BusLine;
import TransportModels.BusSchedule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataPoa
{

    private static final String BusScheduleUrl = "http://www.poatransporte.com.br/php/facades/process.php?a=il&p=#ID#&t=o";
    private static final String BusLinesUrl = "http://www.poatransporte.com.br/php/facades/process.php?a=nc&p=%&t=o";

    public static List<BusSchedule> GetBusSchedules()
    {
        List<BusLine> busLineList = GetBusLines();
        List<BusSchedule> busScheduleList = new ArrayList<>();

        for (BusLine busLine : busLineList)
        {
            BusSchedule busSchedule = GetScheduleOf(busLine.id);
            busScheduleList.add(busSchedule);
        }

        return busScheduleList;
    }

    public static List<BusLine> GetBusLines()
    {
        JSONArray jsonArray = GetJsonArray(BusLinesUrl);

        List<BusLine> busLines = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            BusLine busLine = new BusLine();
            busLine.parse(jsonObject);

            busLines.add(busLine);
        }

        return busLines;
    }

    public static BusSchedule GetScheduleOf(String lineId)
    {
        String busScheduleUrlWithLineId =  BusScheduleUrl.replace("#ID#", lineId);
        JSONObject jsonObject = GetJson(busScheduleUrlWithLineId);
        BusSchedule busSchedule = new BusSchedule();
        busSchedule.parse(jsonObject);

        return busSchedule;
    }

    private static JSONArray GetJsonArray(String url)
    {
        String output = Get(url);
        JSONArray jsonArray = new JSONArray(output);
        return jsonArray;
    }

    private static JSONObject GetJson(String url)
    {
        String output = Get(url);
        JSONObject jsonObject = new JSONObject(output);
        return jsonObject;
    }

    private static String Get(String url)
    {
        try {

            Client client = Client.create();

            WebResource webResource = client.resource(url);

            ClientResponse response = webResource
                    .accept("application/json")
                    .get(ClientResponse.class);

            if (response.getStatus() == 200)
            {
                String output = response.getEntity(String.class);
                return output;
            }
            else
            {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
