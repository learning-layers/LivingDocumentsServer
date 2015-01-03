package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.UserEtherpadInfo;
import de.hska.ld.content.persistence.repository.UserEtherpadInfoRepository;
import de.hska.ld.content.service.UserEtherpadInfoService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEtherpadInfoServiceImpl implements UserEtherpadInfoService {

    @Autowired
    private UserEtherpadInfoRepository userEtherpadInfoRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(readOnly = false)
    public UserEtherpadInfo save(UserEtherpadInfo userEtherpadInfo) {
        return userEtherpadInfoRepository.save(userEtherpadInfo);
    }

    @Override
    public UserEtherpadInfo findById(Long id) {
        return userEtherpadInfoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeSessionForUser(String sessionId, Long validUntil, UserEtherpadInfo userEtherpadInfo) {
        userEtherpadInfo = userEtherpadInfoRepository.findById(userEtherpadInfo.getId());
        userEtherpadInfo.setSessionId(sessionId);
        userEtherpadInfo.setValidUntil(validUntil);
        userEtherpadInfoRepository.save(userEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeAuthorIdForCurrentUser(String authorId) {
        // 1.1.2.1 register an AuthorId for the Etherpad Server and store the Author Id for current user
        UserEtherpadInfo userEtherpadInfo = new UserEtherpadInfo();
        userEtherpadInfo.setAuthorId(authorId);
        User currentUser = Core.currentUser();
        User user = userService.findById(currentUser.getId());
        userEtherpadInfo.setUser(user);
        userEtherpadInfoRepository.save(userEtherpadInfo);
    }
}
