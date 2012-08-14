package org.scalatra
package databinding
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat, DateTimeFormatter}
import scala.util.control.Exception._
import org.joda.time.{DateTimeZone, DateTime}
import org.scalatra.util.RicherString._
import java.text.{SimpleDateFormat, ParseException}
import java.util.{Locale, TimeZone}

trait DateParser {
  def parse(s: String): Option[DateTime]
  def unapply(s: String) = parse(s)
}

object JodaDateFormats extends DateParser {
  DateTimeZone.setDefault(DateTimeZone.UTC)


  val Web: DateParser = JodaDateFormats(Iso8601NoMillis, Iso8601, HttpDate)


  def parse(s: String) = Web.parse(s)

  def apply(f: DateFormat*): DateParser = new DateParser {
    def parse(s: String) = f.toList.foldLeft (None: Option[DateTime]) { (r, f) ⇒ if (!r.isDefined) f.parse(s) else r }
  }

  trait DateFormat extends DateParser {
    def dateTimeFormat: DateTimeFormatter

    def parse(s: String) = {
      s.blankOption flatMap { s ⇒
        catching(classOf[IllegalArgumentException]) opt {
          dateTimeFormat parseDateTime s
        }
      }
    }
  }

  object Iso8601 extends DateFormat {
    val dateTimeFormat = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  }

  object Iso8601NoMillis extends DateFormat {
    val dateTimeFormat = ISODateTimeFormat.dateTimeNoMillis.withZone(DateTimeZone.UTC)
  }

  object HttpDate extends DateFormat {
    val pattern = "EEE, dd MMM yyyy HH:mm:ss zzz"
    val dateTimeFormat = DateTimeFormat.forPattern(pattern)

    override def parse(s: String) = {
      s.blankOption flatMap { s ⇒
        allCatch opt {
          // I couldn't get this to work with joda time
          // going round by simpledateformat.
          val df = new SimpleDateFormat(pattern, Locale.ENGLISH)
          df.setTimeZone(DateTimeZone.getDefault.toTimeZone)
          df.setLenient(true)
          new DateTime(df.parse(s))
        }
      }
    }
  }

}