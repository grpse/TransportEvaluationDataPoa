import Services.DataPoa;
import TransportModels.BusLine;
import TransportModels.BusSchedule;

import java.util.List;


public class BusTransportVolumeVisualization {

    static String dataPath = "";

    public static void main(String[] args)
    {
        dataPath = System.getProperty("user.dir") + "\\src\\main\\resources";
        System.out.println("Working Dir: " + dataPath);

        List<BusSchedule> busScheduleList = DataPoa.GetBusSchedulesCached();

        System.out.println(busScheduleList.get(0).line.name);
    }
}
