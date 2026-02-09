package com.danil.appliances.mapper;

import com.danil.appliances.dto.OrderDetailsDto;
import com.danil.appliances.dto.OrderListItemDto;
import com.danil.appliances.dto.OrderRowDto;
import com.danil.appliances.model.Client;
import com.danil.appliances.model.OrderRow;
import com.danil.appliances.model.Orders;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderRowMapper orderRowMapper;

    @Override
    public OrderDetailsDto toDetailsDto(Orders order) {
        if ( order == null ) {
            return null;
        }

        OrderDetailsDto.OrderDetailsDtoBuilder orderDetailsDto = OrderDetailsDto.builder();

        orderDetailsDto.clientName( orderClientName( order ) );
        orderDetailsDto.orderStatus( order.getOrderStatus() );
        orderDetailsDto.rows( orderRowSetToOrderRowDtoList( order.getRows() ) );
        orderDetailsDto.id( order.getId() );

        orderDetailsDto.employeeName( order.getEmployee() == null ? null : order.getEmployee().getName() );
        orderDetailsDto.amount( order.getAmount() );

        return orderDetailsDto.build();
    }

    @Override
    public OrderListItemDto toListItemDto(Orders order) {
        if ( order == null ) {
            return null;
        }

        OrderListItemDto.OrderListItemDtoBuilder orderListItemDto = OrderListItemDto.builder();

        orderListItemDto.clientName( orderClientName( order ) );
        orderListItemDto.orderStatus( order.getOrderStatus() );
        orderListItemDto.id( order.getId() );

        orderListItemDto.employeeName( order.getEmployee() == null ? null : order.getEmployee().getName() );
        orderListItemDto.amount( order.getAmount() );

        return orderListItemDto.build();
    }

    private String orderClientName(Orders orders) {
        if ( orders == null ) {
            return null;
        }
        Client client = orders.getClient();
        if ( client == null ) {
            return null;
        }
        String name = client.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected List<OrderRowDto> orderRowSetToOrderRowDtoList(Set<OrderRow> set) {
        if ( set == null ) {
            return null;
        }

        List<OrderRowDto> list = new ArrayList<OrderRowDto>( set.size() );
        for ( OrderRow orderRow : set ) {
            list.add( orderRowMapper.toDto( orderRow ) );
        }

        return list;
    }
}
