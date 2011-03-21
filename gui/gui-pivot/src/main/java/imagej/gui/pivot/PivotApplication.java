//
// PivotApplication.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.gui.pivot;

import imagej.Log;
import imagej.plugin.gui.pivot.PivotMenuCreator;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.content.NumericSpinnerData;

public class PivotApplication implements Application {

	private Frame frame = null;

	@Override
	public void startup(Display display, Map<String, String> properties) {

		frame = new Frame(display);

		final BoxPane pane = new BoxPane();
		pane.setOrientation(Orientation.VERTICAL);

		final BoxPane menus = new BoxPane();
		pane.add(menus);

		final MenuButton fileButton = new MenuButton();
		fileButton.setButtonData("File");
		final Menu fileMenu = new Menu();
		fileButton.setMenu(fileMenu);
		menus.add(fileButton);

		final Menu.Section fileSection = new Menu.Section();
		fileMenu.getSections().add(fileSection);

		final Menu.Item fileNewItem = new Menu.Item("New");
		fileSection.add(fileNewItem);

		final Menu.Item fileOpenItem = new Menu.Item("Open");
		fileSection.add(fileOpenItem);

		final Label label = new Label();
		label.setText("Hello World!");
		label.getStyles().put("font", new Font("Arial", Font.BOLD, 24));
		label.getStyles().put("color", Color.RED);
		label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
		label.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
		pane.add(label);

		final PushButton pushButton = new PushButton();
		pushButton.setButtonData("Push Button");
		pane.add(pushButton);

		final TextInput textInput = new TextInput();
		textInput.setText("TextInput");
		pane.add(textInput);

		final Checkbox checkbox = new Checkbox();
		checkbox.setSelected(true);
		pane.add(checkbox);

		final BoxPane sliderPane = new BoxPane();
		sliderPane.setOrientation(Orientation.HORIZONTAL);
		final Slider slider = new Slider();
		slider.setValue(18);
		slider.setRange(5, 23);
		sliderPane.add(slider);
		final Label sliderLabel = new Label();
		sliderLabel.setText("18");
		sliderPane.add(sliderLabel);
		slider.getSliderValueListeners().add(new SliderValueListener() {
			@Override
			public void valueChanged(Slider s, int previousValue) {
        sliderLabel.setText("" + s.getValue());
      }
		});
		pane.add(sliderPane);

		final Spinner spinner = new Spinner();
		spinner.setPreferredWidth(50);
		spinner.setSpinnerData(new NumericSpinnerData(5, 23, 2));
		pane.add(spinner);

		final ListButton comboBox = new ListButton();
		final List<String> listData = new ArrayList<String>();
		listData.add("Quick");
		listData.add("Brown");
		listData.add("Fox");
		comboBox.setListData(listData);
		comboBox.setSelectedIndex(0);
		pane.add(comboBox);

		frame.setContent(pane);
		frame.setTitle("Hello World!");
		frame.setMaximized(true);

		frame.open(display);
	}

	@Override
	public boolean shutdown(boolean optional) {
		if (frame != null) {
			frame.close();
		}

		return false;
	}

	@Override
	public void suspend() {}

	@Override
	public void resume() {}

}
