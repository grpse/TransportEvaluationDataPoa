package Services;

import TransportModels.Location;

import java.awt.Point;


/**
 * Contains the Google Maps map projection logic.
 *
 * @author Irina Smidt, Simon Sprankel
 * @see http://de.slideshare.net/lodeblomme/google-maps-projection-and-how-to-use-it-for-clustering-presentation
 * @see https://developers.google.com/maps/documentation/javascript/maptypes#MapCoordinates
 */
public class GoogleMapsProjection {

    private static int widthMultiplier = -320;
    private static int heightMultiplier = -320;
    private static int widthOffset = -640;
    private static int heightOffset = -1080;

    public static void SetCorrdinatesExtentsModifier(int widthMultiplier, int heightMultiplier, int widthOffset, int heightOffset)
    {
        GoogleMapsProjection.widthMultiplier = widthMultiplier;
        GoogleMapsProjection.heightMultiplier = heightMultiplier;
        GoogleMapsProjection.widthOffset = widthOffset;
        GoogleMapsProjection.heightOffset = heightOffset;
    }

    public static final double TILE_SIZE = 256.0;

    public static final double[] CBK = new double[]{128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824};//, 2147483648, 4294967296, 8589934592, 17179869184, 34359738368, 68719476736, 137438953472};
    public static final double[] CEK = new double[]{0.7111111111111111, 1.4222222222222223, 2.8444444444444446, 5.688888888888889, 11.377777777777778, 22.755555555555556, 45.51111111111111, 91.02222222222223, 182.04444444444445, 364.0888888888889, 728.1777777777778, 1456.3555555555556, 2912.711111111111, 5825.422222222222, 11650.844444444445, 23301.68888888889, 46603.37777777778, 93206.75555555556, 186413.51111111112, 372827.02222222224, 745654.0444444445, 1491308.088888889, 2982616.177777778, 5965232.355555556, 11930464.711111112, 23860929.422222223, 47721858.844444446, 95443717.68888889, 190887435.37777779, 381774870.75555557, 763549741.5111111};
    public static final double[] CFK = new double[]{40.74366543152521, 81.48733086305042, 162.97466172610083, 325.94932345220167, 651.8986469044033, 1303.7972938088067, 2607.5945876176133, 5215.189175235227, 10430.378350470453, 20860.756700940907, 41721.51340188181, 83443.02680376363, 166886.05360752725, 333772.1072150545, 667544.214430109, 1335088.428860218, 2670176.857720436, 5340353.715440872, 10680707.430881744, 21361414.86176349, 42722829.72352698, 85445659.44705395, 170891318.8941079, 341782637.7882158, 683565275.5764316, 1367130551.1528633, 2734261102.3057265, 5468522204.611453, 10937044409.222906, 21874088818.445812, 43748177636.891624};

    public static Point LatLngToPixel(Location location, int zoom)
    {
        double cbk = CBK[zoom];

        int x = (int)(Math.round(cbk + (location.longitude * CEK[zoom])));

        double foo = Math.sin(location.latitude * Math.PI / 180.0);
        if (foo < -0.9999)
            foo = -0.9999;
        else if (foo > 0.9999)
            foo = 0.9999;

        int y = (int)(Math.round(cbk + (0.5 * Math.log((1+foo)/(1-foo)) * (-CFK[zoom]))));

        return new Point(x/1280, y/1280);
    }

    /**
     * Converts the given longitude value to the x value of the Google Maps world coordinate.
     *
     * @param lon the longitude value
     * @return the x value of the corresponding Google Maps world coordinate
     */
    public static Double lonToXWorld(Double lon) {
        Double tiles = Math.pow(2, 0);
        Double circumference = 256 * tiles;
        Double radius = circumference / (2 * Math.PI);
        Double falseEasting = -1.0 * circumference / 2.0;
        return (radius * Math.toRadians(lon)) - falseEasting;
    }

    /**
     * Converts the given latitude value to the y value of the Google Maps world coordinate.
     *
     * @param lat the latitude value
     * @return the y value of the corresponding Google Maps world coordinate
     */
    public static Double latToYWorld(Double lat) {
        Double tiles = Math.pow(2, 0);
        Double circumference = 256 * tiles;
        Double radius = circumference / (2 * Math.PI);
        Double falseNorthing = circumference / 2.0;
        return ((radius / 2.0 * Math.log((1.0 + Math.sin(Math.toRadians(lat)))
                / (1.0 - Math.sin(Math.toRadians(lat))))) - falseNorthing)
                * -1;
    }

    /**
     * Converts the given world coordinates to the pixel coordinates corresponding to the given zoom level.
     *
     * @param xWorld the x value of the world coordinate
     * @param yWorld the y value of the world coordinate
     * @param zoomLevel the zoom level
     * @return the pixel coordinates as a Point
     */
    public static Point worldToPixel(Double xWorld, Double yWorld, int zoomLevel) {
        int zoom = (int) Math.pow(2, zoomLevel);
        int x = (int) Math.round(xWorld * zoom);
        int y = (int) Math.round(yWorld * zoom);
        return new Point(x, y);
    }

    public static class Vector2{
        public float x;
        public float y;
        public Vector2(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        public Vector2()
        {

        }
    }

    public static Point LatLngToPixelCoords(Location location, int zoom)
    {

        double lon_rad = Math.toRadians(location.longitude);
        double lat_rad = Math.toRadians(location.latitude);
        double n = 1 << zoom; //Math.pow(2.0, zoom);

        double tileX = ((location.longitude + 180) / 360) * n;
        double tileY = (1 - (Math.log(Math.tan(lat_rad) + 1.0/Math.cos(lat_rad)) / Math.PI)) * n / 2.0;

        long tilexIntPart = (long)tileX;
        long tileyIntPart = (long)tileY;

        double tilexFractPart = tilexIntPart - tileX;
        double tileyFractPart = tileyIntPart - tileY;

        int x = (int)((tilexIntPart + tilexFractPart * widthMultiplier) + widthOffset);
        int y = (int)((tileyIntPart + tileyFractPart * heightMultiplier) + heightOffset);
        return new Point(x, y);
    }

    public static Vector2 ToWorldCoordinates(Location location)
    {
        double siny = Math.sin(location.latitude * Math.PI / 180);

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);

        int x = (int) (TILE_SIZE * (0.5 + location.longitude / 360));
        int y = (int) (TILE_SIZE * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI)));
        return new Vector2(x, y);
    }
}
