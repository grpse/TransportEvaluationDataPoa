import Services.PoaTransporte;
import Services.MapsDataGenerator;
import TransportModels.BusSchedule;
import Services.MapsDataGenerator.*;

import java.nio.file.Path;
import java.util.List;


public class BusTransportVolumeVisualization {

    static String dataPath = "";

    public static void main(String[] args)
    {
        dataPath = System.getProperty("user.dir") + "\\src\\main\\resources";
        System.out.println("Working Dir: " + dataPath);

        List<BusSchedule> busScheduleList = PoaTransporte.GetBusSchedulesCached();
        PoaTransporte.Extents extents = PoaTransporte.FindExtents(busScheduleList);

        MapsRequestBuilder builder = MapsRequestBuilder.Builder().setExtents(extents).setSize(1920,1080).setScale(2);
        String mapsRequestUrl = builder.build();
        System.out.println("Maps request url: " + mapsRequestUrl);
        System.out.println("Extends: " + extents);
        Path imagePath = MapsDataGenerator.GetMapImage(mapsRequestUrl);
    }
}
