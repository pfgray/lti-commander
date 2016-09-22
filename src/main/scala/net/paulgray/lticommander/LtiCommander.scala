package net.paulgray.lticommander

import java.io.{FileInputStream, File}
import java.net.URI
import java.util.Scanner

import com.github.jsonldjava.core.{JsonLdOptions, JsonLdProcessor, JsonLdUtils}
import com.github.jsonldjava.utils.JsonUtils
import org.apache.commons.io.IOUtils
import org.apache.http.entity.StringEntity
import org.apache.http.{HttpEntity, HttpRequest}
import org.apache.http.client.methods.{HttpUriRequest, HttpEntityEnclosingRequestBase, HttpPost, HttpGet}
import org.apache.http.impl.client.HttpClientBuilder
import org.imsglobal.lti.launch.LtiOauthSigner
import pl.project13.scala.rainbow.Rainbow._
import scopt.OptionParser
import scala.collection.JavaConverters._

/**
  * Created by paul on 7/20/16.
  */
object LtiCommander {

  val parser = new OptionParser[LtiCommanderOptions]("lti-commander") {
    opt[String]('m', "method") action { (method, options) =>
      options.copy(method = method)
    }

    opt[Unit]('l', "local") action { (_, c) =>
      c.copy(local = true)
    }

    opt[String]('k', "key") action { (key, options) =>
      options.copy(key = key)
    }

    opt[String]('s', "secret") action { (secret, options) =>
      options.copy(secret = secret)
    }

    opt[String]("jsonld") action { (_, options) =>
      options.copy(jsonLd = true)
    }

    arg[String]("<url>").required() action { (url, options) =>
      options.copy(url = url)
    }

    arg[String]("<body>").optional() action { (body, options) =>
      options.copy(body = Option(body))
    }
  }

  def main(args: Array[String]): Unit = {
    val options = parser
      .parse(args, LtiCommanderOptions())
      .getOrElse({
        println(s"Oops, looks like you forgot to include a url!")
        sys.exit(1)
      })

    //println(s"Using options: ${options}".onMagenta)

    if(options.jsonLd){
      val context = new java.util.HashMap()
      val json = JsonUtils.fromInputStream(getInputStreamForOptions(options))
      val jsonLdOptions = new JsonLdOptions()

      val compact = JsonLdProcessor.compact(json, context, jsonLdOptions)
      println("\ncompact:".yellow)
      println(JsonUtils.toPrettyString(compact).yellow)

      val flattened = JsonLdProcessor.flatten(json, context, jsonLdOptions)
      println("\flattened:".blue)
      println(JsonUtils.toPrettyString(flattened).blue)

      val expanded = JsonLdProcessor.expand(json, jsonLdOptions)
      println("\nexpanded:".magenta)
      println(JsonUtils.toPrettyString(expanded).magenta)
    } else {
      val body = IOUtils.toString(getInputStreamForOptions(options), "UTF-8")

      println(body.blue)

    }


    //IOUtils.copy(response.getEntity.getContent, System.out)
  }

  def green(s: String) = s.green
  def red(s: String) = s.red

  def getHttpRequestForOptions(options: LtiCommanderOptions): HttpUriRequest = {
    options.method match {
      case "GET" =>
        val req = new HttpGet(new URI(options.url))
        req.setHeader("Accept", "application/vnd.ims.lis.v2.lineitemcontainer+json")
        req
      case "POST" =>
        val req = new HttpPost(new URI(options.url))
        req.setHeader("Content-Type", "application/vnd.ims.lis.v2.lineitem+json")
        req.setEntity(new StringEntity(getInput()))
        req
    }
  }

  def getInput(): String = {
    var lines: Seq[String] = Seq()
    val sc = new Scanner(System.in)
    while(sc.hasNext()){
      lines = lines ++ Seq(sc.nextLine())
    }
    lines.reduce(_ + _)
  }

  def getInputStreamForOptions(options: LtiCommanderOptions) = {
    if(!options.local) {
      val request = getHttpRequestForOptions(options)

      val ltiSigner = new LtiOauthSigner()
      ltiSigner.sign(request, options.key, options.secret)

      println(
        s"""
           |Request:
           |${options.method} ${options.url.magenta}
           |${request.getAllHeaders.map(h => ("\u001b[37m" + h.getName + ":") +  h.getValue.cyan).reduce(_ + "\n" + _)}
           |""".stripMargin)

      val client = HttpClientBuilder.create().build()
      val response = client.execute(request)

      val color = if (response.getStatusLine.getStatusCode >= 400) {
        red _
      } else {
        green _
      }

      println(color(
        s"""
           |Received ${response.getStatusLine}
           |Body:""".stripMargin))
      response.getEntity.getContent
    } else {
      println("Reading file locally...".red.blink)
      //read in the file..
      new FileInputStream(options.url)
    }
  }

}
