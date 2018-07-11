package BusTransportVolume;

import MapViewer.Graphics.JPanelBackgroundImage;
import Services.GoogleMapsProjection;
import Services.MapsDataGenerator;
import Services.PoaTransporte;
import TransportModels.BusSchedule;
import sun.nio.ch.ThreadPool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapViewer {
    private JSpinner widthMultiplier;
    private JSpinner heightMultiplier;
    public JButton Run;
    private JSpinner widthOffset;
    private JSpinner heightOffset;
    private JPanelBackgroundImage panel;
    private JPanel mainPanel;
    private JSpinner zoomLevel;
    private JSpinner threadCount;
    private JProgressBar progressProcessing;
    private JLabel numberOfBusLines;
    private JLabel gpsDataCollected;
    private JLabel meanGPSdataCollected;
    private JLabel busScheduleDownloadTime;
    private JLabel timeToProcessImage;
    private BufferedImage mapImage;
    private AtomicBoolean runningMapTask;

    public MapViewer() {

        // Make a start setup for GUI
        runningMapTask = new AtomicBoolean(false);
        progressProcessing.setMinimum(0);
        progressProcessing.setMaximum(100);
        widthMultiplier.setValue(-320);
        heightMultiplier.setValue(-320);
        widthOffset.setValue(-640);
        heightOffset.setValue(-1080);
        zoomLevel.setValue(11);
        SpinnerNumberModel threadCounterModel = new SpinnerNumberModel(1, 1, 10, 1);
        threadCount.setModel(threadCounterModel);
        threadCount.setValue(4);

        Run.addActionListener(e -> new Thread(() -> this.runProcessing()).start());
    }

    private void runProcessing()
    {
        if (runningMapTask.get()) return;
        runningMapTask.set(true);
        progressProcessing.setValue(0);

        // Select zoom level of the map and threads amount
        int valueZoomLevel = (int)zoomLevel.getValue();
        int valueThreadCount = (int)threadCount.getValue();

        // Download and cache bus schedules
        long startTime = System.currentTimeMillis();
        List<BusSchedule> busScheduleList = PoaTransporte.GetBusSchedulesCached(progressProcessing, valueThreadCount);
        long endTime = System.currentTimeMillis();
        busScheduleDownloadTime.setText("" + (endTime - startTime) + " ms");

        PoaTransporte.Extents extents = PoaTransporte.FindExtents(busScheduleList);

        // Setup interface data
        numberOfBusLines.setText(""+busScheduleList.size());

        // Calculate sum of bus schedules data
        int totalNumberOfGPSDataCollected = 0;
        for (BusSchedule busSchedule : busScheduleList)
        {
            totalNumberOfGPSDataCollected += busSchedule.schedule.size();
        }
        gpsDataCollected.setText("" + totalNumberOfGPSDataCollected);
        meanGPSdataCollected.setText("" + totalNumberOfGPSDataCollected/busScheduleList.size());

        // Define some modifiers to adjust google maps pixel projection
        GoogleMapsProjection.SetCorrdinatesExtentsModifier(
                (int)widthMultiplier.getValue(),
                (int)heightMultiplier.getValue(),
                (int)widthOffset.getValue(),
                (int)heightOffset.getValue());

        // Build a request url for google maps static map
        MapsDataGenerator.MapsRequestBuilder builder = MapsDataGenerator.MapsRequestBuilder.Builder().setExtents(extents).setScale(1).setZoom(valueZoomLevel);
        String mapsRequestUrl = builder.build();

        // Download requested image of map with POA extents
        Path imagePath = MapsDataGenerator.GetMapImage(mapsRequestUrl);

        // Process image with GPS data of bus schedule
        startTime = System.currentTimeMillis();
        Path resultImage = MapsDataGenerator.PopulateMapWithIntensityData(imagePath, Paths.get("new_image.png"), busScheduleList, valueThreadCount , valueZoomLevel, progressProcessing);
        endTime = System.currentTimeMillis();
        timeToProcessImage.setText("" + (endTime - startTime) + " ms");

        try {

            // Set image to panel of view
            mapImage = ImageIO.read(resultImage.toFile());
            panel.setImage(mapImage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        runningMapTask.set(false);
    }

    public static void main(String[] args)
    {
        MapViewer viewer = new MapViewer();
        JFrame appFrame = new JFrame("MapViewer");
        appFrame.setContentPane(viewer.mainPanel);
        appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        appFrame.pack();
        appFrame.setVisible(true);
    }
}
