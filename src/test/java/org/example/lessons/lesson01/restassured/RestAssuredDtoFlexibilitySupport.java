package org.example.lessons.lesson01.restassured;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;

/** Примеры JSON для второй части. Служебный код изменять не нужно. */
abstract class RestAssuredDtoFlexibilitySupport {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected static final String USER_WITH_NULL_PROFILE_JSON = """
            {
              "id": 7,
              "name": "Anna",
              "profile": null
            }
            """;

    protected static final String USER_WITHOUT_PROFILE_JSON = """
            {
              "id": 7,
              "name": "Anna"
            }
            """;

    protected static final String USER_WITH_EXTRA_FIELD_JSON = """
            {
              "id": 7,
              "name": "Anna",
              "profile": {
                "city": "Kaluga"
              },
              "traceId": "trace-123"
            }
            """;

    protected static final String BOOKING_ENVELOPE_JSON = """
            {
              "status": "ok",
              "data": {
                "bookingid": 101,
                "firstname": "Anna"
              },
              "error": null
            }
            """;

    protected static final String BOOKING_LIST_ENVELOPE_JSON = """
            {
              "status": "ok",
              "data": [
                {"bookingid": 101, "firstname": "Anna"},
                {"bookingid": 102, "firstname": "Ivan"}
              ]
            }
            """;

    protected static final String EVENTS_JSON = """
            [
              {
                "type": "created",
                "bookingId": 101,
                "author": "api"
              },
              {
                "type": "price_changed",
                "bookingId": 101,
                "oldPrice": 100,
                "newPrice": 150
              },
              {
                "type": "deleted",
                "bookingId": 102,
                "reason": "duplicate"
              }
            ]
            """;

    protected static final String MIXED_ITEMS_JSON = """
            [
              {"bookingid": 101},
              "heartbeat",
              {"warning": "slow response"},
              null,
              42
            ]
            """;

    protected Response jsonResponse(String json) {
        return new ResponseBuilder()
                .setStatusCode(200)
                .setContentType("application/json")
                .setBody(json)
                .build();
    }
}
