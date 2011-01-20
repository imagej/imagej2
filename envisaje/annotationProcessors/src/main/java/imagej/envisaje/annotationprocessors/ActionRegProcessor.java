
package imagej.envisaje.annotationprocessors;
import imagej.envisaje.annotations.ActionRegistration;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("imagej.envisaje.annotations.ActionRegistration") // NOI18N
public final class ActionRegProcessor extends LayerGeneratingProcessor {
    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionRegistration.class)) {
            ActionRegistration ar = e.getAnnotation(ActionRegistration.class);
            String id = ar.id();
            if (id.length() == 0) {
                id = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString()
                        .replace('.', '-');
            }
            File f = layer(e).file("Actions/" + ar.category() + "/" + id + ".instance");
            f.bundlevalue("displayName", ar.displayName());
            if (ar.key().length() == 0) {
                f.methodvalue("instanceCreate", "imagej.envisaje.annotations.ActionRegistration", "alwaysEnabled");
            } else {
                f.methodvalue("instanceCreate", "imagej.envisaje.annotations.ActionRegistration", "callback");
                f.methodvalue("fallback", "imagej.envisaje.annotations.ActionRegistration", "alwaysEnabled");
                f.stringvalue("key", ar.key());
            }
            try {
                f.instanceAttribute("delegate", ActionListener.class);
            } catch (LayerGenerationException ex) {
                generateContext(e, f);
            }
            f.boolvalue("noIconInMenu", !ar.iconInMenu());
            f.write();
        }
        return true;
    }

    private void generateContext(Element e, File f) throws LayerGenerationException {
        ExecutableElement ee = null;
        for (ExecutableElement element : ElementFilter.constructorsIn(e.getEnclosedElements())) {
            if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                if (ee != null) {
                    throw new LayerGenerationException("Only one public constructor allowed in " + e); // NOI18N
                }
                ee = element;
            }

        }
        if (ee.getParameters().size() != 1) {
            throw new LayerGenerationException("Constructor must have one argument: " + ee);
        }

        VariableElement ve = (VariableElement)ee.getParameters().get(0);
        DeclaredType dt = (DeclaredType)ve.asType();
        String dtName = processingEnv.getElementUtils().getBinaryName((TypeElement)dt.asElement()).toString();
        if ("java.util.Collection".equals(dtName)) {
            f.stringvalue("type", dt.getTypeArguments().get(0).toString());
            f.methodvalue("delegate", "imagej.envisaje.annotations.ActionRegistration", "inject");
            f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString());
            f.stringvalue("selectionType", "ANY");
            f.methodvalue("instanceCreate", "imagej.envisaje.annotations.ActionRegistration", "context");
            return;
        }
        if (!dt.getTypeArguments().isEmpty()) {
            throw new LayerGenerationException("No type parameters allowed in " + ee);
        }

        f.stringvalue("type", ve.asType().toString());
        f.methodvalue("delegate", "imagej.envisaje.annotations.ActionRegistration", "inject");
        f.stringvalue("injectable", processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString());
        f.stringvalue("selectionType", "EXACTLY_ONE");
        f.methodvalue("instanceCreate", "imagej.envisaje.annotations.ActionRegistration", "context");
    }
}