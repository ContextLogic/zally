package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReviewRepository
import de.zalando.zally.exception.TimeParameterIsInTheFutureException
import de.zalando.zally.exception.InsufficientTimeIntervalParameterException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@CrossOrigin
@RestController
class ReviewStatisticsController @Autowired
constructor(private val apiReviewRepository: ApiReviewRepository) {

    @ResponseBody
    @GetMapping("/review-statistics")
    fun retrieveReviewStatistics(
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @RequestParam(value = "user_agent", required = false) userAgent: String?
    ): ReviewStatistics {

        if (from != null && from.isAfter(today())) {
            throw TimeParameterIsInTheFutureException()
        }

        if (to != null && from == null) {
            throw InsufficientTimeIntervalParameterException()
        }

        val apiReviews = if (from != null) {
            apiReviewRepository.findByDayBetween(userAgent, from, to ?: today())
        } else {
            apiReviewRepository.findFromLastWeek(userAgent)
        }

        LOG.info("Found {} api reviews from {} to {} user_agent {}", apiReviews.size, from, to, userAgent)
        return ReviewStatistics(apiReviews)
    }

    private fun today(): LocalDate {
        return Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewStatisticsController::class.java)
    }
}