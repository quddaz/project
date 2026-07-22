package com.woowapractice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest);
    String githubId = String.valueOf(user.getAttribute("id"));
    String login = String.valueOf(user.getAttribute("login"));
    String displayName = user.getAttribute("name");
    userRepository
        .findByGithubId(githubId)
        .map(
            saved -> {
              saved.updateProfile(login, displayName);
              return saved;
            })
        .orElseGet(() -> userRepository.save(User.create(githubId, login, displayName)));
    return user;
  }
}
