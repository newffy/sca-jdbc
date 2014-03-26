package ru.simplesys.scajdbc
package macros

import shapeless.{::, HNil, HList}

trait DataSource


trait SQLColumnExpression[T] {
  type TSimpleType = T
  val sqlExpression: String
}

trait SQLColumnNamed[T] {
  val sqlColumnName: String
}

trait TableColumn[T] extends SQLColumnExpression[T] with SQLColumnNamed[T] {
  val sqlExpression = sqlColumnName
}

trait AliasedSQLColumnExpression[T] extends SQLColumnExpression[T] with SQLColumnNamed[T]

class BaseTableColumn[T](val sqlColumnName: String) extends TableColumn[T]

class TableColumnLong(sqlColumnName: String) extends BaseTableColumn[Long](sqlColumnName)

object TableColumnLong {
  def apply(sqlColumnName: String): TableColumnLong = new TableColumnLong(sqlColumnName)//new BaseTableColumn[Long](sqlColumnName)
}

object TableColumnLongOption {
  def apply(sqlColumnName: String): TableColumn[Option[Long]] = new BaseTableColumn[Option[Long]](sqlColumnName)
}

object TableColumnString {
  def apply(sqlColumnName: String): TableColumn[String] = new BaseTableColumn[String](sqlColumnName)
}

object TableColumnStringOption {
  def apply(sqlColumnName: String): TableColumn[Option[String]] = new BaseTableColumn[Option[String]](sqlColumnName)
}

trait SQLRelation {
  val ds: DataSource
  def columns: List[TableColumn[_]]
}


/*
trait FieldValueTypes[F <: HList, V <: HList]

object FieldValueTypes {
  implicit val nilFieldValueTypes: FieldValueTypes[HNil, HNil] = new FieldValueTypes[HNil, HNil] {}

  implicit def hlistFieldValueTypes[FH <: TableColumn[_], FT <: HList, VT <: HList](implicit next: FieldValueTypes[FT, VT]): FieldValueTypes[FH :: FT, FH#TSimpleType :: VT] = new FieldValueTypes[FH :: FT, FH#TSimpleType :: VT] {}
}


*/

trait Table extends SQLRelation {
  type ProjectionType <: HList
  type SelectType <: HList
  val sqlTableName: String
  val columns: List[TableColumn[_]] = List()
  val test: SelectType
}

trait TableImpl[CL <: HList, VL <: HList] {
  self: Table =>
  type ProjectionType = CL
  type SelectType = VL
}

abstract class TableQuery(val ds: DataSource) extends SQLRelation
