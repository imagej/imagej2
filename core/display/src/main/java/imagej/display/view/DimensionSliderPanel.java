package imagej.display.view;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.TitledBorder;

/**
 *
 * @author GBH
 */
public class DimensionSliderPanel extends JPanel {
	/*
	 * CompositeSliderPanel
	 * If there is a channel dimension is displayed as a composite, 
	 * a slider for that dim should not be added.
	 */

	public DimensionSliderPanel(final DatasetView view) {
		setBorder(new TitledBorder(view.getImg().getName()));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(200, 18));
		// add one slider per dimension beyond the first two
		for (int d = 3; d < view.getImg().numDimensions(); d++) {
			final int dim = d;
			final int dimLength = (int) view.getImg().dimension(d);
			final JScrollBar bar = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, dimLength);
			bar.setPreferredSize(new Dimension(500,32));
			bar.addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					final int value = bar.getValue();
					//System.out.println("dim #" + dim + ": value->" + value);//TEMP
					view.setPosition(value, dim);
					view.project();
				}
			});
			add(bar);
		}
	}

}