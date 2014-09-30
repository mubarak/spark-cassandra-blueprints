package com.helenaedelson.blueprints.api

object ApiData {

  class UserId private (val value: String) extends AnyVal {
    override def toString: String = s"Id($value)"
  }

  object UserId {
    import javax.servlet.http.HttpServletRequest

    import scalaz._

    val HttpHeader = "X-BLUEPRINT-ID"

    def apply(value: String): Validation[String, UserId] =
      if (regex.pattern.matcher(value).matches) Success(new UserId(value.toLowerCase))
      else Failure(s"invalid Id '$value'")

    def apply(request: HttpServletRequest): Option[Validation[String, UserId]] =
      Option(request.getHeader(HttpHeader)) map (id => UserId(id))

    private val regex = """[0-9a-f]{32}""".r
  }
}
