package com.apple.eawt;

/* This is a placeholder class for compiling only */

public interface ApplicationListener {
        public void handleAbout(ApplicationEvent event);
	public void handleOpenFile(ApplicationEvent event);
	public void handlePreferences(ApplicationEvent event);
	public void handleQuit(ApplicationEvent event);
 	public void handleOpenApplication(ApplicationEvent event);
	public void handleReOpenApplication(ApplicationEvent event);
	public void handlePrintFile(ApplicationEvent event);
}
