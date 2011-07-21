/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
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

package imagej.ui;

/**
 *
 * @author GBH
 */
public interface DialogPrompt {

	/*
	 * Constructor: 
	 * DialogPrompt dialogPrompt(String message, String title, 
			DialogPrompt.MessageType msg, DialogPrompt.OptionType option);
	 * 
	 * Use: 
	 * final UserInterface ui = ImageJ.get(UIService.class).getUI();
	 * DialogPrompt dialog = ui.dialogPrompt("Message", "Title", 
	 *     DialogPrompt.MessageType.QUESTION_MESSAGE, DialogPrompt.OptionType.YES_NO_OPTION);
	 * DialogPrompt.Result result = dialog.prompt();
	 * if(result == DialogPrompt.Result.YES_OPTION) {
	 *	// do something
	 * }
	 * 
	 */
		
	Result prompt();
	
	/*
	 * Types copied from Swing JOptionPane
	 */
	enum MessageType {

		ERROR_MESSAGE,
		INFORMATION_MESSAGE,
		WARNING_MESSAGE,
		QUESTION_MESSAGE,
		PLAIN_MESSAGE
	}

	enum OptionType {

		DEFAULT_OPTION,
		YES_NO_OPTION,
		YES_NO_CANCEL_OPTION,
		OK_CANCEL_OPTION
	}

	enum Result {

		YES_OPTION,
		NO_OPTION,
		CANCEL_OPTION,
		OK_OPTION,
		CLOSED_OPTION
	}
	
}
