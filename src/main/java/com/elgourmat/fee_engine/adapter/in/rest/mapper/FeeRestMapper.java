package com.elgourmat.fee_engine.adapter.in.rest.mapper;

import com.elgourmat.fee_engine.adapter.in.rest.dto.FeeBreakdownResponse;
import com.elgourmat.fee_engine.adapter.in.rest.dto.FeeLineResponse;
import com.elgourmat.fee_engine.adapter.in.rest.dto.TransactionRequest;
import com.elgourmat.fee_engine.application.command.CalculateFeesCommand;
import com.elgourmat.fee_engine.domain.model.FeeBreakdown;
import com.elgourmat.fee_engine.domain.model.FeeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FeeRestMapper {

    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency", qualifiedByName = "upper")
    @Mapping(target = "customerType", source = "customerType", qualifiedByName = "upper")
    @Mapping(target = "channel", source = "channel", qualifiedByName = "upper")
    @Mapping(target = "countryCode", source = "countryCode", qualifiedByName = "upper")
    CalculateFeesCommand toCommand(TransactionRequest request);

    @Mapping(target = "currency", expression = "java(breakdown.transaction().currency().name())")
    @Mapping(target = "transactionAmount", expression = "java(breakdown.transaction().amount().amount())")
    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "totalFees", expression = "java(breakdown.totalFees().amount())")
    @Mapping(target = "grandTotal", expression = "java(breakdown.grandTotal().amount())")
    FeeBreakdownResponse toResponse(FeeBreakdown breakdown);

    @Mapping(target = "ruleName", source = "ruleName")
    @Mapping(target = "type", expression = "java(line.type().name())")
    @Mapping(target = "amount", expression = "java(line.amount().amount())")
    @Mapping(target = "currency", expression = "java(line.amount().currency().name())")
    @Mapping(target = "reason", source = "reason")
    FeeLineResponse toLineResponse(FeeLine line);

    @Named("upper")
    default String upper(String value) {
        return value == null ? null : value.toUpperCase();
    }
}
