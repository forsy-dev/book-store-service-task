package com.forsy.conf;

import com.forsy.dto.BookItemDTO;
import com.forsy.dto.OrderDTO;
import com.forsy.model.BookItem;
import com.forsy.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfig{

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Order.class, OrderDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getClient().getEmail(), OrderDTO::setClientEmail);
                    mapper.map(src -> src.getEmployee().getEmail(), OrderDTO::setEmployeeEmail);
                });

        modelMapper.createTypeMap(BookItem.class, BookItemDTO.class)
                .addMappings(mapper ->
                    mapper.map(src -> src.getBook().getName(), BookItemDTO::setBookName));

        return modelMapper;


    }
}
