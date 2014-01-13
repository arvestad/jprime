package se.cbb.jprime.apps.vmcmc.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{
	private static final long serialVersionUID = 1L;
	Image img;

    public ImagePanel(Image img) {
        this.img = img;
        Dimension dm = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(dm);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(this.img, 0, 0, null);
    }
}
