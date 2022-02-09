package de.caritas.cob.userservice.api.port.in;

import java.util.Map;
import java.util.Optional;

public interface IdentityManaging {

  Optional<String> setUpOneTimePassword(String username, String email);

  void setUpOneTimePassword(String username, String initialCode, String secret);

  Map<String, Boolean> validateOneTimePassword(String username, String email, String code);

  void deleteOneTimePassword(String username);
}
