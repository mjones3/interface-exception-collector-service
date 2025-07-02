package com.arcone.biopro.distribution.irradiation.adapter.in.listener.completed;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.irradiation.application.dto.VolumeInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ProductCompletedMessageMapper extends MessageMapper<ProductCompletedInput, ProductCompletedMessage> {

    String VOLUME_TYPE = "volume";
    String ANTICOAGULANT_VOLUME_TYPE = "anticoagulantVolume";

    @Mapping(target = "volumes", expression = "java(createVolumeInputs(message.volume(), message.anticoagulantVolume()))")
    ProductCompletedInput toInput(ProductCompletedMessage message);

    default List<VolumeInput> createVolumeInputs(Volume volume, Volume anticoagulantVolume) {
        List<VolumeInput> volumeInputs = new ArrayList<>();
        if (Objects.nonNull(volume)) {
            volumeInputs.add(new VolumeInput(VOLUME_TYPE, volume.value(), volume.unit()));
        }
        if (Objects.nonNull(anticoagulantVolume)) {
            volumeInputs.add(new VolumeInput(ANTICOAGULANT_VOLUME_TYPE, anticoagulantVolume.value(), anticoagulantVolume.unit()));
        }
        return volumeInputs;
    }

}
