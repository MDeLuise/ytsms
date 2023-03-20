package com.github.mdeluise.ytsms.common;

public interface IdentifiedEntity<E> {
    E getId();

    void setId(E id);
}
