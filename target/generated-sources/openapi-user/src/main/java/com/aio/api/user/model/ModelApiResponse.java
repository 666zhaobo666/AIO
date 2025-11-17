package com.aio.api.user.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;
import lombok.Setter;

/**
 * ModelApiResponse
 */

@Setter
@JsonTypeName("ApiResponse")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-11-17T00:13:03.771057300+08:00[Asia/Shanghai]")
public class ModelApiResponse {

  private Integer code;

  private String message;

  private Object data;

  public ModelApiResponse code(Integer code) {
    this.code = code;
    return this;
  }

  /**
   * 状态码（200成功，其他失败）
   * @return code
  */
  
  @Schema(name = "code", description = "状态码（200成功，其他失败）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("code")
  public Integer getCode() {
    return code;
  }

    public ModelApiResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * 提示信息
   * @return message
  */
  
  @Schema(name = "message", description = "提示信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

    public ModelApiResponse data(Object data) {
    this.data = data;
    return this;
  }

  /**
   * Get data
   * @return data
  */
  
  @Schema(name = "data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("data")
  public Object getData() {
    return data;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelApiResponse _apiResponse = (ModelApiResponse) o;
    return Objects.equals(this.code, _apiResponse.code) &&
        Objects.equals(this.message, _apiResponse.message) &&
        Objects.equals(this.data, _apiResponse.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, data);
  }

  @Override
  public String toString() {
      return "class ModelApiResponse {\n" +
              "    code: " + toIndentedString(code) + "\n" +
              "    message: " + toIndentedString(message) + "\n" +
              "    data: " + toIndentedString(data) + "\n" +
              "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

