package edu.ucsf.rbvi.seqViz.internal.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Anti-aliasing code of Scooter Morris.
 * @author Scooter Morris
 *
 */
class AntiAlias {
	static void antiAliasing(Graphics2D g2) {
		 /* Set up all of our anti-aliasing, etc. here to avoid doing it redundantly */
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
		RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	
		    // High quality color rendering is ON.
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
		                        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	
		g2.setRenderingHint(RenderingHints.KEY_DITHERING,
		                        RenderingHints.VALUE_DITHER_ENABLE);
	
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	
		    // Text antialiasing is ON.
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		                        RenderingHints.VALUE_STROKE_PURE);
	}
}
