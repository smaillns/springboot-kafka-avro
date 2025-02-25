package com.example.demo.demokafka.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

/**
 * KAFKA UTILS.
 */
@NoArgsConstructor (access = PRIVATE)
public class KafkaUtil {

	public static final String RETRY_COUNT_HEADER = "retry_count";
	public static final String ERROR_MESSAGE_HEADER = "error_message";
	public static final String ERROR_DATE_HEADER = "error_date";
	public static final String ERROR_STACK_TRACE_HEADER = "error_stack_trace";

	public static void incrementRetryCountHeader(Headers headers) {
		String numberRetry = String.valueOf(parseInt(getSingleHeaderValue(headers, RETRY_COUNT_HEADER, "0")) + 1);
		headers.remove(RETRY_COUNT_HEADER);
		headers.add(newHeader(RETRY_COUNT_HEADER, numberRetry));
	}

	public static void addExceptionHeaders(Headers headers, @Nullable Throwable exception) {
		if (exception == null) {
			return;
		}
		headers.remove(ERROR_MESSAGE_HEADER);
		headers.remove(ERROR_DATE_HEADER);
		headers.remove(ERROR_STACK_TRACE_HEADER);
		headers.add(newHeader(ERROR_MESSAGE_HEADER, exception.getMessage()));
		headers.add(newHeader(ERROR_DATE_HEADER, now().format(ISO_LOCAL_DATE_TIME)));
		headers.add(newHeader(ERROR_STACK_TRACE_HEADER, getStackTrace(exception)));
	}

	public static Header newHeader(String name, String value) {
		Headers headers = new RecordHeaders();
		headers.add(new RecordHeader(name, value.getBytes()));
		return headers.iterator().next();
	}

	public static String getHeaderValue(Headers headers, String key, String delimiter) {
		return stream(headers.headers(key).spliterator(), false)
			.map(Header::value)
			.map(String::new)
			.collect(joining(delimiter));
	}

	public static String getSingleHeaderValue(Headers headers, String name, String defaultValue) {
		Header value = headers.lastHeader(name);
		return value != null ? new String(value.value()) : defaultValue;
	}
}