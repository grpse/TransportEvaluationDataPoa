package MapViewer.Graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JPanelBackgroundImage extends JPanel {

        private BufferedImage image;

        public void setImage(BufferedImage image)
        {
            this.image = image;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            if (this.image != null)
            {
                g.drawImage(this.image, 0, 0, null);
            }

        }
}
