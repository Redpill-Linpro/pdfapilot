package org.redpill.pdfapilot.promus.repository;

import org.redpill.pdfapilot.promus.domain.OAuth2AuthenticationRefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the OAuth2AuthenticationRefreshToken entity.
 */
public interface OAuth2RefreshTokenRepository extends MongoRepository<OAuth2AuthenticationRefreshToken, String> {

    public OAuth2AuthenticationRefreshToken findByTokenId(String tokenId);
}
