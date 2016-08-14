# Generic Function for Value Types - What Happend on the Way
## What was I trying to do?
Basically, I was simply following along in a very easy reading *Grokking Algorithms*which seems to target people without a computer science background.   However, it covers some interesting algorithms later in the book which I never had in my college education many years ago.  So, while working on the code, I decided to use Scala.  For a simple recursive summing of lists I was using 'match' and trying to make the code work all value types: 'Int', 'Double, 'Long', and so forth.
## Before Getting to a Solution
On the way to writing my recursive, generic list summer I bumped into a few weird errors.  Looking at this first pass which I wrote using a text editor that also runs the SBT repl so I could '~test' and each change saved would compile and run tests.

```scala
    package org.invisibletech.grokking 

    import scala.math.Numeric._

    object DivideAndConquer {
        def sum[T](a: List[T])(implicit n: Numeric[T]): T = {
            a match {
                case Nil => 0
                case head :: rest => head + sum(rest)
            }
        }
    }
```

I incorrectly assumed Numeric would fix this.  I forgot, the 'Numeric' that comes in via the 'implicit' is actually an object you need to use.  The errors you get are not all helpful:

```bash
[info] Compiling 1 Scala source to /Users/johnferguson/Documents/Code/scala/algorithms-programming-solutions/target/scala-2.11/classes...
[error] /Users/johnferguson/Documents/Code/scala/algorithms-programming-solutions/src/main/scala/DvideAndConquer.scala:8: type mismatch;
[error]  found   : Int(0)
[error]  required: T
[error]             case Nil => 0
[error]                         ^
[error] /Users/johnferguson/Documents/Code/scala/algorithms-programming-solutions/src/main/scala/DvideAndConquer.scala:9: type mismatch;
[error]  found   : List[T]
[error]  required: List[String]
[error]             case head :: rest => head + sum(rest)
[error]                                             ^
[error] two errors found
[error] (compile:compileIncremental) Compilation failed
[error] Total time: 2 s, completed Aug 13, 2016 11:08:25 PM
```

Of course the first error makes sense you need a generic `zero` value for `AnyVal` that is a numeric type.  The second error isn't telling you anything about your code you wrote.  However, having played with `Numeric` based code in another exercise, I guessed that what I was missing was using the implicit to add values.  This, I hoped, would address whatever type inferencing was causing the tail of the list to be treated as a `List[String]`:

```scala
    package org.invisibletech.grokking 
    import scala.math.Numeric._

    object DivideAndConquer {
        def sum[T](a: List[T])(implicit n: Numeric[T]): T = {
            a match {
                case Nil => n.zero
                case head :: rest => n.plus(head, sum(rest))
            }
        }
    }
```

I had been doing some other experiments with 'implicits' and 'Array' objects.  I quickly learned that 'Predefs' and other parts of the Scala baseline classes present to Scala programs do an awful lot of implicit work to make Arrays function as they appear to by default.  In fact, as I was trying to mute various imports to allow my implicit function to work, I simply got to the point that I couldn't figure out the last part of making this happen.  Since none of that work was central to what I was trying to do, I put that off for now.

Anyway, what I wanted to show here was that while the compiler with generics and type inferencing can give you messages that are cryptic.  The solution was to question my assumptions about my type and learn more about using Numeric as designed.  There may be a way to add the overloaded operators needed to Numeric but this was enough for my current needs.
