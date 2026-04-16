package com.medicatch.insurance.codef;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwoWayAuthHandler {

    private final CodefInsuranceClient codefClient;
    private final CodefResponseParser responseParser;

    public CodefResponse executeFirst(String productUrl, HashMap<String, Object> params) {
        log.debug("CODEF 1차 요청: url={}", productUrl);
        String rawJson = codefClient.requestProduct(productUrl, params);
        CodefResponse response = responseParser.parse(rawJson);
        log.debug("CODEF 1차 응답: code={}", response.getResultCode());
        return response;
    }

    public CodefResponse executeSecond(String productUrl,
                                       HashMap<String, Object> params,
                                       TwoWayContext ctx) {
        injectTwoWayParams(params, ctx);
        log.debug("CODEF 2차 요청: url={}, jti={}", productUrl, ctx.getJti());
        String rawJson = codefClient.requestCertification(productUrl, params);
        CodefResponse response = responseParser.parse(rawJson);
        log.debug("CODEF 2차 응답: code={}", response.getResultCode());
        return response;
    }

    public TwoWayContext extractContext(CodefResponse response) {
        return responseParser.extractTwoWayContext(response);
    }

    private void injectTwoWayParams(HashMap<String, Object> params, TwoWayContext ctx) {
        params.put("is2Way", true);
        params.put("simpleAuth", "1");
        params.put("twoWayInfo", ctx.toParamMap());
    }
}
