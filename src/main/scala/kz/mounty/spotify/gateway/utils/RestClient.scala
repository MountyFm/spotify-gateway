package kz.mounty.spotify.gateway.utils

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import kz.mounty.fm.exceptions.{ErrorCodes, ServerErrorRequestException}
import org.json4s.jackson.JsonMethods.parse

import java.security.cert.X509Certificate
import javax.net.ssl.{KeyManager, SSLContext, X509TrustManager}
import scala.concurrent.{ExecutionContext, Future, Promise}

trait RestClient extends LocalSerializer with MountyEndpoint {
  def makePostRequest[T: Manifest](uri: String,
                                   headers: List[HttpHeader],
                                   body: Option[String])
                                  (implicit system: ActorSystem,
                                   ec: ExecutionContext,
                                   mat: Materializer): Future[T] = {
    val p = Promise[T]

    val entity = if(body.isDefined) {
      HttpEntity(
        ContentType(MediaTypes.`application/json`),
        body.get
      )
    } else {
      HttpEntity.Empty
    }

    Http()
      .singleRequest(
        request = HttpRequest(
          uri = uri,
          method = HttpMethods.POST,
          headers = headers,
          entity = entity,
          protocol = HttpProtocols.`HTTP/1.1`
        ),
        getNoCertificateCheckContext
      ).flatMap(httpResponse => handleResponse(p, httpResponse))
  }

  def makePutRequest[T: Manifest](uri: String,
                                  headers: List[HttpHeader],
                                  body: Option[String])
                                 (implicit system: ActorSystem,
                                  ec: ExecutionContext,
                                  mat: Materializer): Future[T] = {
    val p = Promise[T]

    val entity = if(body.isDefined) {
      HttpEntity(
        ContentType(MediaTypes.`application/json`),
        body.get
      )
    } else {
      HttpEntity.Empty
    }

    Http()
      .singleRequest(
        request = HttpRequest(
          uri = uri,
          method = HttpMethods.PUT,
          headers = headers,
          entity = entity,
          protocol = HttpProtocols.`HTTP/1.1`
        ),
        getNoCertificateCheckContext
      ).flatMap(httpResponse => handleResponse(p, httpResponse))
  }

  def makeGetRequest[T: Manifest](uri: String,
                                  headers: List[HttpHeader])
                                 (implicit system: ActorSystem,
                                  ec: ExecutionContext,
                                  mat: Materializer): Future[T] = {
    val p = Promise[T]

    Http()
      .singleRequest(
        request = HttpRequest(
          uri = uri,
          method = HttpMethods.GET,
          headers = headers,
          protocol = HttpProtocols.`HTTP/1.1`
        ),
        getNoCertificateCheckContext
      ).flatMap(httpResponse => handleResponse(p, httpResponse))
  }

  private val trustfulSslContext: SSLContext = {

    object NoCheckX509TrustManager extends X509TrustManager {
      override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {}

      override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {}

      override def getAcceptedIssuers: Array[X509Certificate] = Array[X509Certificate]()
    }
    val context = SSLContext.getInstance("TLS")
    context.init(Array[KeyManager](), Array(NoCheckX509TrustManager), null)
    context
  }

  def getNoCertificateCheckContext(implicit ec: ExecutionContext,
                                   system: ActorSystem,
                                   mat: Materializer): HttpsConnectionContext = {
    val badSSLConfig = AkkaSSLConfig().mapSettings(s => s.withLoose(s.loose.withDisableHostnameVerification(true)))
    ConnectionContext.https(trustfulSslContext, Some(badSSLConfig))
  }

  def getAuthorizationHeaders(accessToken: String)(implicit config: Config): List[HttpHeader] = {
    List(
      Authorization(OAuth2BearerToken(s"${accessToken}")),
    )
  }

  def handleResponse[T: Manifest](p: Promise[T], httpResponse: HttpResponse)
                                 (implicit mat: Materializer, ex: ExecutionContext): Future[T] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        if (httpResponse.entity.contentType == ContentTypes.`application/json`) {
          Unmarshal(httpResponse.entity).to[String].map { jsonString =>
            p.success(parse(jsonString).camelizeKeys.extract[T])
          } recover {
            case e: Throwable =>
              p.failure(
                ServerErrorRequestException(
                  ErrorCodes.INTERNAL_SERVER_ERROR(errorSeries),
                  Some(s"Parse error: $e")
                )
              )
          }
        }
      case e =>
        p.failure(
          ServerErrorRequestException(
            ErrorCodes.INTERNAL_SERVER_ERROR(errorSeries),
            Some(s"Http error response: ${httpResponse.status}")
          )
        )
    }
    p.future
  }
}
