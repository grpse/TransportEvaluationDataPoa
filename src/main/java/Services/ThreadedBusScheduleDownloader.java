package Services;

import TransportModels.BusLine;
import TransportModels.BusSchedule;

import java.util.ArrayList;
import java.util.List;

public class ThreadedBusScheduleDownloader extends Thread {

    private final int start;
    private final int end;
    private final List<BusLine> busLineList;
    private final List<BusSchedule> busScheduleList;
    private float progress;

    public ThreadedBusScheduleDownloader(int start, int end, List<BusLine> busLineList)
    {

        this.start = start;
        this.end = end;
        this.busLineList = busLineList;
        busScheduleList = new ArrayList<>();
    }

    public float getProcess()
    {
        return progress;
    }

    public List<BusSchedule> getBusScheduleList()
    {
        return busScheduleList;
    }

    @Override
    public void run() {
        super.run();

        for (int i = this.start; i < this.end; i++)
        {
            BusLine busLine = this.busLineList.get(i);
            BusSchedule busSchedule = PoaTransporte.GetScheduleOf(busLine.id);
            busScheduleList.add(busSchedule);

            progress = i/(this.end - this.start);
        }
    }
}
