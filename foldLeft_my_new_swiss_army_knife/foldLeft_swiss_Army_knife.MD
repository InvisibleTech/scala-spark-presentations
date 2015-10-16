# foldLeft - My New Swiss Army knife
## In a Prior Post
In a [prior post](http://www.aninvisiblefriend.com/post/131130570800/implementing-map-using-foldleft-in-scala) this week I mentioned how I was working on [Maurício Linhares' Learning Scala by building Scala - Lists](http://mauricio.github.io/2013/11/25/learning-scala-by-building-scala-lists.html) tutorial and decided to take a challenge of deriving my own implementation of `map` using `foldLeft`.  If you know the left-associative nature of `foldLeft` you'll know why I called that a challenge.  If you don't get it, please read my referenced [prior post](http://www.aninvisiblefriend.com/post/131130570800/implementing-map-using-foldleft-in-scala).

Anyway, I was working through [Maurício Linhares' second tutorial in the series](http://mauricio.github.io/2013/12/08/learning-scala-by-building-scala-lists-part-2.html) and was having a good time of it.  In fact, there were times for expository reasons the original tutorials would implement new functions like `::`, `:::` or `foreach` using new recursive helpers and `case` statements.  At this point I decided to turn the `foldLeft` dial up to **11** and see what I could get done that way.
## foldLeft: A Thing of Utility and Beauty
So, taking a cue from last post's exercise I realize I didn't need no **O(2N)** `reverse` followed by a `foldLeft` to implement `foldRight`.  No I had suffered through that `map` exercise to be able to do this on my own (without [Sensei Chiusano's help](http://pchiusano.github.io/)). 

Assuming this `foldLeft` is in place:

```scala
@tailrec
final def foldLeft[B](accum: B)(f: (B, E) => B): B = { 
    this match {
        case Empty => accum
        case Node(head, tail) => tail.foldLeft(f(accum, head))(f)
    }
}
``` 
I went with this for `foldRight`:

```scala
def foldRight[B](initialAccum: B)(f: (E, B) => B) : B = {
    foldLeft((b: B) => b)((g, head) => (lastAccum) => g(f(head, lastAccum))) (initialAccum)
}
```
In a normal, intuitive approach one could simply implement a `reverse` then use  `foldLeft` by chaining them together here like this:

```scala
def foldRight[B]( accumulator : B )(f : (E,B) => B) : B = {
  reverse().foldLeft(accumulator)((acc,item) => f(item,acc))
}
```
Yes, I do see this as more readable, but it is still **O(2N)** and well, like I said I felt like going to my limits with `foldLeft`.

Now, I admit, I cheated a bit because IMO using my `foldRight` *is* using my `foldLeft` so in the interest of time and space I ended up doing all of these functions via `foldLeft` directly or via `foldRight` using just `foldLeft` **without** `reverse` being used too:

```scala

def reverse(): LinkedList[E] = {
    foldLeft(LinkedList[E]())((acc, head) => Node(head, acc))
}

def filter(f: E => Boolean) : LinkedList[E] = {
    foldRight(LinkedList[E]())((head, acc) => if (f(head)) Node(head, acc) else acc)
}

def ::[B >: E](elem: B): LinkedList[B] = Node(elem, this)

def :::[B >: E](prefix: LinkedList[B]): LinkedList[B] = {
    prefix match {
        case Empty =>  this
        case _ => prefix.foldRight(this:LinkedList[B])((e, acc) => Node[B](e, acc))
    }
}

def foreach(f : (E) => Unit) {
    foldLeft(())((acc, head) => f(head))
}
```
Now, due to a lack of theoretical maturity on my part (I am working on that too) I am not sure what deeper meaning I should derive from this experience.  What property of Functonal Programming, Type Theory?  What is that I am seeing here?  Well, if I find it I will be sure to share that insight here with you.
