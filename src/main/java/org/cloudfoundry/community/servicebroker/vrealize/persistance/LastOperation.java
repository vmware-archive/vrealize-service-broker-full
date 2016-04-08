package org.cloudfoundry.community.servicebroker.vrealize.persistance;

import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;

import java.io.Serializable;

public class LastOperation implements Serializable {

    private OperationState state;

    private String description;

    private boolean isDelete;

    public static LastOperation fromResponse(GetLastServiceOperationResponse response) {
        return new LastOperation(response.getState(), response.getDescription(), response.isDeleteOperation());
    }

    public LastOperation(OperationState state, String description, boolean isDelete) {
        setState(state);
        setDescription(description);
        setDelete(isDelete);
    }

    public OperationState getState() {
        return state;
    }

    public void setState(OperationState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public GetLastServiceOperationResponse toResponse() {
        return new GetLastServiceOperationResponse().
                withDescription(getDescription()).
                withOperationState(getState()).
                withDeleteOperation(isDelete());
    }
}
