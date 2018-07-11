package ImageEdit;

import Services.MapsDataGenerator;

public class Color32 {
    public float r = 0;
    public float g = 0;
    public float b = 0;
    public float a = 0;

    public Color32(float a, float r, float g, float b)
    {

        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color32()
    {

    }

    public int getARGB()
    {
        int a = (int) Math.floor(this.a*255);
        int r = (int) Math.floor(this.r*255);
        int g = (int) Math.floor(this.g*255);
        int b = (int) Math.floor(this.b*255);
        return (a << 24 | r << 16 | g << 8 | b);
    }

    public byte[] toByteArray()
    {
        return new byte[] {
                (byte) Math.floor(this.a*255),
                (byte) Math.floor(this.r*255),
                (byte) Math.floor(this.g*255),
                (byte) Math.floor(this.b*255)
        };
    }
}
