# The Java Map Operation Using a Lambda, Comparative Performance
## Preface
My goal is to share results from a test that is meant to determine the *relative* time differences of using three mechanisms:
* Map on a collection stream using lambda.
* Map on a collection stream using an instance of a static inner class implementing the `java.util.function.Function` interface.
* Iterate on a collection using `foreach`.

All three types of processing a `List<Double>`  are being used to collect a `List<String>` based on applying `toString` to each `Double` object.

Why did I include the `foreach`?  Because from some claims I have read, that is supposed to be one of the fastest ways to step over a collection.  Though of course you should measure that.

### The Benchmark
My benchmark can be found at [my github repo](https://github.com/InvisibleTech/jmhlambdas)

## TL;DR
Though I hope you'll look at and run the test for yourself, in the interest of getting to the point I'll say that you shouldn't automatically avoid using Java lambdas based on *the performance argument*.  You may have other reasons. For some people and uses, they are not the best choice.  I will disclose, I do use them a lot, in the interest of really getting a feel for their use.  I have backed off in places where they add no value.  What value?  That is another discussion.

During my two rounds of benchmarks, I found that there was about a 2% *tax* for using lambdas across the whole processing time.  At time scales of microseconds and below this might be absurd to consider if those times are for processes that don't need to approach real time (for which Java is not guaranteed).  Going in the opposite direction, if your processing already takes 24 hours and due to some SLA **it cannot take more**, not even 28 minutes more, then maybe lambdas are also the wrong choice.  However, I haven't measured this case and I don't know if the 2% difference gets worse or better at such large time scales.

I derived my 2% simply by taking the difference between the lambda time and the fastest time in each JMH benchmark, and divding that difference by the fastest time. I did not include anything to the right of decimal point since the numbers are not absolute as indicated by their error ranges.  

I have extracted the summary of the output, see right below.  You can also get the code and run the tests yourself.  I advise you find something to do while you wait, unless of course you have **The Ultimate Machine**.

```bash
Running org.invisibletech.LambdaPerfTest
# JMH 1.17.1 (released 4 days ago)
# VM version: JDK 1.8.0_74, VM 25.74-b02
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 3 iterations, 30 s each
# Measurement: 10 iterations, 30 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
...
Benchmark                         Mode  Cnt    Score   Error  Units
LambdaPerfTest.anonStaticClasses  avgt   10  150.335 ± 0.629  us/op
LambdaPerfTest.forEach            avgt   10  152.494 ± 0.490  us/op
LambdaPerfTest.lambdas            avgt   10  154.513 ± 0.625  us/op
...
# JMH 1.17.1 (released 4 days ago)
# VM version: JDK 1.8.0_74, VM 25.74-b02
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 3 iterations, 30 s each
# Measurement: 10 iterations, 30 s each
# Timeout: 10 min per iteration
# Threads: 3 threads, will synchronize iterations
# Benchmark mode: Average time, time/op
...
Benchmark                         Mode  Cnt    Score   Error  Units
LambdaPerfTest.anonStaticClasses  avgt   10  201.358 ± 1.165  us/op
LambdaPerfTest.forEach            avgt   10  199.723 ± 2.323  us/op
LambdaPerfTest.lambdas            avgt   10  205.454 ± 1.558  us/op

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2,360.062 sec

```
## Your Results May Differ
I understand the lambdas and any optimizations regarding them may be highly dependent on versions of Java and whose Java.  So, reallly the best thing you can do is try my test.  If you find anything different, insane or puzzling feel free to contact me the email on my [github profile](https://github.com/InvisibleTech).
# The Java Map Operation Using a Lambda, Comparative Performance
## Preface
The statement is pretty specific, my goal is to share results from a test that is meant to determine the *relative* time differences of using three mechanisms:
* Map on a collection stream using lambda.
* Map on a collection stream using an instance of a static inner class implementing the `java.util.function.Function` interface.
* Iterate on a collection using `foreach`.

All three types of processing a `List<Double>`  are being used to collect a `List<String>` based on applying `toString` to each `Double` object.

Why did I include the `foreach`?  Because from some claims I have read, that is supposed to be one of the fastest ways to step over a collection.  Though of course you should measure that.

### The Benchmark
My benchmark can be found at [my github repo https://github.com/InvisibleTech/jmhlambdas] 

## TL;DR
Though I hope you'll look at and run the test for yourself, in the interest of getting to the point I'll say that you shouldn't automatically avoid using Java lambdas based on *the performance argument*.  You may have other reasons. For some people and uses, they are not the best choice.  I will disclose, I do use them a lot, in the interest of really getting a feel for their use.  I have backed off in places where they add no value.  What value?  That is another discussion.

During my two rounds of benchmarks, I found that there was about a 2% *tax* for using lambdas across the whole processing time.  At time scales of microseconds and below this might be absurd to consider if those times are for processes that don't need to approach real time (for which Java is not guaranteed).  Going in the opposite direction, if your processing already takes 24 hours and due to some SLA **it cannot take more**, not even 28 minutes more, then maybe lambdas are also the wrong choice.  However, I haven't measured this case and I don't know if the 2% difference gets worse or better at such large time scales.

I derived my 2% simply by taking the difference between the lambda time and the fastest time in each JMH benchmark, and divding that difference by the fastest time. I did not include anything to the right of decimal point since the numbers are not absoulte as indicated by their error ranges.  

I have extracted the summary of the output, see right below.  You can also get the code and run the tests yourself.  I advise you find something to do while you wait... unless of course you have **The Ultimate Machine**.

```bash
Running org.invisibletech.LambdaPerfTest
# JMH 1.17.1 (released 4 days ago)
# VM version: JDK 1.8.0_74, VM 25.74-b02
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 3 iterations, 30 s each
# Measurement: 10 iterations, 30 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
...
Benchmark                         Mode  Cnt    Score   Error  Units
LambdaPerfTest.anonStaticClasses  avgt   10  150.335 ± 0.629  us/op
LambdaPerfTest.forEach            avgt   10  152.494 ± 0.490  us/op
LambdaPerfTest.lambdas            avgt   10  154.513 ± 0.625  us/op
...
# JMH 1.17.1 (released 4 days ago)
# VM version: JDK 1.8.0_74, VM 25.74-b02
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home/jre/bin/java
# VM options: <none>
# Warmup: 3 iterations, 30 s each
# Measurement: 10 iterations, 30 s each
# Timeout: 10 min per iteration
# Threads: 3 threads, will synchronize iterations
# Benchmark mode: Average time, time/op
...
Benchmark                         Mode  Cnt    Score   Error  Units
LambdaPerfTest.anonStaticClasses  avgt   10  201.358 ± 1.165  us/op
LambdaPerfTest.forEach            avgt   10  199.723 ± 2.323  us/op
LambdaPerfTest.lambdas            avgt   10  205.454 ± 1.558  us/op

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2,360.062 sec

```
## Your Results May Differ
I understand the lambdas and any optimizations regarding them may be highly dependent on versions of Java and whose Java.  So, reallly the best thing you can do is try my test.  If you find anything different, insane or puzzling feel free to contact me the email on my [github profile https://github.com/InvisibleTech].
