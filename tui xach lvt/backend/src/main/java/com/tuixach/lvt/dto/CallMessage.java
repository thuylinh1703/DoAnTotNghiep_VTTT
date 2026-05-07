package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallMessage {
    private String type; // offer, answer, candidate, call-request, call-accepted, call-rejected, call-ended
    private String from;
    private String to;
    private Object data;
}
