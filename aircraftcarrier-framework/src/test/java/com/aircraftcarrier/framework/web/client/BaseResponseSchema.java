package com.aircraftcarrier.framework.web.client;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuzhipeng
 * @description
 * @create 2019-11-26 11:14
 */
public class BaseResponseSchema implements Serializable {
    private static final long serialVersionUID = -8471155212679251263L;

    public static final String SERIALIZED_NAME_RESPONSE_HOST = "responseHost";
    @SerializedName(SERIALIZED_NAME_RESPONSE_HOST)
    protected String responseHost;

    public static final String SERIALIZED_NAME_RESPONSE_ID = "responseId";
    @SerializedName(SERIALIZED_NAME_RESPONSE_ID)
    protected String responseId;

    public static final String ERROR_CODE = "errorCode";
    @SerializedName(ERROR_CODE)
    protected String errorCode;

    public static final String ERROR_DESCRIPTION = "errorDescription";
    @SerializedName(ERROR_DESCRIPTION)
    protected String errorDescription;

    public static final String SERIALIZED_NAME_ERRORS = "errors";
    @SerializedName(SERIALIZED_NAME_ERRORS)
    protected List<Error> errors = new ArrayList<Error>();


    /**
     * The MasterCard host that originated the request. Future calls in the same conversation may be routed to this
     * host.
     *
     * @return responseHost
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "site2.payment-app-provider.com", value = "The MasterCard host that originated the " +
            "request. Future calls in the same conversation may be routed to this host.  ")
    public String getResponseHost() {
        return responseHost;
    }

    public void setResponseHost(String responseHost) {
        this.responseHost = responseHost;
    }

    /**
     * Unique identifier for the response.
     *
     * @return responseId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "123456", value = "Unique identifier for the response. ")
    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    /**
     * @return errorCode
     */
    @Nullable
    @ApiModelProperty(example = "example", required = true, value = "value")
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return errorDescription
     */
    @Nullable
    @ApiModelProperty(example = "example", required = true, value = "value")
    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * @return errors
     */
    @Nullable
    @ApiModelProperty(example = "example", required = true, value = "value")
    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
