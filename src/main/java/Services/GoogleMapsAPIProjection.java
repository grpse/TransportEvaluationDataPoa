package Services;

import TransportModels.Location;

import java.util.Locale;

public class GoogleMapsAPIProjection
{
    private final double PixelTileSize = 256d;
    private final double DegreesToRadiansRatio = 180d / Math.PI;
    private final double RadiansToDegreesRatio = Math.PI / 180d;
    private final GoogleMapsProjection.Vector2 PixelGlobeCenter;
    private final double XPixelsToDegreesRatio;
    private final double YPixelsToRadiansRatio;

    public GoogleMapsAPIProjection(double zoomLevel)
    {
        double pixelGlobeSize = this.PixelTileSize * Math.pow(2d, zoomLevel);
        this.XPixelsToDegreesRatio = pixelGlobeSize / 360d;
        this.YPixelsToRadiansRatio = pixelGlobeSize / (2d * Math.PI);
        float halfPixelGlobeSize = (float)(pixelGlobeSize / 2d);
        this.PixelGlobeCenter = new GoogleMapsProjection.Vector2(halfPixelGlobeSize, halfPixelGlobeSize);
    }

    public GoogleMapsProjection.Vector2 FromCoordinatesToPixel(Location coordinates)
    {
        double x = Math.round(this.PixelGlobeCenter.x + (coordinates.latitude * this.XPixelsToDegreesRatio));
        double f = Math.min(Math.max(Math.sin(coordinates.longitude * RadiansToDegreesRatio), -0.9999d), 0.9999d);
        double y = Math.round(this.PixelGlobeCenter.y + .5d * Math.log((1d + f) / (1d - f)) * -this.YPixelsToRadiansRatio);
        return new GoogleMapsProjection.Vector2((float)x/1280, (float) y/1280);
    }

    public Location FromPixelToCoordinates(GoogleMapsProjection.Vector2 pixel)
    {
        double longitude = (pixel.x - this.PixelGlobeCenter.x) /
                this.XPixelsToDegreesRatio;
        double latitude = (2 * Math.atan(Math.exp(
                (pixel.y - this.PixelGlobeCenter.y) / -this.YPixelsToRadiansRatio))
                - Math.PI / 2) * DegreesToRadiansRatio;
        return new Location(latitude, longitude);
    }
}
