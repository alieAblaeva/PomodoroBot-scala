import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl.Source
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.api.{Polling, RequestHandler}
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class PomodoroBot(val token: String)(implicit system: ActorSystem) 
  extends TelegramBot 
  with Polling 
  with Commands[Future] {

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  override val client: RequestHandler[Future] = new ScalajHttpClient(token)  

  private var timers: Map[Long, Cancellable] = Map()

  onCommand("/start") { implicit msg =>
    reply("Welcome to the Pomodoro bot! Type /pomodoro to start a 25-minute session.").map(_ => ())
  }

  onCommand("/pomodoro") { implicit msg =>
    val chatId = msg.chat.id
    if (timers.contains(chatId)) {
      reply("You already have a Pomodoro session running.").map(_ => ())
    } else {
      reply("Starting your 25-minute Pomodoro session. Stay focused!").map { _ =>
        val cancellable = system.scheduler.scheduleOnce(25.minutes) {
          sendMessage(chatId, "Pomodoro session complete! Time for a 5-minute break.")
          timers = timers - chatId
        }
        timers = timers + (chatId -> cancellable)
      }.map(_ => ())
    }
  }

  onCommand("/stop") { implicit msg =>
    val chatId = msg.chat.id
    timers.get(chatId).foreach { cancellable =>
      cancellable.cancel()
      timers = timers - chatId
      reply("Pomodoro session stopped.").map(_ => ())
    }
    Future.successful(())
  }

  onCommand("/help") { implicit msg =>
    reply(
      """
        |Available commands:
        |/start - Start the bot
        |/pomodoro - Start a 25-minute Pomodoro session
        |/stop - Stop the current Pomodoro session
        |/help - Show help
      """.stripMargin).map(_ => ())
  }

  private def sendMessage(chatId: Long, text: String): Future[Message] = {
    request(SendMessage(chatId, text))
  }
}

object PomodoroBotApp extends App {
  implicit val system: ActorSystem = ActorSystem("PomodoroBotSystem")
  val token = "" //tut mog byt vash token  
  val bot = new PomodoroBot(token)
  bot.run()
  Await.result(system.whenTerminated, Duration.Inf)
}
