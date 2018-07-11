package ImageEdit;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.file.Path;

public class ImageEditor {

    private BufferedImage img;
    private byte[] pixels = null;
    private int width, height;
    private boolean hasAlphaChannel;
    private int pixelLength;
    private Object[] mutexes;

    public ImageEditor(BufferedImage img)
    {
        this.img = img;
        pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        width = img.getWidth();
        height = img.getHeight();
        hasAlphaChannel = img.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }

        mutexes = new Object[pixels.length];
        for (int i = 0; i < pixels.length; i++)
        {
            mutexes[i] = new Object();
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setPixel(int x, int y, Color32 c)
    {
        if (y > height || y < 0 || x > width || x < 0)
        {
            return;
        }

        int position = (y * width) + x;
        synchronized (mutexes[position])
        {
            Graphics g = img.createGraphics();
            g.setColor(Color.red);
            g.fillRect(x, y, 2, 2);

            img.setRGB(x, y, c.getARGB());
        }
    }

    public Color32 getPixel(int x, int y)
    {
        Color32 c = new Color32();
        int position = (y * pixelLength * width) + (x * pixelLength);

        if (hasAlphaChannel)
        {
            c.a = pixels[position + 0];
            c.r = pixels[position + 1];
            c.g = pixels[position + 2];
            c.b = pixels[position + 3];
        }
        else
        {
            c.r = pixels[position + 0];
            c.g = pixels[position + 1];
            c.b = pixels[position + 2];
        }

        return c;
    }

    public void saveImage(Path filePath)
    {
        try {
            ImageIO.write(img, "png", filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
