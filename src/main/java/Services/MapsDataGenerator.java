package Services;

import ImageEdit.ImageEditor;
import ImageEdit.ThreadedImageEditor;
import TransportModels.BusSchedule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.client.utils.URIBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MapsDataGenerator
{
    static final String MapsStaticApiBaseUrl = "http://maps.googleapis.com/maps/api/staticmap";
    static final String MapStaticApiUrl = "http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=1920x1080&maptype=roadmap&visible=29.64,-13.09&visible=27.38,-18.53";
    static final String MapStaticApiUrlWithPlaceholders = "http://maps.googleapis.com/maps/api/staticmap?scale=%d&zoom=%d&size=%dx%d&maptype=%s&visible=%.6f,%.6f&visible=%.6f,%.6f";
    static final String MapImageFileCached = "maps_image.png";
    public static Path GetMapImage(String url)
    {
        byte[] imageData = Get(url);
        try {
            return Files.write(Paths.get(MapImageFileCached), imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] Get(String url)
    {
        try {

            Client client = Client.create();

            URI uri = new URI(url);
            String encodedURL = uri.toASCIIString();

            WebResource webResource = client.resource(encodedURL);

            ClientResponse response = webResource.get(ClientResponse.class);

            if (response.getStatus() == 200)
            {
                byte[] output = response.getEntity(byte[].class);
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

    public static class MapsRequestBuilder
    {
        int width = 1280, height = 1280, scale = 2, zoom = 11;
        String type = "roadmap";

        PoaTransporte.Extents extents;

        public static MapsRequestBuilder Builder()
        {
            return new MapsRequestBuilder();
        }

        public MapsRequestBuilder setScale(int scale)
        {
            this.scale = scale;

            return this;
        }

        public MapsRequestBuilder setSize(int width, int height)
        {
            this.width = width;
            this.height = height;

            return this;
        }

        public MapsRequestBuilder setType(String type)
        {
            this.type = type;
            return this;
        }

        public MapsRequestBuilder setZoom(int zoom)
        {
            this.zoom = zoom;
            return this;
        }

        public MapsRequestBuilder setExtents(PoaTransporte.Extents extents)
        {
            this.extents = extents;
            return this;
        }

        public String build()
        {
            try {
                URIBuilder b = new URIBuilder(MapsStaticApiBaseUrl);
                b.addParameter("scale", String.valueOf(scale));
                b.addParameter("zoom", String.valueOf(zoom));
                b.addParameter("size", String.format("%dx%d", width, height));
                b.addParameter("maptype", type);
                b.addParameter("visible", extents.min.toString());
                b.addParameter("visible", extents.max.toString());


                String path = "color:0xff0000ff|weight:1";
                path += "|" + extents.min;
                path += "|" + extents.min.latitude + "," + extents.max.longitude;
                path += "|" + extents.max;
                path += "|" + extents.max.latitude + "," + extents.min.longitude;
                path += "|" + extents.min;

                b.addParameter("path", path);


                return b.toString();

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return "";
        }
    }

    public static Path PopulateMapWithIntensityData(Path baseImage, Path mapImagePath, List<BusSchedule> busScheduleList, int threadCount, int zoomLevel, JProgressBar progressBar)
    {
        try
        {

            BufferedImage img = ImageIO.read(baseImage.toFile());
            ImageEditor editor = new ImageEditor(img);

            // Divide threads between bus schedules
            ThreadedImageEditor[] threads = new ThreadedImageEditor[threadCount];
            int threadDataDivision = (int)Math.floor(busScheduleList.size() / threadCount);

            for (int i = 0; i < threadCount; i++)
            {
                // Define thread data boundaries
                int threadBoundStart = threadDataDivision * i;
                int threadBoundEnd = threadDataDivision * (i + 1);

                // last thread, if it's not a round division, have to access the remaining slot of data
                if (i == threadCount - 1 && busScheduleList.size() % threadCount > 0 && threadCount > 1)
                {
                    threadBoundEnd += 1;
                }

                // start image editor thread
                threads[i] = new ThreadedImageEditor(threadBoundStart, threadBoundEnd, busScheduleList, editor, zoomLevel);
                threads[i].start();
            }

            new Thread(() -> {
                synchronized (progressBar)
                {
                    float totalProgress = 0f;
                    while(totalProgress < 100)
                    {
                        totalProgress = 0f;


                        for (int i = 0; i < threads.length; i++)
                        {
                            totalProgress += threads[i].getProgress();
                        }

                        totalProgress /= threads.length;
                        totalProgress = Math.round(totalProgress * 100);
                        progressBar.setValue((int)totalProgress);
                    }

                    System.out.println("Done Processing Image");
                }
            }).start();

            for (int i = 0; i < threadCount; i++)
            {
                threads[i].join();
            }

            editor.saveImage(mapImagePath);

            return mapImagePath;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
