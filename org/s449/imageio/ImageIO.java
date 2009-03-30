package org.s449.imageio;

import java.awt.*;
import javax.swing.*;
import sun.awt.image.codec.JPEGImageEncoderImpl;
import java.awt.image.*;
import java.io.*;

public class ImageIO {
	public static boolean write(BufferedImage im, String format, File file) {
		try {
			// assert format == "jpeg"
			FileOutputStream fos = new FileOutputStream(file);
			JPEGImageEncoderImpl i = new JPEGImageEncoderImpl(fos);
			i.encode(im);
			fos.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	public static Image read(File file) {
		ImageIcon im = new ImageIcon(file.getAbsolutePath());
		MediaTracker media = new MediaTracker(new Panel());
		media.addImage(im.getImage(), 0);
		try {
			media.waitForID(0);
		} catch (Exception e) {
			return null;
		}
		return im.getImage();
	}
}