import Services.DataPoa;
import Services.MapsDataGenerator;
import TransportModels.BusLine;
import TransportModels.BusSchedule;
import Services.MapsDataGenerator.*;

import java.util.List;


public class BusTransportVolumeVisualization {

    static String dataPath = "";

    public static void main(String[] args)
    {
        dataPath = System.getProperty("user.dir") + "\\src\\main\\resources";
        System.out.println("Working Dir: " + dataPath);

        List<BusSchedule> busScheduleList = DataPoa.GetBusSchedulesCached();

        //busScheduleList.

        //System.out.println(busScheduleList.get(0).line.name);

        //MapsRequestBuilder builder = MapsRequestBuilder.Builder().setExtents()
    }
}
