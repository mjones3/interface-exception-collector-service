package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

public interface MessageMapper<TInput, TMessage> {

    TInput toInput(TMessage message);
}
