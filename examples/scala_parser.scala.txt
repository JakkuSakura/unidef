def parse(pattern: String): Unit = {
  for (c <- pattern)
    c match {
      case '1' => println("1")
      case '2' => println("2")
      case '3' => println("3")
      case '4' => println("4")
      case '5' => println("5")
    }
}
def main(): Unit = {
  parse("12345")
}
