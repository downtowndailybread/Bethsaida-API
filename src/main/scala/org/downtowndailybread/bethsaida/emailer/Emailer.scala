package org.downtowndailybread.bethsaida.emailer

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials, PropertiesCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.{Body, Content, Destination, Message, SendEmailRequest}
import org.downtowndailybread.bethsaida.Settings


object Emailer {

  def main(args: Array[String]): Unit = {
    val settings = new Settings(args)

    val to = args(1)
    val subject = args(2)
    val content = args(3)
    Emailer.sendEmail(to, subject, content, settings)

  }

  def sendEmail(to: String, subject: String, content: String, settings: Settings) {
    try {
      val client = AmazonSimpleEmailServiceClientBuilder.standard()
        .withCredentials(
          new AWSStaticCredentialsProvider(new BasicAWSCredentials(
            settings.awsAccess, settings.awsSecret
          ))
        )
        .withRegion(Regions.US_EAST_1)
        .build()
      val request = new SendEmailRequest()
        .withDestination(new Destination().withToAddresses(settings.emailFrom))
        .withReturnPath(settings.emailFrom)
        .withMessage(
          new Message()
            .withBody(new Body()
              .withHtml(new Content()
                .withCharset("UTF-8").withData(content))
              .withText(new Content().withCharset("UTF-8").withData(content)))
            .withSubject(new Content().withCharset("UTF-8").withData(subject))
        ).withSource(settings.emailFrom)

      client.sendEmail(request)
    } catch {
      case e => throw e
    }
  }
}
