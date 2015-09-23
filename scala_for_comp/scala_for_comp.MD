# For Comprehenions: More than Java foreach
## Sources
I put together my spin on this topic based on many excellent resources, including:

* “11.2. Creating a Mutable List” From: Alvin Alexander. “Scala Cookbook” 
* "Scala for Comprehensions" From: Dean Wampler and Alex Payne. “Programming Scala”

## Basic Use Cases
The usage which all Java, C++ and C# programers can relate to is the one that is most like a `foreach`:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    for (breed <- dogBreeds)
        println(breed)
```

Okay so like your 'foreach' construct there is no data returned by this code it is basically causing a side effect by printing out a list of dog breeds.  You could use it to collect data into a list by using a list in scope like this:

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds)
        bestOfBest += breed

    println(bestOfBest)
```  

This of course is something most Java programmers get and do... all the time.  With Java 8 this becomes a lot better via `map` and `filter`.  In fact, we will see later that Scala already had that and we'll also see how Scala `for comprehensions` simplify the syntax.

### The Generator Expression
A brief aside about the `<-` thingy aka the `arrow operator`.  I know that really seems a lot like `:` in the `for (String name : names)` for loop, but that is not actually all it is.  As we'll see later it can mean a lot more.  In fact, it has more in common with the processing supported by Java 8 streams.

Keep in mind generators are not assignment statements.  They imply a contract for obtaining values from the generator which can be bound to the variable.  I'll show this in the next section.

### Guards: Filters on the Stream of Values
So the generator produces a stream/sequence of values (we'll tryt to tighten up the definition later).  The Scala `for comprehension` provides conditonal filtering using a `guard expression` which returns a `Boolean` value (this is Scala and all types are objects, even primitives) which is used to determine if the current value from the stream is fit for use by the rest of the comprehension.

```scala
    import scala.collection.mutable.ListBuffer

    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val  bestOfBest = ListBuffer[String]()

    for (breed <- dogBreeds
        if breed.contains("Pug"))
        bestOfBest += breed

    println(bestOfBest)
```  

Above I stated that I would talk a little bit more about `generators` not being assignments.  Here we have a context to do so.  When I first saw the `guard expression` I thought to myself what is the `if` syntax doing?  Why can't we assign and test the value like in 1990s style Java for loop?  Well as I have stated and will repeat `for comprehensions` are not Java for loops, they are much more.  I'm willing to bet experienced Python programmers are already grokking most of this.  I didn't get it until I started reading about how special they were in *Paul Chiusano and Rúnar Bjarnason's “Functional Programming in Scala.”*  Hence, I felt like this presentation would be useful to even non-Scala programmers because they may encounter Scala in Apache Spark or if they choose to learn Scala, I wanted them to appreciate the power "under the hood" of the "humble" `for comprehension`.

Let's try that shall we?  Create an "assignment" of the conditional and use it in the body of the comprehension like proper Java developers.

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

As you can see, we cannot simply use `<-` to assign values because it is looking for a foreach methd on the type.  So if you need to conditonally filter data using a `for comprehension`, then you need to use the `if (<condition>)` syntax.  So far we have been using side effects on a ListBuffer.  This isn't very clean and proper, not in a Functional Programming model. As expected, there is something supported by Scala for that too.

### Yield
The body of the comprehension can use the `yield` keyword to return values into a list:

```scala
    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val bestOfBest: List[String] = for (breed <- dogBreeds
                                        if breed.contains("Pug"))
                                            yield breed

    println(bestOfBest)
```  

The type of the List is determined by the collection being iterated.  However, before you assume it is the first generator, consider this case where the first generator traverses a list of Strings and the second one traverses the String which is a Sequence of Char.  If we yield each character, we end up with a list of Chars:


```scala
    val  dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    val realCharacters: List[Char] = for (breed <- dogBreeds;
                                            achar <- breed
                                            if breed.contains("Pug"))
                                            yield achar

    println(realCharacters)
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

Before we close up this discussion of the basics of `for comprehensions` in Scala, I'd like to show where we are going in the next section.  The point behind the comprehension is to regularize My Account simplify a pattern of interactions with collections.   Basically to be a Facade of sorts to the various traversals and filterings of collections, even simple one-element collections like `Option` type.  Java  programmers should take note, because the creators of Java 8 took some of these same ideas into Java.

For our simple case assume you have a function that hands back dog breed names as options based on an initial letter for the name and you want to process the result:

```scala
    val dogBreeds = List("Pug", "French Bulldog", "Shiba Inu", "German Bepperd", "Puggle", "Lab")

    def findStarter(start: String): Option[String] = dogBreeds.collectFirst({case breed if (breed.startsWith(start)) => breed})

    def printResult(result: Option[String]) = for {breedName <- result} println(breedName)

    printResult(findStarter("P"))

    printResult(findStarter("X"))

```

So for now just understand (if you are new to Scala) that the def defines functions and ignore the internals of `findStarter` for now.  Just understand that a single element collection of type Option[String] can be returned from `findStarter` which represents either and Option with name of the breed of dog which starts with the given string or None.  None means no value in Scala Option.   More importantly notice the implementation of `printResult` which is simply a `for comprehension`.  Let's talk about this some more:

The expression `breedName <- result` allows the comprehension to bind the value in the Option (a dog breed name) to the variable `breedName`.  What if there is no value or, more properly in Option-speak, what if the value is None?  Normally, if you try to access the value of None you'd get an exception of type `NoSuchElementException`, except the `for comprehension`  deals with this exception and simply stops processing this element.

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

Personally, the comprehension version is cleaner to read but it is idiomatic.

## Advanced Use Cases