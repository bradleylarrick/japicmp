package japicmp.test.annotation;

public class AnnotationRemovedFromConstructor {

	@TestAnnotation(name = "ctor-annotation", list = {"x"}, type = @TestAnnotation.Type(label = "ctor-label"))
	public AnnotationRemovedFromConstructor(String arg) {
	}
}
