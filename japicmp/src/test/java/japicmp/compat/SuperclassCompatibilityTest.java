package japicmp.compat;

import japicmp.cmp.ClassesHelper;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.*;
import japicmp.util.*;
import javassist.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static japicmp.util.Helper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class SuperclassCompatibilityTest {

	@Test
	void testSuperclassRemoved() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass ctClass = CtInterfaceBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		assertThat(jApiClass.getSuperclass().getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.SUPERCLASS_REMOVED)));
		assertThat(jApiClass.isBinaryCompatible(), is(false));
	}

	@Test
	void testSuperclassChanged() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass2").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		assertThat(jApiClass.getSuperclass().getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.SUPERCLASS_REMOVED)));
		assertThat(jApiClass.isBinaryCompatible(), is(false));
	}

	@Test
	void testSuperclassAdded() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		assertThat(jApiClass.getSuperclass().getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.SUPERCLASS_ADDED)));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		JApiSuperclass superclass = jApiClass.getSuperclass();
		Assertions.assertEquals("japicmp.Test", superclass.getJApiClassOwning().getFullyQualifiedName());
		Assertions.assertEquals("japicmp.Superclass", superclass.getCorrespondingJApiClass().get().getFullyQualifiedName());
		assertThat(superclass.isBinaryCompatible(), is(true));
		assertThat(superclass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.SUPERCLASS_ADDED)));
	}

	@Test
	void testSuperclassUnchangedObject() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").addToClassPool(classPool);
				return Collections.singletonList(ctClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiClass.getCompatibilityChanges().size(), is(0));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		JApiSuperclass superclass = jApiClass.getSuperclass();
		Assertions.assertEquals("japicmp.Test", superclass.getJApiClassOwning().getFullyQualifiedName());
		Assertions.assertEquals("java.lang.Object", superclass.getCorrespondingJApiClass().get().getFullyQualifiedName());
		assertThat(superclass.isBinaryCompatible(), is(true));
		assertThat(superclass.getCompatibilityChanges().size(), is(0));
	}

	@Test
	void testMethodRemovedInSuperclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().returnType(superclass).name("getInstance").addToClass(superclass);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS)));
		assertThat(jApiClass.isBinaryCompatible(), is(false));
	}

	@Test
	void testMethodRemovedInSuperclassButOverriddenInSubclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().returnType(superclass).name("getInstance").addToClass(superclass);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().returnType(superclass).name("getInstance").addToClass(ctClass);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().returnType(superclass).name("getInstance").addToClass(ctClass);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(true));
	}

	@Test
	void testFieldRemovedInSuperclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtFieldBuilder.create().type(CtClass.intType).name("field").addToClass(superclass);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiClass.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.FIELD_REMOVED_IN_SUPERCLASS)));
		assertThat(jApiClass.isBinaryCompatible(), is(false));
	}

	@Test
	void testFieldRemovedInSuperclassButOverriddenInSubclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtFieldBuilder.create().type(CtClass.intType).name("field").addToClass(superclass);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				CtFieldBuilder.create().type(CtClass.intType).name("field").addToClass(ctClass);
				return Arrays.asList(ctClass, superclass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass superclass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(superclass).addToClassPool(classPool);
				CtFieldBuilder.create().type(CtClass.intType).name("field").addToClass(ctClass);
				return Arrays.asList(ctClass, superclass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(true));
	}

	@Test
	void testAbstractClassNowExtendsAnotherAbstractClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setIncludeSynthetic(true);
		options.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass superClass = CtClassBuilder.create().abstractModifier().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass ctClass = CtClassBuilder.create().abstractModifier().name("japicmp.Test").addToClassPool(classPool);
				return Arrays.asList(ctClass, superClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass superClass = CtClassBuilder.create().abstractModifier().name("japicmp.Superclass").addToClassPool(classPool);
				CtMethodBuilder.create().returnType(CtClass.intType).abstractMethod().name("newAbstractMethod").addToClass(superClass);
				CtClass ctClass = CtClassBuilder.create().abstractModifier().name("japicmp.Test").withSuperclass(superClass).addToClassPool(classPool);
				return Arrays.asList(ctClass, superClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(false));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Superclass");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "newAbstractMethod");
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_ABSTRACT_ADDED_TO_CLASS)));
		assertThat(jApiMethod.isBinaryCompatible(), is(true));
		assertThat(jApiMethod.isSourceCompatible(), is(false));
	}

	@Test
	void testPrivateFieldAndMethodRemovedInSuperclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aSuperClass = CtClassBuilder.create().finalModifier().name("japicmp.Superclass").addToClassPool(classPool);
				CtFieldBuilder.create().privateAccess().name("field").addToClass(aSuperClass);
				CtMethodBuilder.create().privateAccess().name("method").addToClass(aSuperClass);
				CtClass aClass = CtClassBuilder.create().finalModifier().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aSuperClass = CtClassBuilder.create().finalModifier().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().finalModifier().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Superclass");
		JApiField jApiField = getJApiField(jApiClass.getFields(), "field");
		assertThat(jApiField.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasSize(0));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(true));
	}

	@Test
	void testPackagePrivateMethodRemovedInSuperclass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setAccessModifier(AccessModifier.PROTECTED);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aSuperClass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtMethodBuilder.create().packageProtectedAccess().name("method").addToClass(aSuperClass);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aSuperClass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Superclass");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasSize(0));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(true));
	}

	@Test
	void testPackagePrivateSyntheticMethodRemovedInSuperclassIgnoreSynthetic() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		options.setAccessModifier(AccessModifier.PRIVATE);
		options.setIncludeSynthetic(false);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aSuperClass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtMethodBuilder.create().packageProtectedAccess().syntheticModifier().name("method").addToClass(aSuperClass);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass aSuperClass = CtClassBuilder.create().name("japicmp.Superclass").addToClassPool(classPool);
				CtClass aClass = CtClassBuilder.create().name("japicmp.Test").withSuperclass(aSuperClass).addToClassPool(classPool);
				return Arrays.asList(aSuperClass, aClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.Superclass");
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Test");
		assertThat(jApiClass.getCompatibilityChanges(), hasSize(0));
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.isSourceCompatible(), is(true));
	}
}
