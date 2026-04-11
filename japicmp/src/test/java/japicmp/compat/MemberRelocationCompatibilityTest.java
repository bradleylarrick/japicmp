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

class MemberRelocationCompatibilityTest {

	/**
	 * Tests that no regression of issue #222 occurs
	 */
	@Test
	void testMethodMovedToSuperClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(aClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(bClass);

				return Arrays.asList(aClass, bClass, cClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(aClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(aClass);
				return Arrays.asList(aClass, bClass, cClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.C");
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
	}

	@Test
	void testMethodMovedToNewSuperClassInTheMiddle() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(aClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(bClass);

				return Arrays.asList(aClass, bClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(aClass).addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(cClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(cClass);
				return Arrays.asList(aClass, bClass, cClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.C");
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.B");
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
	}

	@Test
	void testMethodMovedToSuperClassOfSuperClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(aClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(bClass);

				return Arrays.asList(aClass, bClass, cClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bbClass = CtClassBuilder.create().name("japicmp.Bb").withSuperclass(aClass).addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(bbClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtMethodBuilder.create().name("foo").returnType(aClass).publicAccess().addToClass(bbClass);

				return Arrays.asList(aClass, bClass, bbClass, cClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.A");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Bb");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.B");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.C");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS))));
	}

	@Test
	void testFieldMovedToSuperClassOfSuperClass() throws Exception {
		JarArchiveComparatorOptions options = new JarArchiveComparatorOptions();
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(options, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(aClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtFieldBuilder.create().name("testField").type(aClass).addToClass(bClass);

				return Arrays.asList(aClass, bClass, cClass);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass aClass = CtClassBuilder.create().name("japicmp.A").addToClassPool(classPool);
				CtClass bbClass = CtClassBuilder.create().name("japicmp.Bb").withSuperclass(aClass).addToClassPool(classPool);
				CtClass bClass = CtClassBuilder.create().name("japicmp.B").withSuperclass(bbClass).addToClassPool(classPool);
				CtClass cClass = CtClassBuilder.create().name("japicmp.C").withSuperclass(bClass).addToClassPool(classPool);

				CtFieldBuilder.create().name("testField").type(aClass).addToClass(bbClass);

				return Arrays.asList(aClass, bClass, bbClass, cClass);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "japicmp.A");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.FIELD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.Bb");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.FIELD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.B");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.FIELD_REMOVED_IN_SUPERCLASS))));
		jApiClass = getJApiClass(jApiClasses, "japicmp.C");
		assertThat(jApiClass.isBinaryCompatible(), is(true));
		assertThat(jApiClass.getCompatibilityChanges(), not(hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.FIELD_REMOVED_IN_SUPERCLASS))));
	}
}
