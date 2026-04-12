package japicmp.compat;

import japicmp.cmp.ClassesHelper;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.*;
import japicmp.util.*;
import javassist.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static japicmp.util.Helper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

class AnnotationCompatibilityTest {

	@Test
	void testAnnotcationDeprecatedAddedToMethod() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").addToClass(aClass);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().withAnnotation("java.lang.Deprecated").name("method").addToClass(aClass);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_DEPRECATED_ADDED)));
		assertThat(jApiMethod.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_ADDED))));
	}

	@Test
	void testAnnotcationDeprecatedAddedToClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("java.lang.Deprecated").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_DEPRECATED_ADDED)));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_ADDED))));
	}

	@Test
	void testAnnotcationDeprecatedRemovedFromClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("java.lang.Deprecated").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_DEPRECATED_ADDED))));
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_REMOVED)));
	}

	@Test
	void testAnnotationAddedToClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("japicmp.MyAnnotation").addToClassPool(classPool);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_ADDED)));
	}

	@Test
	void testAnnotationRemovedFromClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("japicmp.MyAnnotation").addToClassPool(classPool);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_REMOVED)));
	}

	@Test
	void testAnnotationOnClassModified() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtMethodBuilder.create().name("foo").returnType(CtClass.intType).publicAccess().addToClass(anAnnotation);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 1000)).addToClassPool(classPool);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 999)).addToClassPool(classPool);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.MyAnnotation");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "foo");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED)));
		JApiClass jApiClassWithAnnotation = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClassWithAnnotation.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_MODIFIED)));
	}

	@Test
	void testAnnotationAddedToMethod() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").addToClass(aClass);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().withAnnotation("japicmp.MyAnnotation").name("method").addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_ADDED)));
	}

	@Test
	void testAnnotationRemovedFromMethod() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").withAnnotation("japicmp.MyAnnotation").addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").addToClass(aClass);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_REMOVED)));
	}

	@Test
	void testAnnotationOnMethodModified() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtMethodBuilder.create().name("foo").returnType(CtClass.intType).publicAccess().addToClass(anAnnotation);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 1000)).addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().name("method").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 999)).addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_MODIFIED)));
	}

	@Test
	void testAnnotationAddedToField() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field").addToClass(aClass);
				return Collections.singletonList(aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().withAnnotation("japicmp.MyAnnotation").name("field").addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiField jApiField = getJApiField(jApiClass.getFields(), "field");
		assertThat(jApiField.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_ADDED)));
	}

	@Test
	void testAnnotationRemovedFromField() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field").withAnnotation("japicmp.MyAnnotation").addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field").addToClass(aClass);
				return Collections.singletonList(aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiField jApiField = getJApiField(jApiClass.getFields(), "field");
		assertThat(jApiField.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_REMOVED)));
	}

	@Test
	void testAnnotationOnFieldModified() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtMethodBuilder.create().name("foo").returnType(CtClass.intType).publicAccess().addToClass(anAnnotation);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 1000)).addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass anAnnotation = CtAnnotationBuilder.create().name("japicmp.MyAnnotation").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field").withAnnotation("japicmp.MyAnnotation", new CtElement("foo", 999)).addToClass(aClass);
				return Arrays.asList(aClass, anAnnotation);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiField jApiField = getJApiField(jApiClass.getFields(), "field");
		assertThat(jApiField.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.ANNOTATION_MODIFIED)));
	}

	@Test
	void testAnnotationsOnRemovedConstructorAreNotEmpty() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtConstructorBuilder.create().publicAccess()
					.withAnnotation("java.lang.Deprecated")
					.addToClass(ctClass);
				return Collections.singletonList(ctClass);
			}
			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiConstructor removedCtor = jApiClass.getConstructors().stream()
			.filter(c -> c.getChangeStatus() == JApiChangeStatus.REMOVED)
			.findFirst().orElseThrow(() -> new AssertionError("No removed constructor found"));
		assertThat(removedCtor.getAnnotations().isEmpty(), is(false));
		assertThat(removedCtor.getAnnotations().get(0).getFullyQualifiedName(), is("java.lang.Deprecated"));
		assertThat(removedCtor.getAnnotations().get(0).getChangeStatus(), is(JApiChangeStatus.REMOVED));
	}

	@Test
	void testAnnotationsOnRemovedMethodAreNotEmpty() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtMethodBuilder.create().name("method").publicAccess()
					.withAnnotation("java.lang.Deprecated")
					.addToClass(ctClass);
				return Collections.singletonList(ctClass);
			}
			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiMethod removedMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(removedMethod.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		assertThat(removedMethod.getAnnotations().isEmpty(), is(false));
		assertThat(removedMethod.getAnnotations().get(0).getFullyQualifiedName(), is("java.lang.Deprecated"));
		assertThat(removedMethod.getAnnotations().get(0).getChangeStatus(), is(JApiChangeStatus.REMOVED));
	}

	@Test
	void testAnnotationsOnRemovedFieldAreNotEmpty() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				CtFieldBuilder.create().name("field")
					.withAnnotation("java.lang.Deprecated")
					.addToClass(ctClass);
				return Collections.singletonList(ctClass);
			}
			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		JApiField removedField = getJApiField(jApiClass.getFields(), "field");
		assertThat(removedField.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		assertThat(removedField.getAnnotations().isEmpty(), is(false));
		assertThat(removedField.getAnnotations().get(0).getFullyQualifiedName(), is("java.lang.Deprecated"));
		assertThat(removedField.getAnnotations().get(0).getChangeStatus(), is(JApiChangeStatus.REMOVED));
	}
}
