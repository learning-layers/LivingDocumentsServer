package de.hska.ld.core.logging;

import java.util.UUID;

public interface ExceptionLogger {

    UUID log(Throwable ex);

}
