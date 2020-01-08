package indigo.platform

import indigo.shared.platform.Platform
import indigo.shared.platform.Renderer
import indigo.shared.platform.GlobalEventStream
import indigo.shared.GameContext
import indigo.shared.config.GameConfig
import indigo.shared.IndigoLogger
import indigo.shared.datatypes.Vector2
import indigo.platform.renderer.RendererInit
import indigo.platform.renderer.LoadedTextureAsset
import indigo.shared.platform.RendererConfig
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.TextureRefAndOffset
import indigo.platform.events.WorldEvents
import indigo.platform.assets.AssetCollection
import indigo.platform.assets.TextureAtlas
import indigo.platform.assets.TextureAtlasFunctions
import indigo.platform.assets.ImageRef

import indigo.shared.EqualTo._

import org.scalajs.dom
import org.scalajs.dom.html.Canvas

class PlatformImpl(assetCollection: AssetCollection, globalEventStream: GlobalEventStream) extends Platform {

  import PlatformImpl._

  def initialiseRenderer(gameConfig: GameConfig): GameContext[(Renderer, AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      canvas              <- createCanvas(gameConfig)
      _                   <- listenToWorldEvents(canvas, gameConfig.magnification, globalEventStream)
      renderer            <- startRenderer(gameConfig, loadedTextureAssets, canvas)
    } yield (renderer, assetMapping)

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def tick(loop: Long => Unit): Unit = {
    dom.window.requestAnimationFrame(t => loop(t.toLong))
    ()
  }

}

object PlatformImpl {
  def createTextureAtlas(assetCollection: AssetCollection): GameContext[TextureAtlas] =
    GameContext(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name.name, i.data.width, i.data.height)),
        (name: String) => assetCollection.images.find(_.name.name === name),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): GameContext[List[LoadedTextureAsset]] =
    GameContext(
      textureAtlas.atlases.toList
        .map(a => a._2.imageData.map(data => new LoadedTextureAsset(a._1.id, data)))
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): GameContext[AssetMapping] =
    GameContext(
      new AssetMapping(
        mappings = textureAtlas.legend
          .map { p =>
            p._1 -> new TextureRefAndOffset(
              atlasName = p._2.id.id,
              atlasSize = textureAtlas.atlases.get(p._2.id).map(_.size.value).map(Vector2.apply).getOrElse(Vector2.one),
              offset = p._2.offset
            )
          }
      )
    )

  def createCanvas(gameConfig: GameConfig): GameContext[Canvas] =
    Option(dom.document.getElementById("indigo-container")) match {
      case None =>
        GameContext.raiseError[Canvas](new Exception("""Parent element "indigo-container" could not be found on page."""))

      case Some(parent) =>
        GameContext(RendererInit.createCanvas(gameConfig.viewport.width, gameConfig.viewport.height, parent))
    }

  def listenToWorldEvents(canvas: Canvas, magnification: Int, globalEventStream: GlobalEventStream): GameContext[Unit] = {
    IndigoLogger.info("Starting world events")
    GameContext(WorldEvents.init(canvas, magnification, globalEventStream))
  }

  def startRenderer(gameConfig: GameConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: Canvas): GameContext[Renderer] =
    GameContext {
      IndigoLogger.info("Starting renderer")
      RendererInit(
        new RendererConfig(
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification,
          maxBatchSize = gameConfig.advanced.batchSize,
          antiAliasing = gameConfig.advanced.antiAliasing
        ),
        loadedTextureAssets,
        canvas
      )
    }

}
