package net.paulgray.lticommander

import java.io.File

import org.apache.http.HttpRequest


/**
  * Created by paul on 7/20/16.
  */
case class LtiCommanderOptions(
  local: Boolean = false,
  url: String = "",
  method: String = "GET",
  body: Option[String] = None,
  key: String = "key",
  secret: String = "secret",
  jsonLd: Boolean = false
)
