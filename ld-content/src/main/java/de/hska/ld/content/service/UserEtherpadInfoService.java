package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.UserEtherpadInfo;

public interface UserEtherpadInfoService {

    public UserEtherpadInfo save(UserEtherpadInfo userEtherpadInfo);

    public UserEtherpadInfo findById(Long id);

}
