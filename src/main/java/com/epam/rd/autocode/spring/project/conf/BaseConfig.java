package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Order;
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
                .addMappings(mapper -> {
                    mapper.map(src -> src.getBook().getName(), BookItemDTO::setBookName);
                });

        return modelMapper;


    }
}
