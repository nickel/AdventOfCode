package advent.y2016

import scalaz.Scalaz._
import scalaz._

object Day2 {

  type Action = Point => Writer[String, Point]

  def withNoOutput[A](a: A): Writer[String, A] = a.set("")

  implicit val actionMonoid = new Monoid[Action] {
    override def zero: Action = withNoOutput
    override def append(f1: Action, f2: => Action): Action = point => f1(point).flatMap(f2)
  }

  val Initial = Point(0, 0)

  private def toAction(keysByPos: Map[Point, String])(c: Char): Action = {
    val punch: Action = pos => pos.set(keysByPos(pos))

    def moveTo(dir: Point): Action =
      pos =>
        withNoOutput {
          val newPos = pos + dir
          if (keysByPos.contains(newPos)) newPos else pos
      }

    c match {
      case '\n' => punch
      case 'U' => moveTo(Point(x = 0, y = 1))
      case 'D' => moveTo(Point(x = 0, y = -1))
      case 'L' => moveTo(Point(x = -1, y = 0))
      case 'R' => moveTo(Point(x = 1, y = 0))
    }
  }

  private def runInstructions(input: String, keysByPos: Map[Point, String]) = {
    val allActions = input.toList.foldMap(toAction(keysByPos))
    allActions(Initial).written
  }

  def part1(input: String): String = {
    val keysByPos = Map(
      Point(x = -1, y = 1) -> "1",
      Point(x = 0, y = 1) -> "2",
      Point(x = 1, y = 1) -> "3",
      Point(x = -1, y = 0) -> "4",
      Point(x = 0, y = 0) -> "5",
      Point(x = 1, y = 0) -> "6",
      Point(x = -1, y = -1) -> "7",
      Point(x = 0, y = -1) -> "8",
      Point(x = 1, y = -1) -> "9"
    )

    runInstructions(input, keysByPos)
  }

  def part2(input: String): String = {
    val keysByPos = Map(
      Point(x = 2, y = 2) -> "1",
      Point(x = 1, y = 1) -> "2",
      Point(x = 2, y = 1) -> "3",
      Point(x = 3, y = 1) -> "4",
      Point(x = 0, y = 0) -> "5",
      Point(x = 1, y = 0) -> "6",
      Point(x = 2, y = 0) -> "7",
      Point(x = 3, y = 0) -> "8",
      Point(x = 4, y = 0) -> "9",
      Point(x = 1, y = -1) -> "A",
      Point(x = 2, y = -1) -> "B",
      Point(x = 3, y = -1) -> "C",
      Point(x = 2, y = -2) -> "D"
    )

    runInstructions(input, keysByPos)
  }

  def main(args: Array[String]): Unit = {
    val input =
      """LRULLRLDUUUDUDDDRLUDRDLDDLUUDLDDLRDRLDRLLURRULURLDRLDUDURLURRULLDDDUDDRRRDLRRDDLDURDULLRDLLLDRDLLDULDUDLLDLDRUDLLDLDDRRRDRLUDRDDLUDRRDUDUDLLDDUUDLRDUDRRUDUDRULRULUDRUUDLDLULLRLDLDDRULLRLLLULUULDURURLUUULDURLDDDURRUUDURDDDULDLURLRDRURDRUDRLLDLDRUURLLLRDRURUDLRLUDULLDDURLRURDLRDUUURRLULRRLDDULUUURLRRRLLLLLURDDRUULUDRRRUDDLLULRRUULDRDDULRLDDDRRUULUDRLRUDURUUULDLDULUUDURLLLRRDDRDLURDDDLDDDLRDRLDDURLRLLRUDRRLLDDDDDURDURRDDULDULLRULDRUURDRRDUDDUDDDDRRDULDUURDRUDRLDULRULURLLRRDRDRDLUUDRRLRLDULDDLUUUUUURRLRRRULLDDDRLRDRRRRRRRDUUDLLUDURUDDLURRUDL
        |UDUUURRLRLLDDRRDRRRLDDDLURURLLUDDRLUUDRRRDURRLLRURDLLRRDUUDDDDRDRURRLLLLURDLRRRULLLDLLLUDDLDRRRDLDUUDDRDUDDUURDDLULULDURDURDRUULURURRURDUURUDRRUDRLLLLRRDLLDRDDRLLURDDDUDUDUDRUURDDRUURDLRUUDDRDUURUDDLLUURDLUDRUUDRRDLLUUURDULUULDUUDLLULUUDLUDRUUDUUURLDDDRLRURDDULLRDRULULUDLUUDDDUUDLDUUDRULLDUURDDRUDURULDRDDLRUULRRRDLDLRDULRDDRLLRRLURDLDRUDLRLUDLRLDLDURRUULRLUURDULDRRULLRULRDLLDLDUDRUDDUDLDDURDDDRDLUDRULRUULLRURLDDDRDLRRDRULURULDULRDLDULDURDRDRDRDURDRLUURLRDDLDDRLDDRURLLLURURDULDUDDLLUURDUUUDRUDDRDLDRLRLDURRULDULUUDDLRULDLRRRRDLLDRUUDRLLDLUDUULRDRDLRUUDLRRDDLUULDUULRUDRURLDDDURLRRULURR
        |LDURLLLRLLLUURLLULDLRLLDLURULRULRDUDLDDUDRLRRDLULLDDULUUULDRLDURURLURLDLRUDULLLULDUURLLRDLUULRULLLULRDRULUDLUUULDDURLUDDUDDRDLDRDRUDLUURDDLULDUULURLUULRDRDLURUDRUDLDRLUUUUULUDUDRRURUDRULDLDRDRLRURUUDRDLULLUDLLRUUDUUDUDLLRRRLDUDDDRDUDLDLLULRDURULLLUDLLRUDDUUDRLDUULLDLUUDUULURURLLULDUULLDLUDUURLURDLUULRRLLRUDRDLLLRRRLDDLUULUURLLDRDLUUULLDUDLLLLURDULLRUDUUULLDLRLDRLLULDUDUDRULLRRLULURUURLRLURRLRRRDDRLUDULURUDRRDLUDDRRDRUDRUDLDDRLRDRRLDDRLLDDDULDLRLDURRRRRULRULLUUULUUUDRRDRDRLLURRRRUULUDDUDDDLDURDRLDLLLLLRDUDLRDRUULU
        |URURRUUULLLLUURDULULLDLLULRUURRDRRLUULRDDRUDRRDUURDUDRUDDRUULURULDRLDRDDDLDLRLUDDRURULRLRLLLDLRRUDLLLLRLULDLUUDUUDRDLRRULLRDRLRLUUDDRRLLDDRULLLRLLURDLRRRRRLLDDRRDLDULDULLDLULLURURRLULRLRLLLLURDDRDDDUUDRRRDUUDDLRDLDRRLLRURUDUUUDLDUULLLRLURULRULRDRLLLDLDLRDRDLLLRUURDDUDDLULRULDLRULUURLLLRRLLLLLLRUURRLULRUUUDLDUDLLRRDDRUUUURRRDRRDULRDUUDULRRRDUUUUURRDUURRRRLDUDDRURULDDURDDRDLLLRDDURUDLLRURLRRRUDDLULULDUULURLUULRDLRDUDDRUULLLRURLDLRRLUDLULDRLUDDDRURUULLDLRLLLDULUDDRLRULURLRDRRDDLDLURUDDUUURRDDLUDDRDUULRRDLDRLLLULLRULRURULRLULULRDUD
        |RUDLLUDRRDRRLRURRULRLRDUDLRRLRDDUDRDLRRLLRURRDDLRLLRRURULRUULDUDUULDULDLRLRDLRDLRUURLDRLUDRRDDDRDRRRDDLLLRRLULLRRDDUDULRDRDUURLDLRULULUDLLDRUDUURRUDLLRDRLRRUUUDLDUDRRULLDURRDUDDLRURDLDRLULDDURRLULLRDDDRLURLULDLRUDLURDURRUDULDUUDLLLDDDUUURRRDLLDURRDLULRULULLRDURULLURDRLLRUUDDRRUDRDRRRURUUDLDDRLDRURULDDLLULULURDLDLDULLRLRDLLUUDDUDUDDDDRURLUDUDDDRRUDDLUDULLRDLDLURDDUURDLRLUUDRRULLRDLDDDLDULDUDRDUUULULDULUDLULRLRUULLDURLDULDRDLLDULLLULRLRD
        |""".stripMargin
    println("Part 1 result: " + part1(input))
    println("Part 2 result: " + part2(input))
  }
}