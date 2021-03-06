package indigo.shared.datatypes

import indigo.shared.EqualTo
import indigo.shared.EqualTo._

final case class Vector4(x: Double, y: Double, z: Double, w: Double) {

  def withX(newX: Double): Vector4 =
    this.copy(x = newX)

  def withY(newY: Double): Vector4 =
    this.copy(y = newY)

  def withZ(newZ: Double): Vector4 =
    this.copy(z = newZ)

  def withW(newW: Double): Vector4 =
    this.copy(w = newW)

  def translate(vec: Vector4): Vector4 =
    Vector4.add(this, vec)

  def scale(vec: Vector4): Vector4 =
    Vector4.multiply(this, vec)

  def round: Vector4 =
    Vector4(Math.round(x).toDouble, Math.round(y).toDouble, Math.round(z).toDouble, Math.round(w).toDouble)

  def toList: List[Double] =
    List(x, y, z, w)

  def +(other: Vector4): Vector4 = Vector4.add(this, other)
  def -(other: Vector4): Vector4 = Vector4.subtract(this, other)
  def *(other: Vector4): Vector4 = Vector4.multiply(this, other)
  def /(other: Vector4): Vector4 = Vector4.divide(this, other)

  def +(value: Double): Vector4 = Vector4.add(this, Vector4(value, value, value, value))
  def -(value: Double): Vector4 = Vector4.subtract(this, Vector4(value, value, value, value))
  def *(value: Double): Vector4 = Vector4.multiply(this, Vector4(value, value, value, value))
  def /(value: Double): Vector4 = Vector4.divide(this, Vector4(value, value, value, value))

  def dot(other: Vector4): Double =
    Vector4.dotProduct(this, other)

  def normalise: Vector4 =
    Vector4(
      if (x === 0) 0 else (x / Math.abs(x)),
      if (y === 0) 0 else (y / Math.abs(y)),
      if (z === 0) 0 else (z / Math.abs(z)),
      if (w === 0) 0 else (w / Math.abs(w))
    )

  def applyMatrix4(matrix4: Matrix4): Vector4 = Vector4.applyMatrix4(this, matrix4)

  def toVector2: Vector2 =
    Vector2(x, y)

  def toVector3: Vector3 =
    Vector3(x, y, x)

  override def toString: String =
    s"Vector4(x = ${x.toString()}, y = ${y.toString()}, z = ${z.toString()}, w = ${z.toString()})"

  def ===(other: Vector4): Boolean =
    implicitly[EqualTo[Vector4]].equal(this, other)
}

object Vector4 {

  implicit val eq: EqualTo[Vector4] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y) && ev.equal(a.z, b.z) && ev.equal(a.w, b.w)
    }
  }

  def apply(i: Int): Vector4 =
    Vector4(i.toDouble, i.toDouble, i.toDouble, i.toDouble)

  val zero: Vector4 = Vector4(0d, 0d, 0d, 0d)
  val one: Vector4  = Vector4(1d, 1d, 1d, 1d)

  @inline def add(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z, vec1.w + vec2.w)

  @inline def subtract(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z, vec1.w - vec2.w)

  @inline def multiply(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z, vec1.w * vec2.w)

  @inline def divide(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z, vec1.w / vec2.w)

  def position(x: Double, y: Double, z: Double): Vector4 =
    Vector4(x, y, z, 1)

  def direction(x: Double, y: Double, z: Double): Vector4 =
    Vector4(x, y, z, 0)

  def dotProduct(vec1: Vector4, vec2: Vector4): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y) + (vec1.z * vec2.z) + (vec1.w * vec2.w)

  def applyMatrix4(vector4: Vector4, matrix4: Matrix4): Vector4 = {
    val m  = matrix4.transpose
    val vl = vector4.toList

    Vector4(
      x = m.row1.zip(vl).map(p => p._1 * p._2).sum,
      y = m.row2.zip(vl).map(p => p._1 * p._2).sum,
      z = m.row3.zip(vl).map(p => p._1 * p._2).sum,
      w = m.row4.zip(vl).map(p => p._1 * p._2).sum
    )
  }

}
