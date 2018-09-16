package com.helospark.tactview.api.commands;

import lombok.Data;

@Data
public class ClipAddedResponse {
    private boolean success;
    private String reason;

    public ClipAddedResponse(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }



}
