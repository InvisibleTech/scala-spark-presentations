# Overview of Java Generics - Limitations
## Preface
These are notes are based on my reading of Maurice Naftalin and Philip Wadler. “Java Generics and Collections.” While most of these notes in this repo are about my explorations in Scala, I wanted to make sure I really understood Java Generics because as I have used Scala at work, I have found that Java's handling of Generics has lent some *excitement* to the process.  There are cases where, when calling Spark code from Java I simply haven't found an acceptable way to eliminate all compiler warnings.
### Code Samples
All or almost all code samples are taken from “Java Generics and Collections.”.

## Subtyping and Wildcards
### Substitution Principle
A type that is a subtype of another is related by `extends` or `implements` and there are many examples in Java:
* Integer extends Number
* ArrayList<E> implements List<E>
* List<E> extends Collection<E>

“Substitution Principle: a variable of a given type may be assigned a value of any subtype of that type, and a method with a parameter of a given type may be invoked with an argument of any subtype of that type.”

Excerpt From: Maurice Naftalin and Philip Wadler. “Java Generics and Collections.”

```java
List<Number> nums = new ArrayList<Number>();
nums.add(2);

nums.add(3.14)
```
As you can see above we have taken advantage of the second part of the substitution principle. However, there is something that doesn't apply here.  In our first example we see `Integer` is a subtype of `Number` as is `Double` and so we can add both the `Number` based collection. 

Does this also mean that `List<Integer>` is a subclass of `List<Number>`?  No, it doesn't the relationship `List<Integer>` to `Collection<Integer>` is valid. However, the 

```java
@Test
public void should_NOT_Compile_GIVEN_UnSupportedGenericUpCast() throws Exception {
    List<Integer> ints = new ArrayList<Integer>();
    ints.add(1);
    ints.add(2);
    //List<Number> nums = ints;  // compile-time error
    //nums.add(3.14);  // The Substitution Principle would allow this and this
    // is dangerous because now a true List<Integer> contains a Double, if this
    // was allowed and we retrieved that value via `ints` we would have get an
    // error getting it back.  So to avoid this trouble we cannot simply upcast
    // List<Integer> as List<Number>.

    // Should fail.
    assertEquals(Arrays.asList(1, 2, 3.4), ints);
}
```

The Substitution Principle is not in effect for the case above because the fourth line won't compile.  In this case `List<Integer>` is not a subclass of `List<Number>`.  Not *covariant*.

```java
@Test
public void should_NOT_Compile_() throws Exception {
    List<Number> nums = new ArrayList<Number>();
    nums.add(2.78);
    nums.add(3.14);
    // Compiler error - cannot convert from List<Number> to List<Integer>
    // List<Integer> ints = nums;  // compile-time error
    // assert ints.toString().equals("[2.78, 3.14]");  // 
}
```
Once again the Substitution Principle is not in effect for this case because `List<Number>` is not a subtype of `List<Integer>`.  Not *contravariant*.

### What About Arrays?
Well, for arrays there is a default *covariance*.  This means that `Integer[]` is a subtype of `Number[]`.  How can we get *variance* in a List?  There is a way with some exceptions.  This is the reason for wildcards in types.

### Generic Wildcards
#### Extends
To implement *covariance* we must use the following form:

```java
interface Collection<E> {
    public boolean addAll(Collection< ? extends E> c);
}
```
The above example declares a method which can take a Collection containing a subtype of `E`, including `E` itself.  In case it is not clear, `?` is the wildcard character.  Here we see that the use of `? extends Number` allows us to mix `Integer` and `Double` in one collection:

```java
ArrayList<Number> nums = new ArrayList<Number>();

List<Integer> ints = Arrays.asList(1, 2, 3);
List<Double> doubles = Arrays.asList(1.4, 3.175, 2.3);

nums.addAll(ints);  // addAll is covariant 
nums.addAll(doubles);

assertEquals(Arrays.<Number>asList(1, 2, 3, 1.4, 3.175, 2.3), nums);

```
Here we see `covariance` means that `List<Integer>` is a **subtype** of `List<Number>`.  Which is expressed as `< ? extends Number >`.

However, this use of `covariance` has the strange side effect of making a collection using methods that accept instances of the type `< ? extends T >` not compile when we actually try to invoke the method with a type extending `T`:

```java
List<Integer> ints = new ArrayList<Integer>();
ints.add(1);
ints.add(2);
List< ? extends Number> nums = ints;
//    nums.add(3.14);  // compile-time error 
//    <-- here we see input type of ? extends Number is not allowed.
assert ints.toString().equals("[1, 2, 3.14]");  // uh oh!
```
Basically, you are allowed to **output** a `covariant` type value (e.g. `< ? extends Number >`) but not accept one.  This is different from the case where we input a `covariant` type of the containing class.  This is why `addAll(Collection< ? extends E> c)` works for our first case.  We'll talk about why in a bit.

#### Super
To implement *contravariance* we use `< ? super T>` as in the following method:

```java
public static <T> void copy(List< ? super T > dst, List< ? extends T > src)...
```
Basically, this means that `List<Number>` is a **subtype** of `List<Integer>`.  Read that carefully, we are saying a collection of the supertype is a substype of the collection of the subtype.  That of course is the meaning of **contra** in **contravariant**.

So, let's look at sample invocations of this method:

```java
// 1
public static <T> void copy(List<T> dst, List<T> src)
// 2
public static <T> void copy(List<T> dst, List< ? extends T> src)
// 3
public static <T> void copy(List< ? super T> dst, List<T> src)
// 4
public static <T> void copy(List< ? super T> dst, List< ? extends T> src)
```
1. This is too strict as both arguments must be the same type.
2. Only works when the type is `Object` - only this type supports.
3. Only works when the type is `Integer`.
4. Works for: `Object`, `Number`, and `Integer`.

Looking at case 4 more closely you can see that type inferencing always goes down to the most specific type possible:

```java
List<Integer> ints = Arrays.asList(1, 2, 3);
List<Number> nums = new ArrayList<Number>();
List<Object> objs = new ArrayList<Object>();

Collections.copy(objs, ints);
```
### The Principle At Work

“The Get and Put Principle: use an extends wildcard when you only get values out of a structure, use a super wildcard when you only put values into a structure, and don’t use a wildcard when you both get and put.”

Excerpt From: Maurice Naftalin and Philip Wadler. “Java Generics and Collections.”

So what this means is that whenever you both put values into and get values out of a structure you must use `invariant types`  this means you cannot use `extends` or `super`.  

Consider two methods `public static double sum(Collection< ? extends Number> nums)`  and `public static void count(Collection< ? super Integer> ints, int n)` being used in a third method:

```java
public static void printSumOfN(Collection<Number> numbers, int n, double[] output)
```
Assuming this method uses both `sum` and `count` in the same scope to put the sum of N numbers into `output`, we would have to have a type for Number that is **both** `?  extends Number`  and is `? super Number`.  The only type possible here is `Number`.  This means `numbers`  must be the only possible type `Collection<Number>`. 

#### Why this rule? Consider these two cases:
Using `extends Number` means you could send in a Double to something created to contain Integer.  To prevent this, as in:

```java
// Given this class
public static class SomeContainingClass<T> {
    private T value;
    public T give() {
        return value;
    }

    public void take(T in) {
        value = in;
    }
}
```
Also, given these instances and references:

```java
SomeContainingClass<Integer> containing = new SomeContainingClass<Integer>();
SomeContainingClass< ? extends Number> extendsC = containing;
SomeContainingClass< ? super Integer> superC = containing;
```
If we try to use the `covariant` class based on ` ? extends Number`:

```java
        
Number give = extendsC.give();

extendsC.take(1);  // <--- compiler error on input for extends usage.
// The method take(capture#5-of ? extends Number) in the type AppTest.
// SomeContainingClass<capture#5-of ? extends Number> is not applicable for 
// the arguments (int)

extendsC.take(new Integer(1));  // <--- compiler error still.
extendsC.take((Number)new Integer(1));  // <--- compiler error still.

```
As you can see `? extends Number` allows us to treat the containing class based on Integer as a subtype of one based on Number... but only to a point.  No matter what casting tricks we try we end up in the same mess with largely the same error.  Maybe you think declaring the class to be `? extends T`.  That doesn't even compile. 

So having the compiler do this seems like a pain, until you consider what could happen if the compiler allowed this.  Then you could do this:

```java
SomeContainingClass<Integer> containing = new SomeContainingClass<Integer>();
SomeContainingClass< ? extends Number> extendsC = containing;

extendsC.take(new Double(1.0)); // I make sure autoboxing isn't an issue.

Integer value = containing.give(); // What would you expect to happen here
// since the point of generics is to embed casts and such?  Casting Double
// to Integer is an error.  This is a compile-time error.
```
We are protected by the wildcard not matching Double.  Yes, Double extends Number but the compiler wants a real type at this point.

Now, it is the opposite case for `? super T` where we cannot use the `give` method:

```java
Number give2 = superC.give(); // Compiler error. 
// Type mismatch: cannot convert from capture#5-of ? super Integer to 
// Integer

superC.take(1);

```
So in this case the compiler is protecting us from a potential invalid *downcast* of values from some super class of Integer to anything, since `?` is not specific about which class just that in the case of `? super Integer` it is any class (including Integer) of which Integer is a subclass.

#### The Exceptions
Yes, the rules above for Put and Get have exceptions:
* You can always put a `null` to a containing class based on `? extends T` as null is value that extends every type.
* You can always get an `Object` from a containing class based on `? super T` because `Object` is a supertype of all the things.

Another way to remember this is to realize:

* If a type is given as `? extends T` this means the type is bounded as `null <= x <= T`.
* If given as `? super T` the type is bounded `T <= x <= Object`.

What about final classes?  Consider the `String` class which is final and thus has no subtypes.  `List<String>` is a **subtype** of `List< ? extends String >` and they are not the same.  
