import javax.sound.sampled.{AudioFormat, AudioInputStream, AudioSystem}

object Micro {
  private val format = buildAudioFormatInstance
  private val microphone = AudioSystem.getTargetDataLine(format)
  private val sos = AudioSystem.getSourceDataLine(format)


  private def buildAudioFormatInstance: AudioFormat = {
    val sampleRate = 48000
    val sampleSizeInBits = 16
    val channels = 2
    val signed = true
    val bigEndian = true
    new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)
  }

  private def start(): Unit = {
    if (!AudioSystem.isLineSupported(microphone.getLineInfo)) {
      println("Line not supported")
      System.exit(0)
    }

    microphone.open(format)
    microphone.start()

    sos.open(format)
    sos.start()

    println("Start")
  }

  private def stop(): Unit = {
    microphone.stop()
    microphone.close()
    println("Finished")
  }

  def main(args: Array[String]): Unit = {
    val stopper = new Thread(Stopper)

    stopper.start()
    start()
  }

  private object Stopper extends Runnable {
    override def run(): Unit = {
      while (true) {
        val b = new Array[Byte](format.getSampleSizeInBits)
        microphone.read(b, 0, b.length)
        sos.write(b, 0, b.length)
      }
    }
  }
}
