# Generic Function for Value Types - What Happened on the Way
## What was I trying to do?
Basically, I was simply following along in the easy reading *Grokking Algorithms*, which seems targeting people without a computer science background.   However, it covers some interesting algorithms later in the book which I wanted implement using Scala.  For a simple recursive summing of lists I was using 'match' and trying to make the code work all value types: 'Int', 'Double, 'Long', and so forth.
## Before Getting to a Solution
On the way to writing my recursive, generic list summer I bumped into a few weird errors.  I wrote the code using a text editor while relying on the SBT repl so I could use '~test' continuous syntax checking.  In short, no syntax guidance from an IDE.

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

Of course my first error was using '0' instead of something for additive identity.  I forgot, the 'Numeric' that comes in via the 'implicit' is actually an object you need to use and would not *fix* the additive identity with a cast by itself.  The compiler errors you get are a mix of helpful and a little less so:

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

Of course the first error makes sense you need a generic `zero` value to provide the additive identity base value.  The second error isn't telling you anything about the code you wrote.  However, having played with `Numeric` based code in another exercise, I guessed that what I was missing was using the implicit to add values.  This, I hoped, would address whatever type inferencing was causing the tail of the list to be treated as a `List[String]`:

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
This worked and I had my working solution.  For me the interesting part of the exercise was making the leap from an error message that was a bit misleading to making better use of Numeric.