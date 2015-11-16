package org.invisibletech.JavaGenerics;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AppTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static class SomeThingInsideMe<T> {
        protected T value;
    }

    public static class SomeContainingClass<T> extends SomeThingInsideMe<T> {
        public T give() {
            return value;
        }

        public void take(T in) {
            value = in;
        }

        public static <T> SomeContainingClass<T> factory() {
            return new SomeContainingClass<T>();
        }

        public SomeContainingClass<T> isThisOneToo(Object obj) {
            // if (obj instanceof SomeContainingClass<T>) { // Error: Cannot
            // perform instanceof check against parameterized type
            // return (SomeContainingClass<T>) obj; // Unchecked cast warning.
            // }
            if (obj instanceof SomeContainingClass<?>) {
                return (SomeContainingClass<T>) obj;
            }

            return null;
        }

        public static <T> List<SomeContainingClass<T>> promote(List<Object> objs) {
            for (Object o : objs)
                if (!(o instanceof SomeContainingClass<?>))
                    throw new ClassCastException();
            return (List<SomeContainingClass<T>>) (List<?>) objs; // unchecked
                                                                  // cast
            // return ( List<SomeContainingClass<T>>)objs; // unchecked cast as
            // long as we change generic Type of class of objs
            // to List<? extends Object> or List<?>

        }

        public static <T> SomeContainingClass<T> toContaining(SomeThingInsideMe<T> thing) {
            if (thing instanceof SomeContainingClass<?>) {
                return (SomeContainingClass<T>) thing;
            }

            return null;
        }
    }

    @Test
    public void should_NOT_Compile_GIVEN_UnSupportedGenericUpCast() throws Exception {
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        List<? extends Number> nums = ints; // compile-time error
        // / nums.add(new Double(3.14)); // error

        // Should fail.
        assertNotEquals(Arrays.asList(1, 2, 3.14), ints);
    }

    @Test
    public void should_NOT_Compile_() throws Exception {
        List<Number> nums = new ArrayList<Number>();
        nums.add(2.78);
        nums.add(3.14);
        // Compiler error - cannot convert from List<Number> to List<Integer>
        // List<Integer> ints = nums; // compile-time error
        // assert ints.toString().equals("[2.78, 3.14]"); //
    }

    @Test
    public void shouldWork_Given_methodUsedIsCovariant() throws Exception {
        ArrayList<Number> nums = new ArrayList<Number>();

        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.4, 3.175, 2.3);

        nums.addAll(ints); // addAll is covariant
        nums.addAll(doubles);

        assertEquals(Arrays.<Number> asList(1, 2, 3, 1.4, 3.175, 2.3), nums);
    }

    @Test
    public void shouldHaveACompilerError_Given_weTryToUseCovariantTypeOnInput() throws Exception {
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        List<? extends Number> nums = ints;
        // nums.add(3.14); // compile-time error <-- here we see input type of ?
        // extends Number is not allowed.
        assert ints.toString().equals("[1, 2, 3.14]"); // uh oh!
    }

    static class SomeClass {
        public static <T> void copy(List<? super T> dst, List<? extends T> src) {
            Collections.copy(dst, src);
        }
    }

    @Test
    public void shouldDemonstrateContravariance() throws Exception {
        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Number> nums = Arrays.asList(null, null, null);
        List<Object> objs = Arrays.asList(null, null, null);

        // SomeClass.copy(ints, objs); // <-- compiler error

        SomeClass.copy(nums, ints);
        SomeClass.copy(objs, ints);
        SomeClass.<Object> copy(objs, ints);
        SomeClass.<Number> copy(nums, ints);
        SomeClass.<Number> copy(objs, ints);
        SomeClass.<Integer> copy(nums, ints);
        SomeClass.<Integer> copy(objs, ints);
    }

    @SuppressWarnings("unused")
    @Test
    public void showTheGetPutRule_Given_GenericClassThatContainsAValue() throws Exception {
        SomeContainingClass<Integer> containing = new SomeContainingClass<Integer>();
        SomeContainingClass<? extends Number> extendsC = containing;
        SomeContainingClass<? super Integer> superC = containing;

        Integer given = containing.give();
        containing.take(1);

        Number give = extendsC.give();

        // extendsC.take(1); // <--- compiler error on input for extends usage.

        // Integer give2 = superC.give(); // <--- compiler error

        superC.take(1);
    }

    // Let's try this with feeling.
    static class Top {

        @Override
        public String toString() {
            return "Top Top Top Top";
        }

    }

    static class Middle extends Top {
        class HiddenMiddle {
            @Override
            public String toString() {
                return super.toString() + " >>>  " + Middle.this.toString() + " >>> [[[[" + Middle.super.toString();
            }

        }

    }

    static class Bottom extends Middle {

    }

    static class OtherBottom extends Middle {

    }

    @SuppressWarnings("unused")
    @Test
    public void showTheGetPutRule_Given_GenericClassThatContainsAValueAndNewTree() throws Exception {
        SomeContainingClass<Bottom> containing = new SomeContainingClass<Bottom>();
        SomeContainingClass<? extends Top> extendsC = containing;
        SomeContainingClass<? super Bottom> superC = containing;

        Bottom given = containing.give();
        containing.take(new Bottom());

        // Bottom give = extendsC.give(); // <--- compiler error - cannot down
        // cast from Top.

        Top giveTop = extendsC.give();

        // extendsC.take(new Bottom()); // <--- compiler error on input for
        // extends usage.

        // Integer give2 = superC.give(); // <--- compiler error

        // superC.take(new Middle()); // <--- compiler error - cannot assign
        // object of type not same as bottom end of bounds.

        superC.take(new Bottom());

        // Magic - the null or Object.
        superC.take(null);
        Object superMagic = superC.give();

        // More magic
        extendsC.take(null);
        Object extendsMagic = extendsC.give();
    }

    @Test
    public void showTheIssuesWithSuperExtends_Given_ClassTreeAndContainer() throws Exception {

        // Let's kick the tires on inheritance here.
        SomeContainingClass<Bottom> bottomContainer = new SomeContainingClass<Bottom>();
        SomeContainingClass<? super Bottom> superOfBottom = bottomContainer;
        SomeContainingClass<? extends Top> extendsOfTop = bottomContainer;

        // These three fail and if you look at what they initially give back -
        // Eclipse suggests Object which is the ,agic upper bound.
        // Bottom giveBottom = superOfBottom.give(); // Type mismatch: cannot
        // convert from capture#14-of ? super AppTest.Bottom to AppTest.Bottom

        // Middle giveMiddle = superOfBottom.give(); // Type mismatch: cannot
        // convert from capture#14-of ? super AppTest.Bottom to AppTest.Middle

        // Top giveTop = superOfBottom.give(); // Type mismatch: cannot convert
        // from capture#14-of ? super AppTest.Bottom to AppTest.Top
        Object giveObject = superOfBottom.give();

        // Now let's add some items
        superOfBottom.take(new Bottom());
        superOfBottom.take(null);

        // Can only take null or Bottom
        // superOfBottom.take(new Middle()); // The method take(capture#16-of ?
        // super AppTest.Bottom) in the
        // type AppTest.SomeContainingClass<capture#16-of ? super
        // AppTest.Bottom>
        // is not applicable for the arguments (AppTest.Middle)ƒ

        // Extends has limitations. Eclipse infers we want to return Top
        // reference.
        // Type mismatch: cannot convert from capture#14-of ? extends
        // AppTest.Top to AppTest.Middle
        Top givenExt = extendsOfTop.give();
        Object givenObjExt = extendsOfTop.give();

        // Middle giveExtMiddle = extendsOfTop.give(); //Type mismatch: cannot
        // convert from capture#14-of ? extends AppTest.Top to AppTest.Middle

        // Bottom givenExtBottom = extendsOfTop.give(); // Type mismatch: cannot
        // convert from capture#15-of ? extends AppTest.Top to AppTest.Bottom

        extendsOfTop.take(null); // Null is magical but costs 1 Billion Dollars.

        // extendsOfTop.take(new Object()); // The method take(capture#18-of ?
        // extends AppTest.Top) in the type
        // AppTest.SomeContainingClass<capture#18-of ?
        // extends AppTest.Top> is not applicable for the arguments (Object)

        // extendsOfTop.take(new Top()); // The method take(capture#16-of ?
        // extends AppTest.Top) in the type
        // AppTest.SomeContainingClass<capture#16-of ?
        // extends AppTest.Top> is not applicable for the arguments
        // (AppTest.Top)

        // extendsOfTop.take(new Middle()); // The method take(capture#16-of ?
        // extends AppTest.Top) in the type
        // AppTest.SomeContainingClass<capture#16-of ?
        // extends AppTest.Top> is not applicable for the arguments
        // (AppTest.Middle)

        // By now you should grasp that ? super Bottom means you receive a
        // returned value of Type Object and take null or Bottom.

        // Also, ? extends Top means you can receive OBject or Bottom and you
        // can give null and only null.
    }

    @Test
    public void shouldThrowAn_ArrayStoreException_When_AbusingCovarianceInArrays() throws Exception {
        Integer[] ints = { 2, 3 };
        Number[] nums = ints; // Arrays are covarant by default.

        expectedException.expect(ArrayStoreException.class);

        nums[0] = 1.555555;
    }

    @Test
    public void shouldHaveCompilerError_Given_InvalidUseOfCaptures() throws Exception {
        List<?> list = Arrays.asList(2, 3, 4);

        List<Set<?>> asetList = new LinkedList<Set<?>>();

        List<?> tmp = new ArrayList(list);
        for (int i = 0; i < list.size(); i++) {
            // list.set(i, tmp.get(list.size()-i-1)); // compile-time error
        }
    }

    @Test
    public void shouldMakeSomeContainingClass_Given_FactoryCalls() throws Exception {
        SomeContainingClass<?> some1 = SomeContainingClass.factory(); // Okay
        SomeContainingClass<?> some2 = SomeContainingClass.<Object> factory(); // Okay
        // SomeContainingClass< ? > some3 = SomeContainingClass.<?>factory(); //
        // Error - Wildcard is not allowed at this location

        SomeContainingClass<?> some4 = SomeContainingClass.<List<?>> factory(); // Okay
        SomeContainingClass<Set<?>> some5 = SomeContainingClass.<Set<?>> factory(); // Okay

        System.out.println(new Middle().new HiddenMiddle().toString());

    }

    @Test
    public void shouldReturnNull_Given_NonSomeContainingClass() throws Exception {
        assertEquals(null, SomeContainingClass.toContaining(new SomeThingInsideMe<String>()));
    }

    @Test
    public void shouldReturnNotNull_Given_NonSomeContainingClass() throws Exception {
        SomeThingInsideMe<Set<?>> thing = new SomeContainingClass<>();
        assertNotEquals(null, SomeContainingClass.toContaining(thing));
    }

    @Test
    public void createArraysUsingReifiableTypes() throws Exception {
        List[] elles = {};
        List<?>[] elles2 = {};
        // ERROR caannot create a generic array.
        // List<Integer>[] elles3 = {};

        // Runtime class cast exception.
        //Object[] oarr = {};
        //String[] strarr = (String[]) oarr;

    }

    public static <T> T[] makeArrayWithWarning(int sizeOf) {
        T[] a = (T[]) new Object[sizeOf]; // unchecked cast
        return a;
    }

    @Test
    public void makeArrayWithWarningShouldWorkButFail() throws Exception {
        expectedException.expect(ClassCastException.class);
        String[] strArr = makeArrayWithWarning(10);
    }
    
    public static <T> T[] makeArrayBasedOnEmptyModel(int sizeOf, T[]proto) {
        proto = (T[]) Array.newInstance(proto.getClass().getComponentType(), sizeOf);
        
        return proto;
    }
    
    @Test
    public void makeArrayWithProtoShouldWork() throws Exception {
        String[] proto = new String[0];
        String[] strArr = makeArrayBasedOnEmptyModel(11, proto);
        
        assertEquals(0, proto.length);
        assertEquals(11, strArr.length);
    }
    
    public static <T> T[] makeArrayForClass(int sizeOf, Class<T> clazz) {
        return (T[]) Array.newInstance(clazz, sizeOf);
    }
    
    @Test
    public void makeArrayWithClassShouldWork() throws Exception {
        String[] strArr = makeArrayForClass(11, String.class);
        
        assertEquals(11, strArr.length);
    }
}
