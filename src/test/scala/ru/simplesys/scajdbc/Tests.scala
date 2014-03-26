package ru.simplesys
package scajdbc

import org.scalatest.FunSuite
//import com.simplesys.log.Logging
import ru.simplesys.scajdbc.macros._

trait SomeTrait {
  def a = 8
}

@TableDef class UserTable(val ds: DataSource) extends Table {
  import shapeless.{::, HNil, HList}

  val sqlTableName = "A_USER"

  val id = TableColumnLong("id")
  val caption = TableColumnString("sCaption")
  val description = TableColumnStringOption("sDescription")

  override val test: SelectType = 1L :: "Vasya" :: None :: HNil

  @TableDef class UserTableNested extends Table {
    val ds = UserTable.this.ds
    val sqlTableName = "A_USER2"

    val id = TableColumnLong("id2")
    val caption = TableColumnString("sCaption2")
    val description = TableColumnStringOption("sDescription2")

    override val test: SelectType = 2L :: "Vasya2" :: None :: HNil
  }
}


class Tests extends FunSuite /*with Logging*/ {

  test("Table A_USER") {

//    @TableDef

    val testTable = new UserTable(new DataSource {})
    println(testTable.x)
    println(classOf[testTable.ProjectionType])
    println(classOf[testTable.SelectType])

  }

  test("test") {

    @QuasiQuoteAddTrait class Blargh extends SomeTrait {
      def b = "ooga"
    }
    val aBlargh = new Blargh()
    println(aBlargh.x)
  }
}
