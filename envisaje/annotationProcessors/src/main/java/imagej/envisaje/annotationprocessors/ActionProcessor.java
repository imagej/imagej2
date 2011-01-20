package imagej.envisaje.annotationprocessors;


import imagej.envisaje.annotations.Action;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("imagej.envisaje.annotations.Action")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ActionProcessor extends LayerGeneratingProcessor {
    @Override
    protected boolean handleProcess(
            Set set, RoundEnvironment env)
            throws LayerGenerationException {
        Elements elements = processingEnv.getElementUtils();

        for (Element e : env.getElementsAnnotatedWith(Action.class)) {

            TypeElement clazz = (TypeElement) e;
            Action action = clazz.getAnnotation(Action.class);
            String teName = elements.getBinaryName(clazz).toString();

            File f = layer(e).file(
                    "Actions/" + action.path() + teName.replace('.', '-') + ".instance");
            f.newvalue("delegate", teName);
            f.bundlevalue("displayName", action.displayName());
            f.stringvalue("iconBase", action.iconBase());
            f.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
            f.write();

            if (action.menuBar() == true && action.toolBar() == true) {
                writeDisplayLocation(e, teName, action, "Menu/");
                writeDisplayLocation(e, teName, action, "Toolbars/");
            } else if (action.menuBar() == true && action.toolBar() == false) {
                writeDisplayLocation(e, teName, action, "Menu/");
            } else if (action.menuBar() == false && action.toolBar() == true) {
                writeDisplayLocation(e, teName, action, "Toolbars/");
            }
        }
        return true;
    }

    private void writeDisplayLocation(Element e, String teName, Action action, String loc) {
        File f1 = layer(e).shadowFile(teName, loc + action.path(), teName.replace('.', '-'));
        f1.stringvalue("originalFile", "Actions/" + action.path() + teName.replace('.', '-') + ".instance");
        f1.intvalue("position", action.position());
        f1.write();
    }
}
