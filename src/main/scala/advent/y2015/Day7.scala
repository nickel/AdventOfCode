package advent.y2015

import scala.language.postfixOps
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.CharSequenceReader
import scalaz.Memo

object Day7 {

  sealed trait Input
  case class Constant(value: Int) extends Input
  case class InputSignal(value: String) extends Input

  sealed trait Connection {
    def output: String
  }
  case class DirectConnection(input: Input, output: String) extends Connection
  case class AndGate(input1: Input, input2: Input, output: String) extends Connection
  case class OrGate(input1: Input, input2: Input, output: String) extends Connection
  case class NotGate(input: Input, output: String) extends Connection
  case class RShiftGate(bits: Int, input: Input, output: String) extends Connection
  case class LShiftGate(bits: Int, input: Input, output: String) extends Connection

  case class Circuit(connections: Map[String, Connection]) {

    def producerOf(signal: String): Option[Connection] = connections.get(signal)

    def hardwire(signal: String, value: Int): Circuit =
      Circuit(connections.updated(signal, DirectConnection(Constant(value), signal)))
  }

  object Circuit {
    def apply(connections: Connection*): Circuit = Circuit.of(connections)

    def of(connections: Seq[Connection]): Circuit =
      Circuit(connections.map(c => (c.output, c)).toMap)
  }

  object CircuitParser extends RegexParsers {

    val number = """\d+""".r ^^ { _.toInt }
    val signal = "[a-z]+".r

    def circuit = (connection +) ^^ Circuit.of

    def connection = directConnection | binaryConnection | notConnection | shiftConnection

    def directConnection = input ~ "->" ~ signal ^^ {
      case value ~ _ ~ output => DirectConnection(value, output)
    }

    def binaryConnection = input ~ (and | or) ~ input ~ "->" ~ signal ^^ {
      case in1 ~ gate ~ in2 ~ _ ~ out => gate(in1, in2, out)
    }
    def and = "AND" ^^^ AndGate.apply _
    def or = "OR" ^^^ OrGate.apply _

    def notConnection = "NOT" ~> input ~ "->" ~ signal ^^ {
      case in ~ _ ~ out => NotGate(in, out)
    }

    def shiftConnection = input ~ (lshift | rshift) ~ number ~ "->" ~ signal ^^ {
      case in ~ gate ~ bits ~ _ ~ out => gate(bits, in, out)
    }
    def lshift = "LSHIFT" ^^^ LShiftGate.apply _
    def rshift = "RSHIFT" ^^^ RShiftGate.apply _

    def input = inputSignal | inputConstant

    def inputSignal = signal ^^ InputSignal.apply

    def inputConstant = number ^^ Constant.apply

    def parse(input: String): Circuit = circuit(new CharSequenceReader(input)).get
  }

  def part1(input: String): Int = evaluate(CircuitParser.parse(input), "a")

  object BitwiseLogic {
    val Mask = 0xFFFF
    def not(value: Int) = ~value & Mask
    def rshift(bits: Int, value: Int) = (value >> bits) & Mask
    def lshift(bits: Int, value: Int) = (value << bits) & Mask
  }

  def evaluate(circuit: Circuit, outputSignal: String): Int = {

    lazy val valueOfSignal = Memo.mutableHashMapMemo[String, Int] { signal =>
      circuit.producerOf(signal) match {
        case Some(DirectConnection(input, _)) => valueOfInput(input)
        case Some(NotGate(input, _)) => BitwiseLogic.not(valueOfInput(input))
        case Some(AndGate(left, right, _)) => valueOfInput(left) & valueOfInput(right)
        case Some(OrGate(left, right, _)) => valueOfInput(left) | valueOfInput(right)
        case Some(RShiftGate(bits, input, _)) => BitwiseLogic.rshift(bits, valueOfInput(input))
        case Some(LShiftGate(bits, input, _)) => BitwiseLogic.lshift(bits, valueOfInput(input))
        case None => throw new NoSuchElementException(s"No connection for $signal")
      }
    }

    def valueOfInput(input: Input): Int = input match {
      case Constant(value) => value
      case InputSignal(signal) => valueOfSignal(signal)
    }

    valueOfSignal(outputSignal)
  }

  def part2(input: String): Int = {
    val circuit = CircuitParser.parse(input)
    val initialResult = evaluate(circuit, "a")
    val modifiedCircuit = circuit.hardwire("b", initialResult)
    evaluate(modifiedCircuit, "a")
  }

  def main(args: Array[String]): Unit = {
    val input = "bn RSHIFT 2 -> bo\nlf RSHIFT 1 -> ly\nfo RSHIFT 3 -> fq\ncj OR cp -> cq\nfo OR fz -> ga\nt OR s -> u\nlx -> a\nNOT ax -> ay\nhe RSHIFT 2 -> hf\nlf OR lq -> lr\nlr AND lt -> lu\ndy OR ej -> ek\n1 AND cx -> cy\nhb LSHIFT 1 -> hv\n1 AND bh -> bi\nih AND ij -> ik\nc LSHIFT 1 -> t\nea AND eb -> ed\nkm OR kn -> ko\nNOT bw -> bx\nci OR ct -> cu\nNOT p -> q\nlw OR lv -> lx\nNOT lo -> lp\nfp OR fv -> fw\no AND q -> r\ndh AND dj -> dk\nap LSHIFT 1 -> bj\nbk LSHIFT 1 -> ce\nNOT ii -> ij\ngh OR gi -> gj\nkk RSHIFT 1 -> ld\nlc LSHIFT 1 -> lw\nlb OR la -> lc\n1 AND am -> an\ngn AND gp -> gq\nlf RSHIFT 3 -> lh\ne OR f -> g\nlg AND lm -> lo\nci RSHIFT 1 -> db\ncf LSHIFT 1 -> cz\nbn RSHIFT 1 -> cg\net AND fe -> fg\nis OR it -> iu\nkw AND ky -> kz\nck AND cl -> cn\nbj OR bi -> bk\ngj RSHIFT 1 -> hc\niu AND jf -> jh\nNOT bs -> bt\nkk OR kv -> kw\nks AND ku -> kv\nhz OR ik -> il\nb RSHIFT 1 -> v\niu RSHIFT 1 -> jn\nfo RSHIFT 5 -> fr\nbe AND bg -> bh\nga AND gc -> gd\nhf OR hl -> hm\nld OR le -> lf\nas RSHIFT 5 -> av\nfm OR fn -> fo\nhm AND ho -> hp\nlg OR lm -> ln\nNOT kx -> ky\nkk RSHIFT 3 -> km\nek AND em -> en\nNOT ft -> fu\nNOT jh -> ji\njn OR jo -> jp\ngj AND gu -> gw\nd AND j -> l\net RSHIFT 1 -> fm\njq OR jw -> jx\nep OR eo -> eq\nlv LSHIFT 15 -> lz\nNOT ey -> ez\njp RSHIFT 2 -> jq\neg AND ei -> ej\nNOT dm -> dn\njp AND ka -> kc\nas AND bd -> bf\nfk OR fj -> fl\ndw OR dx -> dy\nlj AND ll -> lm\nec AND ee -> ef\nfq AND fr -> ft\nNOT kp -> kq\nki OR kj -> kk\ncz OR cy -> da\nas RSHIFT 3 -> au\nan LSHIFT 15 -> ar\nfj LSHIFT 15 -> fn\n1 AND fi -> fj\nhe RSHIFT 1 -> hx\nlf RSHIFT 2 -> lg\nkf LSHIFT 15 -> kj\ndz AND ef -> eh\nib OR ic -> id\nlf RSHIFT 5 -> li\nbp OR bq -> br\nNOT gs -> gt\nfo RSHIFT 1 -> gh\nbz AND cb -> cc\nea OR eb -> ec\nlf AND lq -> ls\nNOT l -> m\nhz RSHIFT 3 -> ib\nNOT di -> dj\nNOT lk -> ll\njp RSHIFT 3 -> jr\njp RSHIFT 5 -> js\nNOT bf -> bg\ns LSHIFT 15 -> w\neq LSHIFT 1 -> fk\njl OR jk -> jm\nhz AND ik -> im\ndz OR ef -> eg\n1 AND gy -> gz\nla LSHIFT 15 -> le\nbr AND bt -> bu\nNOT cn -> co\nv OR w -> x\nd OR j -> k\n1 AND gd -> ge\nia OR ig -> ih\nNOT go -> gp\nNOT ed -> ee\njq AND jw -> jy\net OR fe -> ff\naw AND ay -> az\nff AND fh -> fi\nir LSHIFT 1 -> jl\ngg LSHIFT 1 -> ha\nx RSHIFT 2 -> y\ndb OR dc -> dd\nbl OR bm -> bn\nib AND ic -> ie\nx RSHIFT 3 -> z\nlh AND li -> lk\nce OR cd -> cf\nNOT bb -> bc\nhi AND hk -> hl\nNOT gb -> gc\n1 AND r -> s\nfw AND fy -> fz\nfb AND fd -> fe\n1 AND en -> eo\nz OR aa -> ab\nbi LSHIFT 15 -> bm\nhg OR hh -> hi\nkh LSHIFT 1 -> lb\ncg OR ch -> ci\n1 AND kz -> la\ngf OR ge -> gg\ngj RSHIFT 2 -> gk\ndd RSHIFT 2 -> de\nNOT ls -> lt\nlh OR li -> lj\njr OR js -> jt\nau AND av -> ax\n0 -> c\nhe AND hp -> hr\nid AND if -> ig\net RSHIFT 5 -> ew\nbp AND bq -> bs\ne AND f -> h\nly OR lz -> ma\n1 AND lu -> lv\nNOT jd -> je\nha OR gz -> hb\ndy RSHIFT 1 -> er\niu RSHIFT 2 -> iv\nNOT hr -> hs\nas RSHIFT 1 -> bl\nkk RSHIFT 2 -> kl\nb AND n -> p\nln AND lp -> lq\ncj AND cp -> cr\ndl AND dn -> do\nci RSHIFT 2 -> cj\nas OR bd -> be\nge LSHIFT 15 -> gi\nhz RSHIFT 5 -> ic\ndv LSHIFT 1 -> ep\nkl OR kr -> ks\ngj OR gu -> gv\nhe RSHIFT 5 -> hh\nNOT fg -> fh\nhg AND hh -> hj\nb OR n -> o\njk LSHIFT 15 -> jo\ngz LSHIFT 15 -> hd\ncy LSHIFT 15 -> dc\nkk RSHIFT 5 -> kn\nci RSHIFT 3 -> ck\nat OR az -> ba\niu RSHIFT 3 -> iw\nko AND kq -> kr\nNOT eh -> ei\naq OR ar -> as\niy AND ja -> jb\ndd RSHIFT 3 -> df\nbn RSHIFT 3 -> bp\n1 AND cc -> cd\nat AND az -> bb\nx OR ai -> aj\nkk AND kv -> kx\nao OR an -> ap\ndy RSHIFT 3 -> ea\nx RSHIFT 1 -> aq\neu AND fa -> fc\nkl AND kr -> kt\nia AND ig -> ii\ndf AND dg -> di\nNOT fx -> fy\nk AND m -> n\nbn RSHIFT 5 -> bq\nkm AND kn -> kp\ndt LSHIFT 15 -> dx\nhz RSHIFT 2 -> ia\naj AND al -> am\ncd LSHIFT 15 -> ch\nhc OR hd -> he\nhe RSHIFT 3 -> hg\nbn OR by -> bz\nNOT kt -> ku\nz AND aa -> ac\nNOT ak -> al\ncu AND cw -> cx\nNOT ie -> if\ndy RSHIFT 2 -> dz\nip LSHIFT 15 -> it\nde OR dk -> dl\nau OR av -> aw\njg AND ji -> jj\nci AND ct -> cv\ndy RSHIFT 5 -> eb\nhx OR hy -> hz\neu OR fa -> fb\ngj RSHIFT 3 -> gl\nfo AND fz -> gb\n1 AND jj -> jk\njp OR ka -> kb\nde AND dk -> dm\nex AND ez -> fa\ndf OR dg -> dh\niv OR jb -> jc\nx RSHIFT 5 -> aa\nNOT hj -> hk\nNOT im -> in\nfl LSHIFT 1 -> gf\nhu LSHIFT 15 -> hy\niq OR ip -> ir\niu RSHIFT 5 -> ix\nNOT fc -> fd\nNOT el -> em\nck OR cl -> cm\net RSHIFT 3 -> ev\nhw LSHIFT 1 -> iq\nci RSHIFT 5 -> cl\niv AND jb -> jd\ndd RSHIFT 5 -> dg\nas RSHIFT 2 -> at\nNOT jy -> jz\naf AND ah -> ai\n1 AND ds -> dt\njx AND jz -> ka\nda LSHIFT 1 -> du\nfs AND fu -> fv\njp RSHIFT 1 -> ki\niw AND ix -> iz\niw OR ix -> iy\neo LSHIFT 15 -> es\nev AND ew -> ey\nba AND bc -> bd\nfp AND fv -> fx\njc AND je -> jf\net RSHIFT 2 -> eu\nkg OR kf -> kh\niu OR jf -> jg\ner OR es -> et\nfo RSHIFT 2 -> fp\nNOT ca -> cb\nbv AND bx -> by\nu LSHIFT 1 -> ao\ncm AND co -> cp\ny OR ae -> af\nbn AND by -> ca\n1 AND ke -> kf\njt AND jv -> jw\nfq OR fr -> fs\ndy AND ej -> el\nNOT kc -> kd\nev OR ew -> ex\ndd OR do -> dp\nNOT cv -> cw\ngr AND gt -> gu\ndd RSHIFT 1 -> dw\nNOT gw -> gx\nNOT iz -> ja\n1 AND io -> ip\nNOT ag -> ah\nb RSHIFT 5 -> f\nNOT cr -> cs\nkb AND kd -> ke\njr AND js -> ju\ncq AND cs -> ct\nil AND in -> io\nNOT ju -> jv\ndu OR dt -> dv\ndd AND do -> dq\nb RSHIFT 2 -> d\njm LSHIFT 1 -> kg\nNOT dq -> dr\nbo OR bu -> bv\ngk OR gq -> gr\nhe OR hp -> hq\nNOT h -> i\nhf AND hl -> hn\ngv AND gx -> gy\nx AND ai -> ak\nbo AND bu -> bw\nhq AND hs -> ht\nhz RSHIFT 1 -> is\ngj RSHIFT 5 -> gm\ng AND i -> j\ngk AND gq -> gs\ndp AND dr -> ds\nb RSHIFT 3 -> e\ngl AND gm -> go\ngl OR gm -> gn\ny AND ae -> ag\nhv OR hu -> hw\n1674 -> b\nab AND ad -> ae\nNOT ac -> ad\n1 AND ht -> hu\nNOT hn -> ho"
    println("Part 1 result: " + part1(input))
    println("Part 2 result: " + part2(input))
  }
}