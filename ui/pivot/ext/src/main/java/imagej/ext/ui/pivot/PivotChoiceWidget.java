
package imagej.ext.ui.pivot;

import imagej.ext.module.ui.ChoiceWidget;
import imagej.ext.module.ui.WidgetModel;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.ListButton;

/**
 * Pivot implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class PivotChoiceWidget extends PivotInputWidget
	implements ChoiceWidget
{

	private final ListButton listButton;

	public PivotChoiceWidget(final WidgetModel model, final String[] items) {
		super(model);

		listButton = new ListButton();
		listButton.setListData(new ArrayList<String>(items));
		add(listButton);

		refreshWidget();
	}

	// -- ChoiceWidget methods --

	@Override
	public String getValue() {
		return listButton.getSelectedItem().toString();
	}

	@Override
	public int getIndex() {
		return listButton.getSelectedIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		if (value.equals(listButton.getSelectedItem())) return; // no change
		listButton.setSelectedItem(value);
	}

}
