# SCALAI

ScalaI is an experiment for some high level optimization, borrowing the syntax of scala

## Effects and Contexts
https://boats.gitlab.io/blog/post/the-problem-of-effects/

https://internals.rust-lang.org/t/can-we-make-a-rusty-effect-system/11697

- Falliblity: The effect of a section of code failing to complete and evaluate to its expected value (in Rust, think Result)
- Multiplicity: The effect of a section of code being evaluated multiple times, yielding many values or operating over many values (in Rust, think Iterator)
- Asynchrony: The effect of a section of code yielding control when it cannot immediately progress, to allow other sections of code to progress instead (in Rust, think Future)

- Pureness: The effect of a function having no side effects
- Input/Output

- Safeness(sorry to toss you in): The effect of a section of code being unsafe, to use `unsafe { }` to suppress. And many other types of safeness
- Deprecation

- Some Rust ideas: Ref, MutRef

## Constant evaluation
```scala
import scala.io.StdIn.readInt
def foo(x: Int): Int = x * 2
println(foo(1) + foo(readInt())) 
```
gives
```scala
import scala.io.StdIn.readInt
def foo(x: Int): Int = x * 2
println(2 + foo(readInt())) 

```

Unless the function is too big, inlining does not perform
## Loop unfolding
```scala
def foo(xs: Seq[() => Int]): Unit = for(func <- xs) println(func)
val x1 = () => 1
val x2 = () => 2
val x3 = () => 3
val xs = Seq(x1, x2, x3)
foo(xs)
```
gives
```scala
println(1)
println(2)
println(3)
```
## Specialization
```scala
import scala.io.StdIn.readInt
def foo(a: Int)(b: Int): Int = a + b
foo(1)(readInt())
def bar(a: Int, b: Int): Int = a + b 
bar(2, readInt()) // auto currying and specialization
bar(readInt(), 3)
```
gives
```scala
import scala.io.StdIn.readInt
def foo_1(b: Int): Int = 1 + b
foo_1(readInt())
def bar_1(b: Int): Int = 2 + b
def bar_2(a: Int): Int = a + 3
bar_1(readInt())
bar_2(readInt())
```
## Type specialization
This is much like transparent inlining in scala3
```scala
class Type
// This syntax is better
def bar(t: Type) = {
  if (t == Boolean) {
    true
  } else {
    "not a boolean"
  }
}
bar(Boolean)
bar(Int)
```
gives
```scala
def foo_1(): Boolean = true
def foo_2(): String = "not a boolean"
foo_1()
foo_2()
def bar_1(): Boolean = true
def bar_2(): String = "not a boolean"
bar_1()
bar_2()
```

## Multiple patterns of arguments
We want to support multiple patterns of arguments

Basic cases

```scala
def foo = ??? // Function(Ident("foo"), Nil)
foo // FunctionApply(Ident("foo"), Nil)
def bar() = ??? // Function(Ident("foo"), List(Params()))
bar() // FunctionApply(Ident("bar"), List(List())
def baz(a: Int) = ??? // Function(Ident("foo"), List(Params(a: Int)))
baz(1) // FunctionApply(Ident("baz"), List(Args(1)))
```

With typed arguments

```scala
def foo[T] = ???
foo[T] // FunctionApply(Ident("foo"), List(Args(T)))
def bar[T, U] = ???
bar[T, U] // FunctionApply(Ident("bar"), List(Args(T, U)))
```

With named arguments

```scala
def foo(a: Int) = ???
foo(a=1) // FunctionApply(Ident("foo"), List(Args(a=1)))
```

With default arguments

```scala
def foo(a: Int=2) = ???
foo() // FunctionApply(Ident("foo"), List(Args(a=Default)))
```

With multiple arguments and currying

```scala
def foo(a: Int)(b: Int) = ???
// Function(Ident("foo"), List(Params(a: Int), Params(b: Int)))
foo(1)
// FunctionApply(Ident("foo"), List(Args(1)))
foo(1)(2)
// FunctionApply(Ident("foo"), List(Args(1), Args(2)))
// FunctionApply(FunctionApply(Ident("foo"), List(Args(1))), List(Args(2)))
```

## TODO
generate similar Encoder/Decoder for BaseNode
```scala
object Test {
  val encoder = new Encoder[TyNode] {
    final def apply(x: TyNode): Json = x match {
      case TyNamed(x) => Json.obj("type" -> Json.fromString("TyNamed"), "value" -> Encoder[String].apply(x))
    }
  }
}
```
