package japicmp.util;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;

import java.util.HashMap;
import java.util.Map;

public class CtConstructorBuilder extends CtBehaviorBuilder {
	private String body = "System.out.println(\"a\");";
	private final Map<String, CtElement[]> annotations = new HashMap<>();

	public static CtConstructorBuilder create() {
		return new CtConstructorBuilder();
	}

	public CtConstructorBuilder modifier(int modifier) {
		this.modifier = modifier;
		return this;
	}

	public CtConstructorBuilder parameters(CtClass[] parameters) {
		return (CtConstructorBuilder) super.parameters(parameters);
	}

	public CtConstructorBuilder parameter(CtClass parameter) {
		return (CtConstructorBuilder) super.parameter(parameter);
	}

	public CtConstructorBuilder exceptions(CtClass[] exceptions) {
		return (CtConstructorBuilder) super.exceptions(exceptions);
	}

	public CtConstructorBuilder body(String body) {
		this.body = body;
		return this;
	}

	public CtConstructorBuilder publicAccess() {
		return (CtConstructorBuilder) super.publicAccess();
	}

	public CtConstructorBuilder protectedAccess() {
		return (CtConstructorBuilder) super.protectedAccess();
	}

	public CtConstructorBuilder privateAccess() {
		return (CtConstructorBuilder) super.privateAccess();
	}

	public CtConstructorBuilder signature(String signature) {
		super.signature(signature);
		return this;
	}

	public CtConstructorBuilder withAnnotation(String annotation, CtElement... elements) {
		this.annotations.put(annotation, elements);
		return this;
	}

	public CtConstructor addToClass(CtClass declaringClass) throws CannotCompileException {
		CtConstructor ctConstructor = CtNewConstructor.make(this.parameters, this.exceptions, this.body, declaringClass);
		ctConstructor.setModifiers(this.modifier);
		declaringClass.addConstructor(ctConstructor);
		for (String annotation : annotations.keySet()) {
			ConstPool constPool = declaringClass.getClassFile().getConstPool();
			AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
			Annotation annot = new Annotation(annotation, constPool);
			for (CtElement element : annotations.get(annotation)) {
				annot.addMemberValue(element.name, element.value.apply(constPool));
			}
			attr.setAnnotation(annot);
			ctConstructor.getMethodInfo().addAttribute(attr);
		}
		if (signature != null) {
			ctConstructor.getMethodInfo().addAttribute(new SignatureAttribute(declaringClass.getClassFile().getConstPool(), signature));
		}
		return ctConstructor;
	}
}
