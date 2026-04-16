package com.medicatch.insurance.codef;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@Builder
@ToString
public class TwoWayContext {

    private final int jobIndex;
    private final int threadIndex;
    private final String jti;
    private final long twoWayTimestamp;

    public HashMap<String, Object> toParamMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("jobIndex", jobIndex);
        map.put("threadIndex", threadIndex);
        map.put("jti", jti);
        map.put("twoWayTimestamp", twoWayTimestamp);
        return map;
    }
}
