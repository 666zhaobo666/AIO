package com.aio.api.user.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Generated;
import lombok.Setter;

/**
 * LoginResponse
 */

@Setter
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-11-17T00:13:03.771057300+08:00[Asia/Shanghai]")
public class LoginResponse {

  private String token;

  private UUID userId;

  private String username;

  private String role;

  public LoginResponse token(String token) {
    this.token = token;
    return this;
  }

  /**
   * JWT令牌
   * @return token
  */
  
  @Schema(name = "token", description = "JWT令牌", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("token")
  public String getToken() {
    return token;
  }

    public LoginResponse userId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   * @return userId
  */
  @Valid 
  @Schema(name = "userId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("userId")
  public UUID getUserId() {
    return userId;
  }

    public LoginResponse username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
  */
  
  @Schema(name = "username", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

    public LoginResponse role(String role) {
    this.role = role;
    return this;
  }

  /**
   * Get role
   * @return role
  */
  
  @Schema(name = "role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("role")
  public String getRole() {
    return role;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginResponse loginResponse = (LoginResponse) o;
    return Objects.equals(this.token, loginResponse.token) &&
        Objects.equals(this.userId, loginResponse.userId) &&
        Objects.equals(this.username, loginResponse.username) &&
        Objects.equals(this.role, loginResponse.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, userId, username, role);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginResponse {\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("}");
    return sb.toString();
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

