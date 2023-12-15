import play.api.ApplicationLoader.Context
import play.api.{Application, ApplicationLoader, LoggerConfigurator}
import services.Config

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {

    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    new AppComponents(context, Config()).application
  }
}
