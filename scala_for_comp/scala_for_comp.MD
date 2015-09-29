# For Comprehenions: More than Java foreach
## Sources
I put together my spin on this topic based on many excellent resources, including:

* “11.2. Creating a Mutable List” From: Alvin Alexander. “Scala Cookbook” 
* "Scala for Comprehensions" From: Dean Wampler and Alex Payne. “Programming Scala”
* "Differences between map and flatMap - converting String to Int" From: Alvin Alexander. “Scala Cookbook” 

## Basic Use Cases
The usage which all Java, C++ and C# programers can relate to is the one that is most like a `foreach`:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    for (breed <- dogBreeds)
        println(breed)
```

For this 'foreach' construct there is no data returned, it is simply printing out a list of dog breeds.  You could use it to collect data into a list by using a list in scope like this:

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds)
        bestOfBest += breed

    println(bestOfBest)
```  

### Guards: Filters on the Stream of Values
The Scala `for comprehension` provides conditonal filtering using a `guard expression` which is used to determine if the current value from the stream is fit for use by the rest of the comprehension.

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds
        if breed.contains("Pug"))
        bestOfBest += breed

    println(bestOfBest)
```  

Now let's see what happens if we try to assign the boolean expression using `<-` and then use it in a conditional statement.  Create an "assignment" of the conditional and use it in the body of the comprehension, as we try below.

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds;
         bestie <- breed.contains("Pug"))
          if (bestie) bestOfBest += breed

    println(bestOfBest)
```

What happens?

```bash
<console>:10: error: value foreach is not a member of Boolean
              bestie <- breed.contains("Pug")
                                    ^
```

As you can see, we cannot simply use `<-` to assign values because it is looking for a foreach method on the type.  So if you need to conditonally filter data using a `for comprehension`, then you use the idiomatic guard expression `if (<condition>)` syntax.

However, if you choose not use a guard expression, you can actually use an assignment and then test for conditions in the body of the comprehension like this:

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds;
         bestie = breed.contains("Pug"))
          if (bestie) bestOfBest += breed

    println(bestOfBest)
```
So far we have been using side effects on a ListBuffer to collect data as we iterate over a list. As expected, there is something better supported by Scala for doing that.

### Yield
The body of the comprehension can use the `yield` keyword to return values into a sequence:

```scala
    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val bestOfBest: List[String] = for (breed <- dogBreeds
                                        if breed.contains("Pug"))
                                            yield breed

    println(bestOfBest)
```  

The type of the Collection output is determined by the first collection being iterated. These simple, inline examples demonstrate this property:

```scala
    for (optionalPet <- List[Option[String]](Some("Cat")); petType <- optionalPet) yield petType

    for (optionalPet <- Array[Option[String]](Some("Cat")); petType <- optionalPet) yield petType

    for (optionalPet <- Set[Option[String]](Some("Cat")); petType <- optionalPet) yield petType
```  

### Conventions 

“An informal convention is to use parentheses when the for comprehension has a single expression and curly braces when multiple expressions are used. Note that older versions of Scala required semicolons between expressions when parentheses were used.”

Excerpt From: Dean Wampler and Alex Payne. “Programming Scala”

This convention will now be used in the following examples.  

### Expanded Scope and Value Definitions

The comprehension can support variable assignments based on computations.  These variables can then be used in the rest of the comprehension.  For example if we want to add a little intensity to our list of names with punctuation:

```scala
    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val awesomeDogs: List[String] = for {breed <- dogBreeds
                                            if breed.contains("Pug")
                                            awesome = s"$breed!"    
                                            }
                                            yield awesome

    println(awesomeDogs)
```  

### Traversing Option using For Comprehension

Before we close up this discussion of the basics of `for comprehensions` in Scala, I'd like to show where we are going in the next section.  The point behind the comprehension is to regularize and simplify a pattern of interactions with collections.   Basically to be a Facade of sorts to the various traversals and filterings of collections, even simple one-element collections like the `Option`.  Java  programmers should take note, because the creators of Java 8 took some of these same ideas into Java.

For our simple case assume you have a function that hands back dog breed names as options based on an initial letter for the name and you want to process the result:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    def findStarter(start: String): Option[String] = dogBreeds.collectFirst({case breed if (breed.startsWith(start)) => breed})

    def printResult(result: Option[String]) = for {breedName <- result} println(breedName)

    printResult(findStarter("P"))

    printResult(findStarter("X"))

```

The expression `breedName <- result` allows the comprehension to bind the value in the Option (a dog breed name) to the variable `breedName`.  What if there is no value or, more properly in Option-speak, what if the value is None?  Normally, if you try to access the value of None you'd get an exception of type `NoSuchElementException`, except the `for comprehension`  deals with this exception and simply avoids processing the None element.

Because of this simplifying behavior of the comprehension, we can basically process one or an infinite number of Options.

It is possible use Scala pattern matching to make this processing explicit when processing lists:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    def findStarter(start: String): List[Option[String]] = dogBreeds.map(breed => if (breed.startsWith(start)) Some(breed) else None)

    def printResult(results: List[Option[String]]) = for {Some(breedName) <- results} println(breedName)

    printResult(findStarter("P"))

    printResult(findStarter("X"))

```

So by using a List[Option[String]] we can use Scala's Pattern Matching against the entries to "deconstruct" the Option subclass Some(value).  So with a simple `for {Some(breedName) <- results}` we can iterate over our List, process only the good stuff and extract the values in the object of concern.  To do this without the comprehension it would be something like this:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    def findStarter(start: String): List[Option[String]] = dogBreeds.map(breed => if (breed.startsWith(start)) Some(breed) else None)

    def printResult(results: List[Option[String]]) = results.foreach({case Some(breedName) => println(breedName)
                                                                        case _ => })

    printResult(findStarter("P"))

    printResult(findStarter("X"))

```

Personally, the comprehension version is cleaner to read but it is idiomatic. In the case where you are using what is called a `case class`, the magic is even more powerful, concise and just plain useful.

### What you saw but may have missed
I also missed it, but as I fininshed writing up this peice to talk about the `for comprehension` I learned about something else it can do which is very powerful.  The additional feature of which I speak is the use of pattern matching to extract parts of data in both the generator expression  (`<-`) and the assignment expressions (`=`) supported in the comprehension construct.  A set of one-liner examples goes as follows:

```scala
// 1
    for (Some(optionalPetOption) <- List[Option[Option[String]]](Some(Some("Cat")), None); Some(petType) = optionalPetOption) yield petType
// 2
    for (Some(optionalPetOption) <- List[Option[Option[String]]](Some(Some("Cat")), None, Some(None)); petType <- optionalPetOption) yield petType
// 3
    for (Some(optionalPetOption) <- List[Option[Option[String]]](Some(Some("Cat")), None, Some(None)); Some(petType) = optionalPetOption) yield petType
// 4
    for (Some(optionalPetOption) <- List[Option[Option[String]]](Some(Some("Cat")), None, Some(None)); petType = optionalPetOption match {case Some(x) => x; case None => }) yield petType
```

In the code above we use `Some(optionalPetOption)` to unwrap the inner Option from the generator and `Some(petType)` to unwrap the value in that Option.  This allows `petType` to be bound to the value `"Cat"`.  The second example supports the case where the value is `Some(None)`.  This is because we are allowing the non-specific generator `petType <- optionalPetOption` to do the default behavior and ignore None.  Now if we if try to get specific using an assignment, as in the third case, the `Some(None)` case reults in errors:

```bash
scala.MatchError: None (of class scala.None$)
  at $anonfun$2.apply(<console>:9)
  at $anonfun$2.apply(<console>:9)
  at scala.collection.TraversableLike$WithFilter$$anonfun$map$2.apply(TraversableLike.scala:728)
  at scala.collection.immutable.List.foreach(List.scala:381)
  at scala.collection.TraversableLike$WithFilter.map(TraversableLike.scala:727)
  ... 33 elided

```

With the fourth case we end up with something that "works" but instead of producing only a list of pet types Strings for all list elements that are not `None`, it produces this:

```bash
res26: List[Any] = List(Cat, ())

```
The `()` is Scala's way of saying `void` a.k.a `Unit`.  Anyway, this can be taken in various directions and the power of declarative, Functional Programming can be used to express your intentions... or not.

## Desugaring For Comprehensions: foreach, map, flatMap, and withFilter
So with the basics in hand we can move on to seeing how the syntax of a `comprehension` maps to methods directly available in Scala on the family of `Traversable` Collections and on implicit collections like Option.

Let's demonstrate each case and show the comprehension against the native format:

### foreach
While we have already seen foreach in action above, I will show it here for completeness:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    for (s <- dogBreeds) println(s)

    dogBreeds foreach println
```

It is intersting to note here that pure scala method is pretty concise and may be a bit more clear, as we saw above there are cases where this is not the situation.

### map
The next operation to show is a map:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    for (s <- dogBreeds) yield s.reverse

    dogBreeds map (_.reverse)
```

### flatMap
The process of flat mapping is a way to map entries and break them out of their nest sequences.  Let's change it up a bit to demonstrate this.  For this case let's show what happens when we create a nested mapping.  In this case we will take the square root of list of numbers and use None when NaN is created or Some() otherwise:

```scala
    val numbers = List(0.0, 4.0, 9.0, -1.0, -2.0, -9.0)

    implicit def convert(l: List[Double]): List[Option[Double]] = l map { x => val sqrt = Math.sqrt(x); if (!sqrt.isNaN) Some(sqrt) else None }

    for {rootOpt <- numbers :List[Option[Double]]
         root <- rootOpt } yield root

    (numbers:List[Option[Double]]).flatMap(x => x)

```

I would say perhaps the flatMap version is less lines of code, but I would also suggest that having to say `x => x` or using an identity function `x => x` isn't as direct as saying `yield root` though that may be a matter of taste.  The point being that the `for comprehension` supports unwinding nested list constructs in an idiomatic way.

### withFilter - on a map process
We can actually solve the same problem above without resorting to leaning on implicit conversions.  In cases where we have the luxury of processing the List elements in our loop we can use conditionals to protect our code from yielding NaNs:

```scala
    val numbers = List(0.0, 4.0, 9.0, -1.0, -2.0, -9.0)

    for {x <- numbers
         if (x >= 0.0)} yield Math.sqrt(x)

   numbers withFilter (_ >= 0.0) map Math.sqrt

```

Once again the `comprehension` approach offers a way to leverage map based processing, in this case it is using `withFilter`.  Perhaps you are wondering if there is a native mechanism to conditionaly support `break` or `continue`. No. Not yet.  However if you read [this post about implementing it in Scala](http://erikerlandson.github.io/blog/2015/01/24/monadic-break-and-continue-for-scala-sequence-comprehensions/), you'll see that there is a Functional Programming way to do this.  Looking at the non-comprehension approach we see nothing very new, just standard Scala. 

## Conclusion
I actually kept that last section down to the basics.  You'll notice I used no patterns on the left side of any assignments or generators - if I did the non-comprehension code would become a little more complex.  Understanding the implications of tbis is best dealt with by reading through  *“Translation Rules of for Comprehensions”, Dean Wampler and Alex Payne, “Programming Scala”* or [the Language Specification: 6.19 For Comprehensions and For Loop](http://www.scala-lang.org/docu/files/ScalaReference.pdf).  I will show one example here, let's return to one of our examples from above that uses pattern matching to get a value from an Option:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    def findStarter(start: String): List[Option[String]] = dogBreeds.map(breed => if (breed.startsWith(start)) Some(breed) else None)

    def printResult(results: List[Option[String]]) = for {Some(breedName) <- results} println(breedName)

    printResult(findStarter("P"))

    printResult(findStarter("X"))

```

To represent the same capabiilty in *normal* Scala you would need to do something like this:

```scala
    // Assume the setup is the same as above here.

    def printResultWithoutFor(results: List[Option[String]]) = results foreach {case Some(breedName) => println(breedName); case _ =>}

    printResultWithoutFor(findStarter("P"))

```

You have seen something like this before but now you see it understanding that the `for comprehension` is in fact a syntactic sugar on top of normal Scala constructs.  