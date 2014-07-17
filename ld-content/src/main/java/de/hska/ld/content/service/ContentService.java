package de.hska.ld.content.service;

import de.hska.ld.core.service.Service;

public interface ContentService<T> extends Service<T> {

    T loadContentCollection(T t, Class... clazz);
}
