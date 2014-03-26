package ru.simplesys.scajdbc
package macros

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

class identity extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro identityMacro.impl
}

object identityMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList
    val (annottee, expandees) = inputs match {
      case (param: ValDef) :: (rest @ (_ :: _)) => (param, rest)
      case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
      case _ => (EmptyTree, inputs)
    }
    println((annottee, expandees))
    val outputs = expandees
    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}


trait Helper[+C <: Context] {

  val c: C
  import c.universe._
  import shapeless.{::, HNil, HList}


  def typeCheckClassDef(cd: ClassDef): c.Type = {
    val block = if (cd.tparams.isEmpty)
      q"(null: ${cd.name})"
    else {
      val tp = cd.tparams//.map { _ => "_" }.mkString(",", "", "")
      q"(null: ${cd.name}[..$tp])"
    }
    c.typeCheck(block, withMacrosDisabled = true).tpe
  }

// code from shapeless
  def mkCompoundTpe[Parent, Nil <: Parent, Cons[_, _ <: Parent] <: Parent](
                                                                            items: List[Type])(implicit
                                                                                               nil: c.WeakTypeTag[Nil],
                                                                                               cons: c.WeakTypeTag[Cons[Any, Nothing]]
                                                                            ): Type = {
    items.foldRight(nil.tpe) {
      case (tpe, acc) =>
        appliedType(cons.tpe, List(tpe, acc))
    }
  }

  def mkHListTpe(items: List[Type]): Type = mkCompoundTpe[HList, HNil, ::](items)
// code from shapeless
}


class TableDef extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro TableDefMacro.implAdd
}

object TableDefMacro {
  def implAdd(ctx: Context)(annottees: ctx.Expr[Any]*): ctx.Expr[Any] = {
    import ctx.universe._

    val helper = new Helper[ctx.type] {
      val c: ctx.type = ctx
    }

      val newDefs: List[Tree] = List(
      q"def x = 5",
      q"def y = 7.0f"
    )

    val inputs = annottees.map(_.tree).toList
    val typs = inputs.map { origTree =>
      println(origTree)
      origTree match {
        case origCl @ q"class $name(..$args) extends $parent with ..$traits { ..$body }" =>
          val dupCl = origCl.duplicate.asInstanceOf[ClassDef]
 // workaround for bug fixed in 2.11.0 and 2.10.5. Also won't work for inner classes!!!!
          val resultType = helper.typeCheckClassDef(dupCl)
//           val resultType = ctx.typeCheck(q"(??? : $dupCl)").tpe
 // workaround for bug fixed in 2.11.0 and 2.10.5. Also won't work for inner classes!!!!



          val itlMembers = resultType.members.filter(m => m.typeSignature <:< typeOf[TableColumn[_]]).toList.reverse
          val itlTypeList = itlMembers.map {m => m.typeSignature}
          val itlTypeParamList = itlMembers.map {t =>
            val typeParam = t.typeSignature.baseType(typeOf[TableColumn[_]].typeSymbol) match {
              case TypeRef(_, _, targ :: Nil) => targ
              case NoType => ctx.abort(ctx.enclosingPosition, "call this method with known type parameter only.")
            }
            typeParam
          }
          val resultColumnsType = helper.mkHListTpe(itlTypeList)
          val resultValuesType = helper.mkHListTpe(itlTypeParamList)

          val resTypeHList = TypeTree(resultColumnsType)
          val resTypeResHList = TypeTree(resultValuesType)

/*
           val addedMembers = List(
            q"type ProjectionType = $resTypeHList",
            q"type SelectType = $resTypeResHList"
          )
*/
          val addingType = typeOf[TableImpl[_, _]].typeSymbol
          val addingTypeParameters = List(resTypeHList, resTypeResHList)
          val addedType = tq"$addingType[..$addingTypeParameters]"
          val addedTypeList: List[Tree] = List(addedType)


          val res = q"class $name(..$args) extends $parent with ..${(traits ++ addedTypeList).toList} { ..${(/*addedMembers ++ */newDefs ++ body).toList} }"
          println(res)
          res
        case x =>
          println("test is there")
          x
      }
    }
    println("test end")
    ctx.Expr[Any](Block(typs, Literal(Constant(()))))
  }
}

class QuasiQuoteAddTrait extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro SimpleTraitImpl.quasiQuotesImpl
}

trait SimpleTrait {
  def x: Int
  def y: Float
}

object SimpleTraitImpl {

  def quasiQuotesImpl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList

    //you need to put the type in explicitly here with quasiquotes
    val newDefs: List[Tree] = List(
      q"def x = 5",
      q"def y = 7.0f"
    )

    val modDefs = inputs map {tree => tree match {
      case q"class $name(..$args) extends $parent with ..$traits { ..$body }"=>
        //again, explicit types everywhere with quasiquotes
        val tbody = body.asInstanceOf[List[Tree]]
        val ttraits = traits.asInstanceOf[List[Tree]]
        val q"class $ignore extends $addedType" = q"class Foo extends ru.simplesys.scajdbc.macros.SimpleTrait"
        val addedTypeList : List[Tree] = List(addedType)
        // and after merging lists together, we need to call .toList again
        println("HI!! Test is successful!!")
        q"class $name(..$args) extends $parent with ..${(ttraits ++ addedTypeList).toList} { ..${(newDefs ++ tbody).toList} }"
      case x =>
        x
    }}
    c.Expr(Block(modDefs, Literal(Constant())))
  }


}
