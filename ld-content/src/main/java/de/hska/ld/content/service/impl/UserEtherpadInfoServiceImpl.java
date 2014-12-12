package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.UserEtherpadInfo;
import de.hska.ld.content.persistence.repository.UserEtherpadInfoRepository;
import de.hska.ld.content.service.UserEtherpadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEtherpadInfoServiceImpl implements UserEtherpadInfoService {

    @Autowired
    private UserEtherpadInfoRepository userEtherpadInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEtherpadInfo save(UserEtherpadInfo userEtherpadInfo) {
        return userEtherpadInfoRepository.save(userEtherpadInfo);
    }


}
