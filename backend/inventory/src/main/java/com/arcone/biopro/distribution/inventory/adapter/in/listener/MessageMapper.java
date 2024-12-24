package com.arcone.biopro.distribution.inventory.adapter.in.listener;

public interface MessageMapper<TInput, TMessage> {

    TInput toInput(TMessage message);
}
