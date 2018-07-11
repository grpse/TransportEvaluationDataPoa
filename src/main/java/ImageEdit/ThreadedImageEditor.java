package ImageEdit;

import Services.GoogleMapsAPIProjection;
import Services.GoogleMapsProjection;
import TransportModels.BusSchedule;
import TransportModels.Location;

import java.awt.*;
import java.util.List;

import static Services.GoogleMapsProjection.latToYWorld;

public class ThreadedImageEditor extends Thread {

    private final int start;
    private final int end;
    private final List<BusSchedule> busScheduleList;
    private final ImageEditor editor;
    private final int zoomLevel;
    private final Color32 TransparentRed;
    private float progress = 0f;

    public ThreadedImageEditor(int start, int end, List<BusSchedule> busScheduleList, ImageEditor editor, int zoomLevel)
    {
        this.TransparentRed = new Color32( 0.5f, 1, 0, 0);
        this.start = start;
        this.end = end;
        this.busScheduleList = busScheduleList;
        this.editor = editor;
        this.zoomLevel = zoomLevel;
    }

    public float getProgress()
    {
        return progress;
    }

    @Override
    public void run() {
        super.run();

        // Write pixels coordinates

        for (int i = this.start; i < this.end; i++)
        {
            BusSchedule busSchedule = this.busScheduleList.get(i);

            for (Location location : busSchedule.schedule)
            {

                Point p = GoogleMapsProjection.LatLngToPixelCoords(location, this.zoomLevel);
                this.editor.setPixel(p.x, p.y, this.TransparentRed);

                progress = i/(this.end - this.start);
            }
        }
    }
}
