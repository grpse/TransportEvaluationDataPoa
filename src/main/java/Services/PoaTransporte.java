package Services;

import TransportModels.BusLine;
import TransportModels.BusSchedule;
import TransportModels.Location;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.uri.UriComponent;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.net.www.http.HttpClient;

import javax.swing.*;
import javax.xml.ws.WebServiceClient;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PoaTransporte
{

    private static final String BusScheduleUrl = "http://www.poatransporte.com.br/php/facades/process.php?a=il&p=#ID#&t=o";
    private static final String BusLinesUrl = "http://www.poatransporte.com.br/php/facades/process.php?a=nc&p=&t=o";
    private static final String BusSchedulesCachedFile = "bus_schedules.json";

    public static class Extents
    {
        public Location min = new Location();
        public Location max = new Location();

        public String toString()
        {
            return "Min: " + min + "\n" + "Max: " + max;
        }
    }

    public static Extents FindExtents(List<BusSchedule> busScheduleList)
    {
        Extents extents = new Extents();
        extents.max.latitude = Double.NEGATIVE_INFINITY;
        extents.max.longitude = Double.NEGATIVE_INFINITY;
        extents.min.latitude = Double.POSITIVE_INFINITY;
        extents.min.longitude = Double.POSITIVE_INFINITY;

        System.out.println(extents);

        for (BusSchedule busSchedule : busScheduleList)
        {
            for (Location location : busSchedule.schedule)
            {
                if (location.latitude > extents.max.latitude)
                    extents.max.latitude = location.latitude;

                if (location.longitude > extents.max.longitude)
                    extents.max.longitude = location.longitude;

                if (location.latitude < extents.min.latitude)
                    extents.min.latitude = location.latitude;

                if (location.longitude < extents.min.longitude)
                    extents.min.longitude = location.longitude;
            }
        }

        return extents;
    }

    public static List<BusSchedule> GetBusSchedulesCached(JProgressBar progressProcessing, int threadCount)
    {
        if (Files.exists(Paths.get(BusSchedulesCachedFile)))
        {
            try {
                byte[] jsonContent = Files.readAllBytes(Paths.get(BusSchedulesCachedFile));
                String jsonStringContent = new String(jsonContent, Charset.defaultCharset());
                JSONArray jsonArray = new JSONArray(jsonStringContent);
                return ParseBusSchedule(jsonArray);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            List<BusSchedule> busScheduleList = GetBusSchedules(progressProcessing, threadCount);
            JSONArray jsonArray = ToJSONArray(busScheduleList);
            try {
                Files.write(Paths.get(BusSchedulesCachedFile), jsonArray.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return busScheduleList;
        }
        return null;
    }

    public static List<BusSchedule> GetBusSchedules(JProgressBar progressProcessing, int threadCount)
    {
        List<BusLine> busLineList = GetBusLines();
        List<BusSchedule> busScheduleList = new ArrayList<BusSchedule>();


        ThreadedBusScheduleDownloader[] thread = new ThreadedBusScheduleDownloader[threadCount];

        // Divide data to download between n threads
        int dataDivision = busLineList.size()/threadCount;

        // Start download of data for each thread
        for (int i = 0; i < threadCount; i++)
        {
            int start = i * dataDivision;
            int end = (i + 1) * dataDivision;

            if (i == threadCount - 1 && busLineList.size()/(float)threadCount > 0)
            {
                end += 1;
            }

            thread[i] = new ThreadedBusScheduleDownloader(start, end, busLineList);
            thread[i].start();
        }

        // thread that monitors the percentage of download
        new Thread(() -> {

            synchronized (progressProcessing)
            {
                float totalProgress = 0;
                while(totalProgress < 100)
                {
                    for (int i = 0; i < threadCount; i++)
                    {
                        totalProgress += thread[i].getProcess();
                    }

                    totalProgress /= thread.length;

                    totalProgress = Math.round(totalProgress * 100);
                    progressProcessing.setValue((int) totalProgress);
                }
            }
        }).start();

        // Join each individual thread
        for (int i = 0; i < threadCount; i++)
        {
            try {
                thread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Adding all individual data together
        for (int i = 0; i < threadCount; i++)
        {
            busScheduleList.addAll(thread[i].getBusScheduleList());
        }
        return busScheduleList;
    }

    public static List<BusSchedule> GetBusSchedules()
    {
        List<BusLine> busLineList = GetBusLines();
        List<BusSchedule> busScheduleList = new ArrayList<>();

        int downloadedCount = 0;
        System.out.println("Bus schedules downloaded...0%");
        for (BusLine busLine : busLineList)
        {
            BusSchedule busSchedule = GetScheduleOf(busLine.id);
            busScheduleList.add(busSchedule);
            double percentageDownloaded = 100.0 * ++downloadedCount/busLineList.size();
            String downloadedBusLines = String.format("Bus schedules downloaded...%.2f", percentageDownloaded ) + "%";
            System.out.println(downloadedBusLines);
        }

        System.out.println("Bus schedules downloaded...100%");

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

            URI uri = new URI(url);
            String encodedURL = uri.toASCIIString();

            WebResource webResource = client.resource(encodedURL);

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

    private static JSONArray ToJSONArray(List<BusSchedule> busScheduleList)
    {
        JSONArray jsonArray = new JSONArray();

        for (BusSchedule busSchedule : busScheduleList)
        {
            jsonArray.put(busSchedule.toJSONObject());
        }

        return jsonArray;
    }

    private static List<BusSchedule> ParseBusSchedule(JSONArray jsonArray)
    {
        List<BusSchedule> busScheduleList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++)
        {
            BusSchedule busSchedule = new BusSchedule();
            busSchedule.parse(jsonArray.getJSONObject(i));
            busScheduleList.add(busSchedule);
        }

        return busScheduleList;
    }
}
