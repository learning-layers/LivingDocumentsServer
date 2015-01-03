package de.hska.ld.etherpad.service;

import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;

public interface UserEtherpadInfoService {

    public UserEtherpadInfo save(UserEtherpadInfo userEtherpadInfo);

    public UserEtherpadInfo findById(Long id);

    public void storeSessionForUser(String sessionId, Long validUntil, UserEtherpadInfo userEtherpadInfo);

    public void storeAuthorIdForCurrentUser(String authorId);

    public UserEtherpadInfo getUserEtherpadInfoForCurrentUser();
}
