package com.aio.api.user.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;
import lombok.Setter;

/**
 * UserLoginRequest
 */

@Setter
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-11-17T00:13:03.771057300+08:00[Asia/Shanghai]")
public class UserLoginRequest {

  private String account;

  private String password;

  public UserLoginRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public UserLoginRequest(String account, String password) {
    this.account = account;
    this.password = password;
  }

  public UserLoginRequest account(String account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  */
  @NotNull 
  @Schema(name = "account", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("account")
  public String getAccount() {
    return account;
  }

    public UserLoginRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   * @return password
  */
  @NotNull 
  @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserLoginRequest userLoginRequest = (UserLoginRequest) o;
    return Objects.equals(this.account, userLoginRequest.account) &&
        Objects.equals(this.password, userLoginRequest.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, password);
  }

  @Override
  public String toString() {
      return "class UserLoginRequest {\n" +
              "    account: " + toIndentedString(account) + "\n" +
              "    password: " + "*" + "\n" +
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

