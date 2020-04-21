package com.e1sordo.sinamicsemulator

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

data class ConnectRequest(val rack: String, val slot: String)

data class ReadWriteRequest(val param: Int, val index: Int, val value: String)

interface Response {
    val status: Int
}

data class SimpleResponse(override val status: Int = 1) : Response

data class ReadWriteResponse(override val status: Int = 0, val value: String) : Response

data class MessageResponse(val result: String, val message: String? = null, override val status: Int = 1) : Response

data class ValuesResponse(val values: List<Long>, override val status: Int = 1) : Response


@RestController
class ServiceController(
        private val repository: DriveParameterRepository,
        private val metrics: Map<Int, List<String>>
) {

    @PostMapping("/connect")
    fun connect(@RequestBody request: ConnectRequest): Response =
            SimpleResponse(if (request == ConnectRequest("0", "2")) 0 else 1)


    val zeroMinutes = 0..60 step 3
    val plus60kMinutes = 1..60 step 3
    val plus30kMinutes = 2..60 step 3


    @PostMapping("/read")
    fun readParam(@RequestBody request: ReadWriteRequest): Response {

        TimeUnit.MILLISECONDS.sleep((10..30).random().toLong())

        val metricIndex = getMetricIndexDependOnCurrentTime()

        val item: DriveParameter
        val finalValue: String
        if (request.param in arrayOf(35, 63, 68, 80) && request.index == 0) {
            val metricType = when(request.param) {
                35 -> 3
                63 -> 1
                68 -> 0
                80 -> 2
                else -> 1
            }

            finalValue = metrics[metricIndex]!![metricType]
        } else {
            val value = 37.6 + if (LocalDateTime.now().second < 33) Math.random() * 3 else (-Math.random())

            item = DriveParameter(value = "$value", dataType = "Float")
            finalValue = item.value
        }

        return if (finalValue == "") {
            ReadWriteResponse(10, "")
        } else {
            ReadWriteResponse(0, packData(finalValue, "Float"))
        }
    }

    private fun getMetricIndexDependOnCurrentTime(): Int {
        val currentTime = LocalDateTime.now()
        val currentMinutes = currentTime.minute
        val currentSeconds = currentTime.second
        val currentMillis = currentTime.nano / 1000000
        val currentMillisEven = if (currentMillis % 2 == 0) currentMillis else currentMillis + 1

        val base = when (currentMinutes) {
            in plus60kMinutes -> 60000
            in plus30kMinutes -> 30000
            in zeroMinutes -> 0
            else -> 0
        }
        val metricIndex = base + currentSeconds * 1000 + currentMillisEven

        val metricIndexCorrected = if (metricIndex > 89998) metricIndex - 89998 else metricIndex
        return metricIndexCorrected
    }

    fun withLoad(value: Double, isPositive: Boolean): Double {
        val currentMinute = LocalDateTime.now().minute

        if (currentMinute in 3..7 ||
                currentMinute in 15..26 ||
                currentMinute in 28..35 ||
                currentMinute in 40..50 ||
                currentMinute in 57..59) {
            return value + if (isPositive) value * 0.2 else (-value * 0.3)
        }
        return value
    }

    @PostMapping("/write")
    fun sort(@RequestBody request: ReadWriteRequest): Response {

//        TimeUnit.MILLISECONDS.sleep((90..260).random().toLong())

//        if not found return 14
        val item: DriveParameter = repository.findByParamIdAndParamIndex(request.param, request.index)
        if (item.value == "") {
            return ReadWriteResponse(14, "")
        }

        item.value = request.value
        repository.save(item)

        return ReadWriteResponse(0, "")
    }
}