package com.zhangmin.meetup.api

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import com.zhangmin.meetup.cassandra.RSVPRepository
import com.zhangmin.meetup.config.TrendingTopicsSettings
import com.zhangmin.meetup.model.RSVPDTO
import com.zhangmin.meetup.utils.DateTimeUtils
import org.joda.time.{DateTime, Instant}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TopkService(settings: TrendingTopicsSettings) {
  private val rsvpRepository = RSVPRepository.local(settings.cassandra.keyspace)

  def handleRequest(
    until: Option[String],
    interval: Option[Int],
    count: Option[Int],
    city: Option[String],
    country: Option[String]
  )(implicit ec: ExecutionContext): Future[HttpResponse] =
    resolveTo(until) match {
      case Left(error) => Future.successful(HttpResponse(StatusCodes.BadRequest, entity = error.getMessage))
      case Right(toDateTime) =>
        val date = DateTimeUtils.toDateString(toDateTime.getMillis)
        val from = resolveFrom(toDateTime.getMillis, interval)
        val to = toDateTime
        rsvpRepository
          .getAllInRange(date, from.getMillis, to.getMillis)
          .map { rsvps =>
            val filteredRSVPs = filterByCountry(filterByCity(rsvps, city), country)
            val flattenedTopics = filteredRSVPs
              .flatMap(_.topics.replaceAllLiterally("[", "").replaceAllLiterally("]", "").split(","))
              .toList
            val topCount = count.getOrElse(settings.topkService.defaultTopCount)
            val topK = findTopK(topCount, flattenedTopics)
            HttpResponse(entity = formatOutput(topCount, from, to, city, country, topK))
          }
    }

  private def findTopK(k: Int, topics: List[String]): List[(String, Int)] =
    topics
      .groupBy(identity)
      .map { case (topic, topics) => (topic, topics.size) }
      .toList
      .sortBy(_._2)(Ordering.Int.reverse)
      .take(k)

  private def formatOutput(
    topCount: Int,
    from: DateTime,
    to: DateTime,
    cityFilter: Option[String],
    countryFilter: Option[String],
    output: List[(String, Int)]
  ): String = {
    val filterClause = (cityFilter, countryFilter) match {
      case (None, Some(country)) => s"in $country."
      case (Some(city), _)       => s"in $city."
      case (None, None)          => ""
    }
    s"Top $topCount trending topics between $from and $to $filterClause\n\n${output
      .map { case (topic, count) => s"$topic - [$count hits]" }
      .mkString("\n")}"
  }

  private def resolveTo(toParam: Option[String]): Either[Throwable, DateTime] = toParam match {
    case Some(value) =>
      Try {
        DateTime.parse(value)
      } match {
        case Success(dateTime) => Right(dateTime)
        case Failure(error)    => Left(error)
      }
    case None => Right(DateTime.now())
  }

  private def resolveFrom(to: Long, interval: Option[Int]): DateTime =
    Instant.ofEpochMilli(to - (interval.getOrElse(settings.topkService.defaultInterval) * 1000 * 60)).toDateTime

  private def filterByCity(rsvps: Seq[RSVPDTO], cityFilter: Option[String]): Seq[RSVPDTO] =
    cityFilter.map(city => rsvps.filter(_.city == city)).getOrElse(rsvps)

  private def filterByCountry(rsvps: Seq[RSVPDTO], countryFilter: Option[String]): Seq[RSVPDTO] =
    countryFilter.map(country => rsvps.filter(_.country == country)).getOrElse(rsvps)
}
