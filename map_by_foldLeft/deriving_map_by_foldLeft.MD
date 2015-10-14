# Deriving map by using foldLeft or (How I lost a Sunday Afternoon)
## How Did I Get Here?
Well, I like working through various tutorials and exercises to improve my programming, specifically Functional Programming using Scala.  I have already made a lot of progress in parts of Dean Wampler and Alex Payne's, “Programming Scala” as well as most of the first section of Paul Chiusano and Rúnar Bjarnason's “Functional Programming in Scala.”

However, I found [Maurício Linhares' Learning Scala by building Scala - Lists](http://mauricio.github.io/2013/11/25/learning-scala-by-building-scala-lists.html) to be interesting enough to try, in the interest of speed and fun.  As I wound down to the conclusion of the first part, Maurício states:

>We could even implement the map function above in terms of a fold!

*Of course! Trivial!* Yeah... not quite, not if it is a `foldLeft`. Now, luckily I had seen something like what I might need as part of the solution for this.  However, I could only remember two things about it:

1. I need to return a function, delay evaluation, we'll talk about why later.

2. I think I need to start with some kind of Identity function as the accumulator.

While I knew where to find the code to do all of this, I wanted to burn in the lesson by struggling with it myself.

## Use the REPL and Model The Thing You Want
### Classes Used from Maurício's Tutorial
My explanation is based on the classes of the tutorial and so I provide them here to make this easier for the reader:

```scala
sealed trait LinkedList[+E] {
    def map[R](f: E => R): LinkedList[R] = {
        this match {
            case Node(head, tail) => Node(f(head), tail.map(f)) 
            case Empty => Empty
        }
    }

    @tailrec
    final def foldLeft[B](accum: B)(f: (B, E) => B): B = { 
        this match {
            case Empty => accum
            case Node(head, tail) => tail.foldLeft(f(accum, head))(f)
        }
    }
}

case class Node[+E]( val head : E, val tail : LinkedList[E]  ) extends LinkedList[E]

case object Empty extends LinkedList[Nothing]

object LinkedList {
    def apply[E](items: E*): LinkedList[E] = {
            if (items.isEmpty)
                Empty
            else
                Node(items.head, apply(items.tail :_*))
    }
}
```
### My "Process"
I wasn't sure how to start.  I am visual by nature and needed to work with something I could *see* in some fashion.  Thankfully I was using a modern language with a REPL, Scala, to do this.  So I started toying around with modeling some part of this:

```scala
val f: LinkedList[Nothing] => LinkedList[String] = (tail) => Node("nine", Node("ten", tail))
```
So here I figured out a pattern, the pattern I'll call *Delaying the Create of the List*.  Which is not very interesting but I'll point out that this is in effect what I want to end up with - sort of - with my real code.  In fact, I abstracted a little further and came up with this:

```scala
val g:(LinkedList[Int], String => Int) =>  LinkedList[Int] = (tail,f) => Node(f("Nine"), Node(f("ten"), tail))
```
Now, I added the `map` function to the whole process and I could use this function to build a `LinkedList` where I mapped the strings to their length.  *Woot!*:

```scala
scala> g(LinkedList[Int](), _.length)
res3: LinkedList[Int] = Node(4,Node(3,Empty))
```
At this point I started stumbling around and I needed less RELP and some paper. Using one of my lab notebooks I use to talk to myself in pictures and pseudo-code I started looking at the `reverse` function from the blog post:

```scala
def reverse() : LinkedList[E] = {
  foldLeft(LinkedList[E]()) {
    (acc, item) => Node(item, acc)
  }
}
```
Then I decided to write down a *trace* of what `foldLeft` did in this case.  Keep in mind that `foldLeft` is tail recursion optimized and accumulates the results of the processing until it reaches the end of the input list.  It does so under the assumption that the operation is *left associative* but in this case it is not.  Notice that the `Node`  constructor wants a value element followed by a `LinkedList` and then this new `LinkedList` would be fed into the right side of the next `concatenation` of a value using the `Node` constructor.   

However, as my trace will reveal, this leads to a natural reversal of order of the list, which is fine for `reverse` but not so much for `map`.  `foldRight` would have been a better choice, but I wanted to take this on as a challenge.  Normally, you would do what I am deriving here in a `foldRight` using a tail recusion optimized `foldLeft` (like this one).  Any way, my trace shows:

```scala
// Given this list:
val l = Node(1, Node(2, Empty))
// We see calls like this against the anonymous function in foldLeft
(Empty, 1)          => Node(1, Empty)
(Node(1, Empty), 2) => Node(2, Node(1, Empty))
// Returns
Node(2, Node(1, Empty))
```
So, I see my key problem is I need to delay assembling the list and I want to find a way to pass that initial empty list, `LinkedList[E]()` into a composed function (like my hand coded examples above) after I have composed the assembly of a list using `foldLeft`.

I did some more sketching in my lab book, not sure how to convey it but I'll try:
```scala
// Starting with my example from above, repeated here for ease of reading:
val g:(LinkedList[Int], String => Int) =>  LinkedList[Int] = (tail,f) => Node(f("Nine"), Node(f("ten"), tail))

// Okay so I need a way to return this by composing it but I also needed tp
// understand what my accumulator was going to be. Then I realized, "duh"
// I need to accept the empty list LinkedList[E] and return a LinkedList[E]:
val acc: LinkedList[Int] => LinkedList[Int] = null

// More generally:
val acc: B => B = null

// So let's apply this nifty concept to foldLeft's signature
def foldLeft[B => B](acc: B=>B)(f: (B=>B, E) => B => B) : B => B = {}

// Now, let's apply this to map using foldLeft that returns an empty list...
def map[R](f: E => R): LinkedList[R] = {
    foldLeft(<stuff>)(LinkedList[R]())
}

// Okay what is <stuff>???  How, do we get rid of the empty list?  
def map[R](f: E => R): LinkedList[R] = {
    foldLeft(x => x)((g, head) => (list) => g(Node(f(head), list)))
}

// Wow!  Okay so I called the accumulator g, it was one of things I remembered
// from Chiusano's book.  However I am returning: 
//      LinkedList[Int] => LinkedList[Int]
// Also, by binding the accumulator "g" as a function, I am composing functions.
// Finally, I am binding the value of of "head" within a call to the mapper.  
```
## The Exciting Conclusion
While I am not sure I conveyed all of my thoughts, my headache and frustration; I think I showed the avenues I needed to follow to come up with a solution based on Functional Programming concepts.

So, while I am not advocating using such *unnatural* means to implement map - use a `foldRight` based on a stack friendly, tail recursive `foldLeft` - I did get this solution:

```scala
def map_lf[R](f: E => R): LinkedList[R] = {
    val acc: LinkedList[R] => LinkedList[R] = b => b
    val h: (LinkedList[R] => LinkedList[R], E) => LinkedList[R] => LinkedList[R] = (g, head) => (list) => g(Node(f(head), list))

    foldLeft(acc)(h)(LinkedList[R]())
}
```
Now, this is very verbose for Scala, and I take a few steps to refactor the code.  At this point I did consult code I had written before because, as usual there were some painful gaps in the type inferencing.  Did I happen to mention I did all of this coding in the REPL and using a Sublime Text 2?  Not a lot of love there regarding completion of feedback:

```scala
def map[R](f: E => R): LinkedList[R] = {
    foldLeft((b: LinkedList[R]) => b)((g, head) => (list) => g(Node(f(head), list)))(LinkedList[R]())
}
```
So, with Scala and Functional Programming in general there is a lot of potential for leveraging deep and powerful concepts.  I admit that it is not as readable as just using `reverse` and `foldLeft` but if you cannot afford to wait for two passes of your data... you can always resort to composition.  I will leave with this parting thought.  While we are using tail recursion optimization, what does that mean regarding memory when composing anonymous functions?  What is the cost?  Maybe later when I learn more about what this compiles into I can say something about this too.