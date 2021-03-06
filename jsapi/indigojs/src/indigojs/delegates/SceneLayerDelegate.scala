package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneLayer

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneLayer")
final class SceneLayerDelegate(_nodes: js.Array[SceneGraphNodeDelegate], _tint: RGBADelegate, _saturation: Double, _magnification: Option[Int]) {

  @JSExport
  val nodes = _nodes
  @JSExport
  val tint = _tint
  @JSExport
  val saturation = _saturation
  @JSExport
  val magnification = _magnification

  @JSExport
  def addLayerNodes(newNodes: js.Array[SceneGraphNodeDelegate]): SceneLayerDelegate =
    new SceneLayerDelegate(nodes ++ newNodes, tint, saturation, magnification)

  @JSExport
  def withTint(newTint: RGBADelegate): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, newTint, saturation, magnification)

  @JSExport
  def withSaturationLevel(amount: Double): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, amount, magnification)

  @JSExport
  def withMagnification(level: Int): SceneLayerDelegate =
    new SceneLayerDelegate(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))

  @JSExport
  def concat(other: SceneLayerDelegate): SceneLayerDelegate = {
    val newSaturation: Double =
      (saturation, other.saturation) match {
        case (1d, b) => b
        case (a, 1d) => a
        case (a, b)  => Math.min(a, b)
      }

    new SceneLayerDelegate(nodes ++ other.nodes, tint.concat(other.tint), newSaturation, magnification.orElse(other.magnification))
  }


  def toInternal: SceneLayer =
    SceneLayer(nodes.map(_.toInternal).toList, tint.toInternal, saturation, magnification)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneLayerHelper")
@JSExportAll
object SceneLayerDelegate {

  def None: SceneLayerDelegate =
    new SceneLayerDelegate(new js.Array(), RGBADelegate.None, 1.0d, Option.empty[Int])

}
