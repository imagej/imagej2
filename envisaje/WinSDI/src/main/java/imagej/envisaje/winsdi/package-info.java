package imagej.envisaje.winsdi;

/*
If you need to replace your Windowmanager with a custom one, follow these simple steps:


 1. Create a Module withDependencies on WindowSystem, Nodes, Lookup and Utilities APIs.

 2. Create a new implementation of WindowManager, or as a first test and starting point simply
   copy the class DummyWindowManager from the NetBeans Sources.

 2. Register the Dummymanager as the new implementation:
      @ServiceProvider(service = WindowManager.class,
          supersedes = “org.netbeans.core.windows.WindowManagerImpl”)

 3. Add an entry to your modules manifest:
      OpenIDE-Module-Provides: org.openide.windows.WindowManager

 4. Remove “Core Windows” from your NetBeans Platform Project (Properties -> Libraries -> platform)


 Run your application. To open windows you might have to use a moduleinstall using.

 WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
    @Override
    public void run() {
        TopComponent tc = new CompositeTopComponent();
        tc.open();
        tc.requestActive();
    }
  });
 *
 For Maven...
 <dependency>
    <groupId>org.netbeans.cluster</groupId>
    <artifactId>platform</artifactId>
    <version>${netbeans.version}</version>
    <type>pom</type>
    <exclusions>
        <exclusion>
            <artifactId>org-netbeans-core-windows</artifactId>
            <groupId>org.netbeans.modules</groupId>
        </exclusion>
    </exclusions>
</dependency> 
 */