package ic.org.arm

import arrow.core.None
import arrow.core.some
import ic.org.util.Code
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

abstract class StdFunc {
  abstract val name: String
  abstract val body: Code
  internal val label by lazy { Label(name) }
}

abstract class IOFunc : StdFunc() {
  abstract val msgTemplate: String
  internal val msg by lazy { StringData(msgTemplate, msgTemplate.length - 1) }
  override val body by lazy { Code(instructions, msg.body) }
  abstract val instructions: PersistentList<Instr>
}

object PrintIntStdFunc : IOFunc() {
  override val name = "p_print_int"
  override val msgTemplate = "%d\\0"

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      MOVInstr(rd = Reg(1), op2 = Reg(0)),
      LDRInstr(Reg.ret, ImmEqualLabel(msg.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("printf"),
      LDRInstr(Reg.ret, 0),
      BLInstr("fflush"),
      POPInstr(PC)
    )
  }
}

object PrintLnStdFunc : IOFunc() {
  override val name = "p_print_ln"
  override val msgTemplate = "\\0"

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      LDRInstr(Reg(0), ImmEqualLabel(msg.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("puts"),
      LDRInstr(Reg(0), 0),
      BLInstr("fflush"),
      POPInstr(PC)
    )
  }
}

object PrintBoolStdFunc : IOFunc() {
  override val name = "p_print_bool"
  override val msgTemplate = "true\\0"

  private const val msg2Template = "false\\0"
  private val msg2 by lazy { StringData(msg2Template, msg2Template.length - 1) }

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      CMPInstr(Reg(0), 0),
      LDRInstr(NECond, Reg(0), ImmEqualLabel(msg.label)),
      LDRInstr(EQCond, Reg(0), ImmEqualLabel(msg2.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("printf"),
      LDRInstr(Reg.ret, 0),
      BLInstr("fflush"),
      POPInstr(PC)
    )
  }

  override val body by lazy { Code(instructions, msg.body + msg2.body) }
}
object PrintStringStdFunc : IOFunc() {
  override val name = "p_print_string"
  override val msgTemplate = "%.*s\\0"

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      LDRInstr(Reg(1), Reg(0).zeroOffsetAddr),
      ADDInstr(None, false, Reg(2), Reg(0), 4),
      LDRInstr(Reg(0), ImmEqualLabel(msg.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("printf"),
      LDRInstr(Reg.ret, 0),
      BLInstr("fflush"),
      POPInstr(PC)
    )
  }
}

object ReadIntStdFunc : IOFunc() {
  override val name = "p_read_int"
  override val msgTemplate = "%d\\0"

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      MOVInstr(rd = Reg(1), op2 = Reg(0)),
      LDRInstr(Reg.ret, ImmEqualLabel(msg.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("scanf"),
      POPInstr(PC)
    )
  }
}

object ReadCharStdFunc : IOFunc() {
  override val name = "p_read_char"
  override val msgTemplate = " %c\\0"

  override val instructions by lazy {
    persistentListOf(
      label,
      PUSHInstr(LR),
      MOVInstr(rd = Reg(1), op2 = Reg(0)),
      LDRInstr(Reg.ret, ImmEqualLabel(msg.label)),
      ADDInstr(None, false, Reg(0), Reg(0), 4),
      BLInstr("scanf"),
      POPInstr(PC)
    )
  }
}

object MallocStdFunc : StdFunc() {
  override val name = "malloc"
  override val body = Code.empty
}

/**
 * C stdlib function. NOT FOR USE IN GENERATED CODE. Use [FreePairFunc] instead, for example.
 */
object FreeStdLibFunc : StdFunc() {
  override val name = "free"
  override val body = Code.empty
}

object FreePairFunc : StdFunc() {
  override val name = "p_free_pair"

  val msgText = "NullReferenceError: dereference a null reference\\n\\0"
  val msg = StringData(msgText, msgText.length - 2) // TODO check length

  override val body = Code.empty.withFunction(RuntimeError.body) +
    PUSHInstr(LR) +
    CMPInstr(cond = None, rn = Reg.first, int8b = 0) +
    LDRInstr(cond = EQCond.some(), rd = Reg.first, addressing = ImmEqualLabel(msg.label)) +
    BInstr(cond = EQCond.some(), label = RuntimeError.label) +
    TODO("William pls pick thhis up to fit the structure of our pairs") as Code
    // PUSHInstr(Reg.first) +
    // LDRInstr(Reg.first, Reg.first.zeroOffsetAddr) +
    // BLInstr(FreeStdLibFunc.label) +
    // LDRInstr(Reg.first, SP.zeroOffsetAddr) +
    // LDRInstr(Reg.first, Reg.first.withOffset(Type.Sizes.Word.bytes)) +



}
