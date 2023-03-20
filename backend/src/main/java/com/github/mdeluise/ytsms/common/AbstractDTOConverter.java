package com.github.mdeluise.ytsms.common;

public abstract class AbstractDTOConverter<D, T> {
    public abstract D convertFromDTO(T dto);

    public abstract T convertToDTO(D data);
}
