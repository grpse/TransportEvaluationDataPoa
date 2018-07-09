package Services;

import TransportModels.Location;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MapsDataGenerator
{

    static final String MapStaticApiUrl = "http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=1920x1080&maptype=roadmap&visible=29.64,-13.09&visible=27.38,-18.53";
    static final String MapStaticApiUrlWithPlaceholders = "http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=%dx%d&maptype=%s&visible=%.2f,$.2f&visible=%.2f,$.2f";
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
        int width = 1920, height = 1080;
        String type = "roadmap";
        Location min, max;

        public static MapsRequestBuilder Builder()
        {
            return new MapsRequestBuilder();
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

        public MapsRequestBuilder setExtents(Location min, Location max)
        {
            this.min = min;
            this.max = max;
            return this;
        }

        public String build()
        {
            String builtUrl = String.format(
                    MapStaticApiUrlWithPlaceholders,
                    width,
                    height,
                    type,
                    min.latitude,
                    min.longitude,
                    max.latitude,
                    max.longitude);
            return builtUrl;
        }
    }
}
