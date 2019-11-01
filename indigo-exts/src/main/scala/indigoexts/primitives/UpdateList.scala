package indigoexts.primitives

import scala.collection.mutable.ListBuffer

final class UpdateList[A](list: List[A], pattern: UpdatePattern) {

  def update(f: A => A): UpdateList[A] = {
    val (v, p) = UpdateList.updateList(list, f, pattern)

    new UpdateList(v, p)
  }

  def withPattern(newPattern: UpdatePattern): UpdateList[A] =
    new UpdateList[A](list, newPattern)

  def toList: List[A] =
    list
}

object UpdateList {

  def apply[A](l: List[A]): UpdateList[A] =
    new UpdateList(
      l,
      UpdatePattern.Constant
    )

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var", "org.wartremover.warts.MutableDataStructures"))
  def updateList[A](l: List[A], f: A => A, pattern: UpdatePattern): (List[A], UpdatePattern) = {
    var i: Int             = 0
    val res: ListBuffer[A] = new ListBuffer[A]

    while (i < l.length) {
      val v = l(i)
      res.insert(i, pattern.update(v, f, v, i))

      i = i + 1
    }

    (res.toList, pattern.step)
  }

}
