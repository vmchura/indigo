package indigo.gameengine.scenegraph.datatypes

import indigo.shared.EqualTo

import indigo.shared.EqualTo._

final case class FontInfo(fontKey: FontKey, fontSpriteSheet: FontSpriteSheet, unknownChar: FontChar, fontChars: List[FontChar], caseSensitive: Boolean) {
  private val nonEmptyChars: List[FontChar] = unknownChar +: fontChars

  def addChar(fontChar: FontChar): FontInfo     = FontInfo(fontKey, fontSpriteSheet, fontChar, nonEmptyChars, caseSensitive)
  def addChars(chars: List[FontChar]): FontInfo = this.copy(fontChars = fontChars ++ chars)
  def addChars(chars: FontChar*): FontInfo      = this.copy(fontChars = fontChars ++ chars)

  def findByCharacter(character: String): FontChar =
    nonEmptyChars
      .find { p =>
        if (caseSensitive) p.character === character else p.character.toLowerCase === character.toLowerCase
      }
      .getOrElse(unknownChar)
  def findByCharacter(character: Char): FontChar = findByCharacter(character.toString)

  def makeCaseSensitive(sensitive: Boolean): FontInfo = this.copy(caseSensitive = sensitive)
  def isCaseSensitive: FontInfo                       = makeCaseSensitive(true)
  def isCaseInSensitive: FontInfo                     = makeCaseSensitive(false)
}

object FontInfo {
  def apply(fontKey: FontKey, imageAssetRef: String, sheetWidth: Int, sheetHeight: Int, unknownChar: FontChar, chars: FontChar*): FontInfo =
    FontInfo(
      fontKey = fontKey,
      fontSpriteSheet = FontSpriteSheet(imageAssetRef, Point(sheetWidth, sheetHeight)),
      unknownChar = unknownChar,
      fontChars = chars.toList,
      caseSensitive = false
    )
}

final case class FontKey(key: String) extends AnyVal
object FontKey {

  implicit class EqualToFontKey(value: FontKey) extends EqualTo[FontKey] {
    def ===(other: FontKey): Boolean =
      equal(value, other)

    def !==(other: FontKey): Boolean =
      !equal(value, other)

    def equal(a1: FontKey, a2: FontKey): Boolean =
      implicitly[EqualTo[String]].equal(a1.key, a2.key)
  }

}

final case class FontSpriteSheet(imageAssetRef: String, size: Point)
final case class FontChar(character: String, bounds: Rectangle)
object FontChar {
  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar =
    FontChar(character, Rectangle(x, y, width, height))
}

sealed trait TextAlignment
object TextAlignment {
  case object Left   extends TextAlignment
  case object Center extends TextAlignment
  case object Right  extends TextAlignment
}
