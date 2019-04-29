package indigoexts.geometry

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point

final class LineSegment(val start: Point, val end: Point) {
  val center: Point = end - start

  def left: Int   = Math.min(start.x, end.x)
  def right: Int  = Math.max(start.x, end.x)
  def top: Int    = Math.min(start.y, end.y)
  def bottom: Int = Math.max(start.y, end.y)

  def normal: Point =
    LineSegment.calculateNormal(start, end)

  def lineProperties: LineProperties =
    LineSegment.calculateLineComponents(start, end)

  def intersectWith(other: LineSegment): IntersectionResult =
    LineSegment.intersection(this, other)

  def containsPoint(point: Point): Boolean =
    LineSegment.lineContainsPoint(this, point)
}

object LineSegment {

  implicit val equalTo: EqualTo[LineSegment] = {
    val eqPt = implicitly[EqualTo[Point]]

    EqualTo.create { (a, b) =>
      eqPt.equal(a.start, b.start) && eqPt.equal(a.end, b.end)
    }
  }

  def apply(start: Point, end: Point): LineSegment =
    new LineSegment(start, end)

  def apply(x1: Int, y1: Int, x2: Int, y2: Int): LineSegment =
    LineSegment(Point(x1, y1), Point(x2, y2))

  def apply(start: (Int, Int), end: (Int, Int)): LineSegment =
    LineSegment(Point.tuple2ToPoint(start), Point.tuple2ToPoint(end))

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  def calculateLineComponents(start: Point, end: Point): LineProperties =
    (start, end) match {
      case (Point(x1, y1), Point(x2, y2)) if x1 === x2 && y1 === y2 =>
        LineProperties.InvalidLine

      case (Point(x1, _), Point(x2, _)) if x1 === x2 =>
        LineProperties.ParallelToAxisY

      case (Point(_, y1), Point(_, y2)) if y1 === y2 =>
        LineProperties.ParallelToAxisX

      case (Point(x1, y1), Point(x2, y2)) =>
        val m: Float = (y2.toFloat - y1.toFloat) / (x2.toFloat - x1.toFloat)

        LineProperties.LineComponents(m, y1 - (m * x1))
    }

  def intersection(l1: LineSegment, l2: LineSegment): IntersectionResult =
    /*
    y-intercept = mx + b (i.e. y = mx + b)
    x-intercept = -b/m   (i.e. x = -b/m where y is moved to 0)
     */
    (l1.lineProperties, l2.lineProperties) match {
      case (LineProperties.LineComponents(m1, b1), LineProperties.LineComponents(m2, b2)) =>
        //x = -b/m
        val x: Float = (b2 - b1) / (m1 - m2)

        //y = mx + b
        val y: Float = (m1 * x) + b1

        IntersectionResult.IntersectionPoint(x, y)

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisX) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisY) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionPoint(l2.start.x.toFloat, l1.start.y.toFloat)

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionPoint(l1.start.x.toFloat, l2.start.y.toFloat)

      case (LineProperties.ParallelToAxisX, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionPoint(
          x = (-b / m) - l1.start.y.toFloat,
          y = l1.start.y.toFloat
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionPoint(
          x = (-b / m) - l2.start.y.toFloat,
          y = l2.start.y.toFloat
        )

      case (LineProperties.ParallelToAxisY, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionPoint(
          x = l1.start.x.toFloat,
          y = (m * l1.start.x) + b
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionPoint(
          x = l2.start.x.toFloat,
          y = (m * l2.start.x) + b
        )

      case (LineProperties.InvalidLine, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (_, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (LineProperties.InvalidLine, _) =>
        IntersectionResult.NoIntersection

      case _ =>
        IntersectionResult.NoIntersection
    }

  def calculateNormal(start: Point, end: Point): Point =
    normalisePoint(Point(-(end.y - start.y), end.x - start.x))

  def normalisePoint(point: Point): Point = {
    val x: Double = point.x.toDouble
    val y: Double = point.y.toDouble

    Point((x / Math.abs(x)).toInt, (y / Math.abs(y)).toInt)
  }

  def lineContainsPoint(lineSegment: LineSegment, point: Point): Boolean =
    lineContainsPoint(lineSegment, point, 0.01f)

  def lineContainsPoint(lineSegment: LineSegment, point: Point, tolerance: Float): Boolean =
    lineSegment.lineProperties match {
      case LineProperties.InvalidLine =>
        false

      case LineProperties.ParallelToAxisX =>
        if (point.y === lineSegment.start.y && point.x >= lineSegment.left && point.x <= lineSegment.right) true
        else false

      case LineProperties.ParallelToAxisY =>
        if (point.x === lineSegment.start.x && point.y >= lineSegment.top && point.y <= lineSegment.bottom) true
        else false

      case LineProperties.LineComponents(m, b) =>
        if (point.x >= lineSegment.left && point.x <= lineSegment.right && point.y >= lineSegment.top && point.y <= lineSegment.bottom) {
          // This is a slope comparison.. Any point on the line should have the same slope as the line.
          val m2: Float =
            if (point.x === 0) 0
            else (b - point.y.toFloat) / (0 - point.x.toFloat)

          val mDelta: Float = m - m2

          mDelta >= -tolerance && mDelta <= tolerance
        } else false
    }

}

sealed trait LineProperties
object LineProperties {
// y = mx + b
  final case class LineComponents(m: Float, b: Float) extends LineProperties
  case object ParallelToAxisX                         extends LineProperties
  case object ParallelToAxisY                         extends LineProperties
  case object InvalidLine                             extends LineProperties
}

sealed trait IntersectionResult
object IntersectionResult {
  final case class IntersectionPoint(x: Float, y: Float) extends IntersectionResult {
    def toPoint: Point =
      Point(x.toInt, y.toInt)
  }
  case object NoIntersection extends IntersectionResult
}