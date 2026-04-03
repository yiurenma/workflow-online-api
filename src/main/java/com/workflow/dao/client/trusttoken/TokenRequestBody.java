package com.workflow.dao.client.trusttoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequestBody {

    @JsonProperty("input_token_state")
    InputTokenState inputTokenState;

    @JsonProperty("output_token_state")
    OutputTokenState outputTokenState;
}
