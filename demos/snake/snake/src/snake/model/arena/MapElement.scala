package snake.model.arena

import snake.model.grid.GridPoint

sealed trait MapElement {
  val gridPoint: GridPoint
}

object MapElement {
  case class Wall(gridPoint: GridPoint)  extends MapElement
  case class Apple(gridPoint: GridPoint) extends MapElement
}
