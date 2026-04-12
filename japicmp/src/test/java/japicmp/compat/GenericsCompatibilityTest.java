package japicmp.compat;

import japicmp.cmp.ClassesHelper;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.*;
import japicmp.util.*;
import javassist.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static japicmp.util.Helper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class GenericsCompatibilityTest {

	@Test
	void testNewMethodWithGenerics() throws Exception {
		JarArchiveComparatorOptions jarArchiveComparatorOptions = new JarArchiveComparatorOptions();
		jarArchiveComparatorOptions.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(jarArchiveComparatorOptions, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				return Collections.singletonList(ctClassC);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(classPool.get("java.lang.Object"))
					.name("newMethod")
					.parameter(classPool.get("java.lang.Object"))
					.signature("<T:Ljava/lang/Object;>(TT;)TT;")
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "C");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "newMethod");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.NEW));
		assertThat(jApiMethod.isBinaryCompatible(), is(true));
		assertThat(jApiMethod.isSourceCompatible(), is(true));
		assertThat(jApiMethod.getCompatibilityChanges().size(), is(0));
	}

	@Test
	void testRemovedMethodWithGenerics() throws Exception {
		JarArchiveComparatorOptions jarArchiveComparatorOptions = new JarArchiveComparatorOptions();
		jarArchiveComparatorOptions.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(jarArchiveComparatorOptions, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws NotFoundException, CannotCompileException {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(classPool.get("java.lang.Object"))
					.name("oldMethod")
					.parameter(classPool.get("java.lang.Object"))
					.signature("<T:Ljava/lang/Object;>(TT;)TT;")
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				return Collections.singletonList(ctClassC);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "C");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "oldMethod");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.REMOVED));
		assertThat(jApiMethod.isBinaryCompatible(), is(false));
		assertThat(jApiMethod.isSourceCompatible(), is(false));
		assertThat(jApiMethod.getCompatibilityChanges().size(), is(1));
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.METHOD_REMOVED)));
	}

	@Test
	void testMethodWithGenericsChanged() throws Exception {
		JarArchiveComparatorOptions jarArchiveComparatorOptions = new JarArchiveComparatorOptions();
		jarArchiveComparatorOptions.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(jarArchiveComparatorOptions, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws NotFoundException, CannotCompileException {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(classPool.get("java.lang.Object"))
					.name("method")
					.parameter(classPool.get("java.lang.Object"))
					.signature("<T:Ljava/lang/Object;>(TT;)TT;")
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(classPool.get("java.lang.Object"))
					.name("method")
					.parameter(classPool.get("java.lang.Object"))
					.signature("<T:Ljava/lang/String;>(TT;)TT;")
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "C");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		JApiMethod jApiMethod = getJApiMethod(jApiClass.getMethods(), "method");
		assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		assertThat(jApiMethod.isBinaryCompatible(), is(true));
		assertThat(jApiMethod.isSourceCompatible(), is(false));
		assertThat(jApiMethod.getCompatibilityChanges().size(), is(1));
		assertThat(jApiMethod.getCompatibilityChanges(), hasItem(new JApiCompatibilityChange(JApiCompatibilityChangeType.CLASS_GENERIC_TEMPLATE_CHANGED)));
	}

	@Test
	void testNewMethodWithGenericsImplemented() throws Exception {
		JarArchiveComparatorOptions jarArchiveComparatorOptions = new JarArchiveComparatorOptions();
		jarArchiveComparatorOptions.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(jarArchiveComparatorOptions, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				return Collections.singletonList(ctClassC);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass ctInterface = CtInterfaceBuilder.create().name("I").addToClassPool(classPool);
				CtMethodBuilder.create()
					.abstractMethod()
					.returnType(CtClass.voidType)
					.name("newMethod")
					.parameter(classPool.get("java.lang.Object"))
					.signature("<T:Ljava/lang/Object;>(TT;)TT;")
					.addToClass(ctInterface);
				CtClass ctClassC = CtClassBuilder.create().name("C").implementsInterface(ctInterface).addToClassPool(classPool);
				CtMethodBuilder.create().publicAccess().returnType(CtClass.voidType).name("newMethod")
					.parameter(classPool.get("java.lang.String")).addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "C");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.MODIFIED));
		assertThat(jApiClass.getCompatibilityChanges().size(), is(1));
	}

	@Test
	void testMethodWithByteArrayAndArrayOfByteArrays() throws Exception {
		JarArchiveComparatorOptions jarArchiveComparatorOptions = new JarArchiveComparatorOptions();
		jarArchiveComparatorOptions.setAccessModifier(AccessModifier.PRIVATE);
		List<JApiClass> jApiClasses = ClassesHelper.compareClasses(jarArchiveComparatorOptions, new ClassesHelper.ClassesGenerator() {
			@Override
			public List<CtClass> createOldClasses(ClassPool classPool) throws Exception {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(CtClass.voidType)
					.name("method")
					.parameters(new CtClass[]{classPool.get("byte[]")})
					.addToClass(ctClassC);
				CtMethodBuilder.create()
					.returnType(CtClass.voidType)
					.name("method")
					.parameters(new CtClass[]{classPool.get("byte[][]")})
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}

			@Override
			public List<CtClass> createNewClasses(ClassPool classPool) throws Exception {
				CtClass ctClassC = CtClassBuilder.create().name("C").addToClassPool(classPool);
				CtMethodBuilder.create()
					.returnType(CtClass.voidType)
					.name("method")
					.parameters(new CtClass[]{classPool.get("byte[]")})
					.addToClass(ctClassC);
				CtMethodBuilder.create()
					.returnType(CtClass.voidType)
					.name("method")
					.parameters(new CtClass[]{classPool.get("byte[][]")})
					.addToClass(ctClassC);
				return Collections.singletonList(ctClassC);
			}
		});
		JApiClass jApiClass = getJApiClass(jApiClasses, "C");
		assertThat(jApiClass.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
		for (JApiMethod jApiMethod : jApiClass.getMethods()) {
			assertThat(jApiMethod.getChangeStatus(), is(JApiChangeStatus.UNCHANGED));
			assertThat(jApiMethod.isBinaryCompatible(), is(true));
			assertThat(jApiMethod.isSourceCompatible(), is(true));
			assertThat(jApiMethod.getCompatibilityChanges().size(), is(0));
		}
	}
}
