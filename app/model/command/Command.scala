package model.command

trait Command[T] {

  def process: Option[T]

}
