package powerapi.core

class Message
trait ListeningMessages {
  def messagesToListen: Array[Message]
}

abstract class EnergyModule extends ListeningMessages
abstract class Sensor extends EnergyModule
abstract class Formula extends EnergyModule